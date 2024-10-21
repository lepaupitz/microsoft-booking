package com.example.Authenticator.controller;

import com.example.Authenticator.service.GraphService;
import com.example.Authenticator.entity.*;
import com.microsoft.graph.models.BookingAppointment;
import com.microsoft.kiota.PeriodAndDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/graph")
public class GraphController {

    @Autowired
    GraphService graphService;


    @GetMapping("/serviceStaff/{bookingBusinessId}/{serviceId}")
    public ResponseEntity<List<String>> getServiceStaffMemberIds(
            @PathVariable String bookingBusinessId,
            @PathVariable String serviceId) {

            List<String> staffMemberIds = graphService.listStaffMemberIdServices(bookingBusinessId, serviceId);
            return ResponseEntity.ok(staffMemberIds);

    }

    @PostMapping("/avaiability/{businessId}/{serviceId}")
    public List<StaffAvailabilityResponseDto> listStaffAvailability(
            @PathVariable String businessId,
            @PathVariable String serviceId,
            @RequestBody StaffAvailabilityRequestDto staffAvailabilityRequestDTO) {

        return graphService.listStaffAvailabilityAvailable(
                businessId,
                serviceId,
                staffAvailabilityRequestDTO.getStartDateTime(),
                staffAvailabilityRequestDTO.getEndDateTime()
        );

    }

    @GetMapping("/{bookingBusinessId}/services/{serviceId}/default-duration")
    public ResponseEntity<Duration> getServiceDefaultDuration(
            @PathVariable String bookingBusinessId,
            @PathVariable String serviceId) {

        try {
            Duration defaultDuration = PeriodAndDuration.ofDuration(graphService.getServiceDefaultDuration(bookingBusinessId, serviceId)).getDuration();
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

            DateTimeWrapper startWrapper = request.getStartDateTime();
            DateTimeWrapper endWrapper = request.getEndDateTime();

            List<TimeSlot> timeSlots = graphService.splitAvailabilityByServiceDuration(
                    bookingBusinessId,
                    serviceId,
                    startWrapper,
                    endWrapper);

            return ResponseEntity.ok(timeSlots);

    }

    @PostMapping("/appointments/{businessId}")
    public ResponseEntity<BookingAppointment> createAppointment(
            @PathVariable String businessId,
            @RequestBody AppointmentDto appointmentDto) {

            BookingAppointment createdAppointment = graphService.createAppointment(businessId, appointmentDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);

    }

    @PostMapping("/{businessId}/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable String businessId,
            @PathVariable String appointmentId) {

            graphService.cancelAppointment(businessId, appointmentId);
            return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{businessId}/{appointmentId}/reschedule")
    public ResponseEntity<BookingAppointment> rescheduleAppointment(
            @PathVariable String businessId,
            @PathVariable String appointmentId,
            @RequestBody AppointmentDto appointmentDto) {

            BookingAppointment rescheduledAppointment = graphService.updateAppointment(businessId, appointmentId, appointmentDto);
            return ResponseEntity.ok(rescheduledAppointment);

    }

    @PostMapping("/busy/{businessId}/{serviceId}")
    public List<String> listStaffAvailabilityBusy(
            @PathVariable String businessId,
            @PathVariable String serviceId,
            @RequestBody StaffAvailabilityRequestDto staffAvailabilityRequestDTO) {

        return graphService.listStaffMemberIdServicesByAvailability(
                businessId,
                serviceId,
                staffAvailabilityRequestDTO.getStartDateTime(),
                staffAvailabilityRequestDTO.getEndDateTime()
        );

    }



}
