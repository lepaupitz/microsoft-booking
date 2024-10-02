package com.example.Authenticator;

import com.microsoft.graph.models.BookingAppointment;
import com.microsoft.graph.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/graph")
public class GraphController {

    @Autowired
    GraphService graphService;

    @GetMapping("/getUsers")
    public List<User> getUsers() {
            return graphService.getAllUsers();

    }

    @GetMapping("/getAppointments/{bookingBusinessId}")
    public List<BookingAppointment> getAllAppointments(@PathVariable String bookingBusinessId) {
        try {
            return graphService.getAllAppointments(bookingBusinessId);
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/appointments/{businessId}")
    public ResponseEntity<BookingAppointment> createAppointment(
            @PathVariable String businessId,
            @RequestBody AppointmentDTO appointmentDTO) {
        try {
            BookingAppointment createdAppointment = graphService.createAppointment(businessId, appointmentDTO);
            return new ResponseEntity<>(createdAppointment, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/serviceStaff/{bookingBusinessId}/{serviceId}")
    public ResponseEntity<List<String>> getServiceStaffMemberIds(
            @PathVariable String bookingBusinessId,
            @PathVariable String serviceId) {

            List<String> staffMemberIds = graphService.getStaffMemberIds(bookingBusinessId, serviceId);
            return ResponseEntity.ok(staffMemberIds);

    }



}
