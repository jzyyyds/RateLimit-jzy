package org.example.ratelimitjzycore.provide;

import com.sun.javafx.css.Rule;
import jodd.util.StringUtil;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.ratelimitjztcommon.enums.RateLimitTypeEnum;
import org.example.ratelimitjztcommon.excepition.ExecuteFunctionException;
import org.example.ratelimitjztcommon.model.RateLimitRule;
import org.example.ratelimitjzycore.annotation.RateLimit;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class RuleProvider implements BeanFactoryAware {
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser parser = new SpelExpressionParser();
    private static final TemplateParserContext PARSER_CONTEXT = new TemplateParserContext();
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public RateLimitRule getRule(ProceedingJoinPoint joinPoint, RateLimit rateLimit){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取key的值
        //1. 先获取默认的值
        String keyName = getDefaultKeyName(signature);
        if (StringUtils.hasLength(rateLimit.key())) {
            keyName = rateLimit.key();
        }
        //基于spel来解析表达式yml文件
        if (StringUtils.hasLength(rateLimit.keyExpressin())) {
            String keyExpression = getKeyExpression(rateLimit);
            if (!ObjectUtils.isEmpty(keyExpression)) {
                keyName = keyExpression;
            }
        }
        //解析spel的值
        if (rateLimit.keys().length > 0) {
            keyName = getSpelKeyName(joinPoint,rateLimit);
        }
        int rate = getRateValue(rateLimit);
        int time = getTimeValue(rateLimit);
        int capacity = getCapaCity(rateLimit);
        RateLimitTypeEnum rateLimitTypeEnum = rateLimit.rateLimitType();
        RateLimitRule rateLimitRule = RateLimitRule.builder()
                .rate(rate)
                .key(keyName)
                .rateLimitType(rateLimitTypeEnum.getKey())
                .time(time)
                .capacity(capacity)
                .fallbackFunction(rateLimit.fallbackFunction())
                .build();
        return rateLimitRule;
    }

    private int getCapaCity(RateLimit rateLimit) {
        if (StringUtils.hasLength(rateLimit.capacityExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.capacityExpression()), PARSER_CONTEXT).getValue(String.class);
            return Integer.valueOf(value);
        }
        return rateLimit.capacity();
    }

    private int getTimeValue(RateLimit rateLimit) {
        if (StringUtils.hasLength(rateLimit.timeExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.timeExpression()), PARSER_CONTEXT).getValue(String.class);
            return Integer.valueOf(value);
        }
        return rateLimit.time();
    }

    private int getRateValue(RateLimit rateLimit) {
        if (StringUtils.hasLength(rateLimit.rateExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.rateExpression()), PARSER_CONTEXT).getValue(String.class);
            return Integer.valueOf(value);
        }
        return rateLimit.rate();
    }

    private String getKeyExpression(RateLimit rateLimit) {
        String value = parser.parseExpression(resolve(rateLimit.keyExpressin()), PARSER_CONTEXT).getValue(String.class);
        if (null != value) {
            return value;
        }
        return null;
    }

    private String resolve(String expression) {
        return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(expression);
    }

    private String getSpelKeyName(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        Method method = getMethod(joinPoint);
        List<String> spelKeys = getSpelKeys(rateLimit.keys(),method,joinPoint.getArgs());
        return StringUtils.collectionToDelimitedString(spelKeys,"","-","");
    }

    private List<String> getSpelKeys(String[] keys, Method method, Object[] args) {
        List<String> keyList = new ArrayList<>();
        for (String key : keys) {
            if (!ObjectUtils.isEmpty(key)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, args, nameDiscoverer);
                Object value = parser.parseExpression(key).getValue(context);
                keyList.add(ObjectUtils.nullSafeToString(value));
            }
        }
        return keyList;
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    private String getDefaultKeyName(MethodSignature signature) {
        return String.format("%s.%s",signature.getDeclaringTypeName(),signature.getMethod().getName());
    }

    /**
     * 执行自定义的函数
     * @param fallbackFunction
     * @param joinPoint
     * @return
     */
    public Object executeFunction(String fallbackFunction, ProceedingJoinPoint joinPoint) {
        //使用反射来实现
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method methodHandle;
        try {
            //获取要调用的方法，并且设置成可以访问
            methodHandle = joinPoint.getTarget().getClass().getDeclaredMethod(fallbackFunction, currentMethod.getParameterTypes());
            methodHandle.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy", e);
        }
        //获取连接点的参数
        Object[] args = joinPoint.getArgs();
        //调用
        Object res;
        try {
            res = methodHandle.invoke(target,args);
        } catch (IllegalAccessException e) {
            throw new ExecuteFunctionException("fail to invoke executeFunction",e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
}
