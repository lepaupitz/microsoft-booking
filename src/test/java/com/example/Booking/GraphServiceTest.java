
package com.example.Booking;

import com.example.Booking.config.MicrosoftGraphClientConfig;
import com.example.Booking.entity.AuthProperties;
import com.example.Booking.service.MicrosoftGraphService;
import com.microsoft.graph.models.BookingService;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GraphServiceTest {


    @Mock
    private AuthProperties authProperties;

    @Mock
    private GraphServiceClient graphServiceClient;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private MicrosoftGraphService microsoftGraphService;

    @Mock
    MicrosoftGraphClientConfig graphClientConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(graphClientConfig.graphClient()).thenReturn(graphServiceClient);
        microsoftGraphService = new MicrosoftGraphService(graphClientConfig);
    }

    @Test
    void createGraphClientSuccessfully() {
        when(authProperties.getClientId()).thenReturn("client-id");
        when(authProperties.getClientSecret()).thenReturn("client-secret");
        when(authProperties.getTenantId()).thenReturn("tenant-id");
        when(authProperties.getScope()).thenReturn("scope");

        GraphServiceClient client = graphClientConfig.graphClient();

        assertNotNull(client);
    }

    @Test
    void createGraphClientWithInvalidCredentials() {
        when(authProperties.getClientId()).thenReturn(null);
        when(authProperties.getClientSecret()).thenReturn(null);
        when(authProperties.getTenantId()).thenReturn(null);
        when(authProperties.getScope()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            graphClientConfig.graphClient();
        });
    }

    @Test
    void createGraphClientWithNullClientId() {
        when(authProperties.getClientId()).thenReturn(null);
        when(authProperties.getClientSecret()).thenReturn("client-secret");
        when(authProperties.getTenantId()).thenReturn("tenant-id");

        assertThrows(RuntimeException.class, () -> {
            graphClientConfig.graphClient();
        });
    }

    @Test
    void createGraphClientWithNullClientSecret() {
        when(authProperties.getClientId()).thenReturn("client-id");
        when(authProperties.getClientSecret()).thenReturn(null);
        when(authProperties.getTenantId()).thenReturn("tenant-id");

        assertThrows(RuntimeException.class, () -> {
            graphClientConfig.graphClient();
        });
    }

    @Test
    void createGraphClientWithNullTenantId() {
        when(authProperties.getClientId()).thenReturn("client-id");
        when(authProperties.getClientSecret()).thenReturn("client-secret");
        when(authProperties.getTenantId()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            graphClientConfig.graphClient();
        });
    }

    @Test
    void createGraphClientThrowsRuntimeExceptionOnFailure() {
        when(authProperties.getClientId()).thenReturn("client-id");
        when(authProperties.getClientSecret()).thenReturn("client-secret");
        when(authProperties.getTenantId()).thenReturn("tenant-id");
        when(authProperties.getScope()).thenReturn("scope");

        doThrow(new RuntimeException("Failed to create GraphServiceClient")).when(authProperties).getClientId();

        assertThrows(RuntimeException.class, () -> {
            graphClientConfig.graphClient();
        });
    }

    @Test
    void listStaffMemberIdServices_withValidIds_shouldReturnStaffMemberIds() {
        when(graphClientConfig.graphClient()).thenReturn(graphServiceClient);
        when(graphServiceClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId("businessId")
                .services()
                .byBookingServiceId("serviceId")
                .get())
                .thenReturn(bookingService);

        List<String> mockStaffIds = List.of("staffId1", "staffId2");
        when(bookingService.getStaffMemberIds()).thenReturn(mockStaffIds);

        List<String> result = microsoftGraphService.listStaffMemberIdServices("businessId", "serviceId");
        assertEquals(mockStaffIds, result);
    }

}

