package com.example.Authenticator;

import com.microsoft.graph.models.BookingAppointment;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.*;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphServiceTest {


    @Mock
    private AuthProperties authProperties;

    @Mock
    private GraphServiceClient<Request> graphClient;

    private GraphService graphService;

    @Mock
    private UserCollectionPage userCollectionPage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graphService = new GraphService();
        graphService.authProperties = authProperties;
    }


    @Test
    void getAllUsersSuccessfully() {
        List<User> users = Arrays.asList(new User(), new User());

        when(authProperties.getClientId()).thenReturn("client-id");
        when(authProperties.getClientSecret()).thenReturn("client-secret");
        when(authProperties.getTenantId()).thenReturn("tenant-id");
        when(graphClient.users().buildRequest().get()).thenReturn(userCollectionPage);
        when(userCollectionPage.getCurrentPage()).thenReturn(users);

        List<User> result = graphService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllUsersWithException() {
        when(authProperties.getClientId()).thenReturn("client-id");
        when(authProperties.getClientSecret()).thenReturn("client-secret");
        when(authProperties.getTenantId()).thenReturn("tenant-id");
        when(graphClient.users().buildRequest().get()).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> {
            graphService.getAllUsers();
        });
    }
}
