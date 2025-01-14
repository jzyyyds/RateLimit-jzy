package org.example.ratelimitjzycore.provide;

import com.sun.javafx.css.Rule;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.ratelimitjztcommon.enums.RateLimitTypeEnum;
import org.example.ratelimitjztcommon.excepition.ExecuteFunctionException;
import org.example.ratelimitjztcommon.model.RateLimitRule;
import org.example.ratelimitjzycore.annotation.RateLimit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RuleProvider {
    public RateLimitRule getRule(ProceedingJoinPoint joinPoint, RateLimit rateLimit){
        String key = rateLimit.key();
        int rate = rateLimit.rate();
        RateLimitTypeEnum rateLimitTypeEnum = rateLimit.rateLimitType();
        RateLimitRule rateLimitRule = RateLimitRule.builder()
                .rate(rate)
                .key(key)
                .rateLimitType(rateLimitTypeEnum.getKey())
                .time(rateLimit.time())
                .capacity(rateLimit.capacity())
                .fallbackFunction(rateLimit.fallbackFunction())
                .build();
        return rateLimitRule;
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
