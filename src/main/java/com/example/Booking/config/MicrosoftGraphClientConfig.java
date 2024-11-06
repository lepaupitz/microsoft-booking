package com.example.Booking.config;


import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.example.Booking.entity.AuthProperties;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class MicrosoftGraphClientConfig {

    private AuthProperties authProperties;

    @Bean
    public GraphServiceClient graphClient() {

        try{
            if (authProperties.getClientId() == null || authProperties.getClientSecret() == null || authProperties.getTenantId() == null) {
                throw new RuntimeException("Invalid credentials");
            }

            ClientSecretCredential clientSecretCredential = createClientSecretCredential();
            return new GraphServiceClient(clientSecretCredential, authProperties.getScope());

        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private ClientSecretCredential createClientSecretCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(authProperties.getClientId())
                .clientSecret(authProperties.getClientSecret())
                .tenantId(authProperties.getTenantId())
                .build();
    }

}
