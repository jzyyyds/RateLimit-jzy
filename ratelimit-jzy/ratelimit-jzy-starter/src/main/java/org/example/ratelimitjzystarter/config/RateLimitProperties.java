package org.example.ratelimitjzystarter.config;

import org.example.ratelimitjztcommon.constant.Constant;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = Constant.RATELIMIT_PROPERTIES)
public class RateLimitProperties {
    private int code = 510;
    private String responseBody =  "{\"code\":510,\"msg\":\"Can not allow\"}";

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
