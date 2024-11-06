package com.example.Booking.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class AuthPropertiesTest {

    @Mock
    private AuthProperties authProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getClientIdSuccessfully() {
        when(authProperties.getClientId()).thenReturn("client-id");

        String clientId = authProperties.getClientId();

        assertNotNull(clientId);
        assertEquals("client-id", clientId);
    }

    @Test
    void getClientSecretSuccessfully() {
        when(authProperties.getClientSecret()).thenReturn("client-secret");

        String clientSecret = authProperties.getClientSecret();

        assertNotNull(clientSecret);
        assertEquals("client-secret", clientSecret);
    }

    @Test
    void getScopeSuccessfully() {
        when(authProperties.getScope()).thenReturn("scope");

        String scope = authProperties.getScope();

        assertNotNull(scope);
        assertEquals("scope", scope);
    }

    @Test
    void getTenantIdSuccessfully() {
        when(authProperties.getTenantId()).thenReturn("tenant-id");

        String tenantId = authProperties.getTenantId();

        assertNotNull(tenantId);
        assertEquals("tenant-id", tenantId);
    }

    @Test
    void getGrantTypeSuccessfully() {
        when(authProperties.getGrantType()).thenReturn("grant-type");

        String grantType = authProperties.getGrantType();

        assertNotNull(grantType);
        assertEquals("grant-type", grantType);
    }
}
