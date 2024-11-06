package com.example.Booking.controller;

import com.azure.core.annotation.Get;
import com.azure.core.exception.ResourceNotFoundException;
import com.example.Booking.service.MicrosoftGraphService;
import com.example.Booking.entity.*;
import com.microsoft.graph.models.BookingAppointment;
import com.microsoft.kiota.PeriodAndDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/graph")
public class MicrosoftGraphController {

    @Autowired
    MicrosoftGraphService microsoftGraphService;

    @GetMapping("/serviceStaff/{bookingBusinessId}/{serviceId}")
    public ResponseEntity<List<String>> getServiceStaffMemberIds(
            @PathVariable String bookingBusinessId,
            @PathVariable String serviceId) {

        try {
            List<String> staffMemberIds = microsoftGraphService.listStaffMemberIdServices(bookingBusinessId, serviceId);
            return ResponseEntity.ok(staffMemberIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @PostMapping("/avaiability/{businessId}/{serviceId}")
    public ResponseEntity<List<StaffAvailabilityResponseDto>> listStaffAvailability(
            @PathVariable String businessId,
            @PathVariable String serviceId,
            @RequestBody StaffAvailabilityRequestDto staffAvailabilityRequestDTO) {

        try {
            List<StaffAvailabilityResponseDto> availability = microsoftGraphService.listStaffAvailabilityAvailable(
                    businessId,
                    serviceId,
                    staffAvailabilityRequestDTO.getStartDateTime(),
                    staffAvailabilityRequestDTO.getEndDateTime()
            );
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{bookingBusinessId}/services/{serviceId}/default-duration")
    public ResponseEntity<Duration> getServiceDefaultDuration(
            @PathVariable String bookingBusinessId,
            @PathVariable String serviceId) {

        try {
            Duration defaultDuration = PeriodAndDuration.ofDuration(microsoftGraphService.getServiceDefaultDuration(bookingBusinessId, serviceId)).getDuration();
            return ResponseEntity.ok(defaultDuration);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/{bookingBusinessId}/services/{serviceId}/split-availability")
    public ResponseEntity<List<TimeSlot>> getSplitAvailability(
            @PathVariable String bookingBusinessId,
            @PathVariable String serviceId,
            @RequestBody StaffAvailabilityRequestDto request) {

        try {
            DateTimeWrapper startWrapper = request.getStartDateTime();
            DateTimeWrapper endWrapper = request.getEndDateTime();

            List<TimeSlot> timeSlots = microsoftGraphService.splitAvailabilityByServiceDuration(
                    bookingBusinessId,
                    serviceId,
                    startWrapper,
                    endWrapper);

            return ResponseEntity.ok(timeSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/appointments/{businessId}")
    public ResponseEntity<BookingAppointment> createAppointment(
            @PathVariable String businessId,
            @RequestBody AppointmentDto appointmentDto) {
        try {
            BookingAppointment createdAppointment = microsoftGraphService.createAppointment(businessId, appointmentDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date and hour.", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create appointment", e);
        }
    }

    @PostMapping("/{businessId}/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable String businessId,
            @PathVariable String appointmentId) {

        try {
            microsoftGraphService.cancelAppointment(businessId, appointmentId);
            return ResponseEntity.noContent().build();
                } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters provided.", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error canceling the appointment", e);
        }
    }

    @PatchMapping("/{businessId}/{appointmentId}/reschedule")
    public ResponseEntity<BookingAppointment> rescheduleAppointment(
            @PathVariable String businessId,
            @PathVariable String appointmentId,
            @RequestBody AppointmentDto appointmentDto) {

        try {
            BookingAppointment rescheduledAppointment = microsoftGraphService.updateAppointment(businessId, appointmentId, appointmentDto);
            return ResponseEntity.ok(rescheduledAppointment);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters provided.", e);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error rescheduling the appointment.", e);
        }

    }

    @PostMapping("/busy/{businessId}/{serviceId}")
    public List<String> listStaffAvailabilityBusy(
            @PathVariable String businessId,
            @PathVariable String serviceId,
            @RequestBody StaffAvailabilityRequestDto staffAvailabilityRequestDTO) {

        try {
            return microsoftGraphService.listStaffMemberIdServicesByAvailability(
                    businessId,
                    serviceId,
                    staffAvailabilityRequestDTO.getStartDateTime(),
                    staffAvailabilityRequestDTO.getEndDateTime()
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters provided.", e);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff availability list not found.", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error obtaining staff availability.", e);
        }
    }


}
