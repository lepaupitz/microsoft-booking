package com.example.Authenticator;

import com.microsoft.graph.models.BookingAppointment;
import com.microsoft.graph.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


public class GraphControllerTests {

    @Mock
    private GraphService graphService;

    private GraphController graphController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graphController = new GraphController();
        graphController.graphService = graphService;
    }

    @Test
    void createAppointmentSuccessfully() {
        String businessId = "business-id";
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        BookingAppointment bookingAppointment = new BookingAppointment();

        when(graphService.createAppointment(businessId, appointmentDTO)).thenReturn(bookingAppointment);

        ResponseEntity<BookingAppointment> response = graphController.createAppointment(businessId, appointmentDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(bookingAppointment, response.getBody());
    }

    @Test
    void createAppointmentWithException() {
        String businessId = "business-id";
        AppointmentDTO appointmentDTO = new AppointmentDTO();

        when(graphService.createAppointment(businessId, appointmentDTO)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BookingAppointment> response = graphController.createAppointment(businessId, appointmentDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAllAppointmentsSuccessfully() throws IOException, ExecutionException, InterruptedException {
        String bookingBusinessId = "business-id";
        List<BookingAppointment> appointments = Arrays.asList(new BookingAppointment(), new BookingAppointment());

        when(graphService.getAllAppointments(bookingBusinessId)).thenReturn(appointments);

        List<BookingAppointment> result = graphController.getAllAppointments(bookingBusinessId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllAppointmentsWithException() throws IOException, ExecutionException, InterruptedException {
        String bookingBusinessId = "business-id";

        when(graphService.getAllAppointments(bookingBusinessId)).thenThrow(new IOException("Error"));

        assertThrows(RuntimeException.class, () -> {
            graphController.getAllAppointments(bookingBusinessId);
        });
    }

    @Test
    void getUsersSuccessfully() {
        List<User> users = Arrays.asList(new User(), new User());

        when(graphService.getAllUsers()).thenReturn(users);

        List<User> result = graphController.getUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getUsersWithException() {
        when(graphService.getAllUsers()).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> {
            graphController.getUsers();
        });
    }
}
