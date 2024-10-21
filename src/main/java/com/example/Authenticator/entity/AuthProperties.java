package com.example.Authenticator.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
@Data
public class AuthProperties {

    private String clientId;
    private String clientSecret;
    private String scope;
    private String tenantId;
    private String grantType;

}
