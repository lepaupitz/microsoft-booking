package com.example.Booking;

import com.example.Booking.controller.MicrosoftGraphController;
import com.example.Booking.service.MicrosoftGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


public class GraphControllerTests {

    @Mock
    private MicrosoftGraphService microsoftGraphService;

    @InjectMocks
    private MicrosoftGraphController microsoftGraphController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getServiceStaffMemberIds_withValidIds_shouldReturnStaffMemberIds() {
        String bookingBusinessId = "valid-business-id";
        String serviceId = "valid-service-id";
        List<String> expectedStaffMemberIds = Arrays.asList("staff1", "staff2");

        when(microsoftGraphService.listStaffMemberIdServices(bookingBusinessId, serviceId))
                .thenReturn(expectedStaffMemberIds);

        ResponseEntity<List<String>> response = microsoftGraphController.getServiceStaffMemberIds(bookingBusinessId, serviceId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStaffMemberIds, response.getBody());
    }

    @Test
    void getServiceStaffMemberIds_withInvalidBusinessId_shouldReturnEmptyList() {
        String invalidBusinessId = "invalid-business-id";
        String serviceId = "valid-service-id";
        List<String> expectedStaffMemberIds = Collections.emptyList();

        when(microsoftGraphService.listStaffMemberIdServices(invalidBusinessId, serviceId))
                .thenReturn(expectedStaffMemberIds);

        ResponseEntity<List<String>> response = microsoftGraphController.getServiceStaffMemberIds(invalidBusinessId, serviceId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getServiceStaffMemberIds_withNullServiceId_shouldThrowException() {
        String bookingBusinessId = "valid-business-id";
        String serviceId = null;

        when(microsoftGraphService.listStaffMemberIdServices(bookingBusinessId, serviceId))
                .thenThrow(new IllegalArgumentException("Service ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            microsoftGraphController.getServiceStaffMemberIds(bookingBusinessId, serviceId);
        });
    }

    @Test
    void getServiceDefaultDuration_withValidIds_shouldReturnDuration() {
        String bookingBusinessId = "valid-business-id";
        String serviceId = "valid-service-id";
        Duration expectedDuration = Duration.ofMinutes(30);

        when(microsoftGraphService.getServiceDefaultDuration(bookingBusinessId, serviceId))
                .thenReturn(expectedDuration);

        ResponseEntity<Duration> response = microsoftGraphController.getServiceDefaultDuration(bookingBusinessId, serviceId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDuration, response.getBody());
    }

    @Test
    void getServiceDefaultDuration_withInvalidBusinessId_shouldReturnServerError() {
        String invalidBusinessId = "invalid-business-id";
        String serviceId = "valid-service-id";

        when(microsoftGraphService.getServiceDefaultDuration(invalidBusinessId, serviceId))
                .thenThrow(new RuntimeException("Business not found"));

        ResponseEntity<Duration> response = microsoftGraphController.getServiceDefaultDuration(invalidBusinessId, serviceId);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getServiceDefaultDuration_withNullServiceId_shouldThrowException() {
        String bookingBusinessId = "valid-business-id";
        String serviceId = null;

        doThrow(new IllegalArgumentException("Service ID cannot be null"))
                .when(microsoftGraphService).getServiceDefaultDuration(bookingBusinessId, serviceId);

        assertThrows(IllegalArgumentException.class, () -> {
            microsoftGraphController.getServiceDefaultDuration(bookingBusinessId, serviceId);
        });
    }
}
