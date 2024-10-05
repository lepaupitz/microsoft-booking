package com.example.Authenticator.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.example.Authenticator.entity.*;
import com.microsoft.graph.models.BookingAppointment;
import com.microsoft.graph.models.BookingCustomerInformation;
import com.microsoft.graph.models.BookingService;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.solutions.bookingbusinesses.item.getstaffavailability.GetStaffAvailabilityPostRequestBody;
import com.microsoft.graph.solutions.bookingbusinesses.item.getstaffavailability.GetStaffAvailabilityPostResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphService {

    @Autowired
    AuthProperties authProperties;

    private GraphServiceClient createGraphClient() {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(authProperties.getClientId())
                .clientSecret(authProperties.getClientSecret())
                .tenantId(authProperties.getTenantId())
                .build();

        String[] scopes = {"https://graph.microsoft.com/.default"};

        GraphServiceClient graphServiceClient = new GraphServiceClient(clientSecretCredential, scopes);
        return graphServiceClient;
    }

    public List<String> listStaffMemberIdServices(String businessId, String serviceId) {

        BookingService businessService = createGraphClient()
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .services()
                .byBookingServiceId(serviceId)
                .get();

        List<String> staffMemberIds = businessService.getStaffMemberIds();
        return staffMemberIds;
    }

    public List<StaffAvailabilityResponseDto> listStaffAvailability(
            String businessId,
            String serviceId,
            DateTimeWrapper startDateTime,
            DateTimeWrapper endDateTime) {

        GraphServiceClient graphClient = createGraphClient();

        List<String> staffIds = listStaffMemberIdServices(businessId, serviceId);

        GetStaffAvailabilityPostRequestBody getStaffAvailabilityPostRequestBody = getGetStaffAvailabilityPostRequestBody(startDateTime, endDateTime, staffIds);

        GetStaffAvailabilityPostResponse response = graphClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .getStaffAvailability()
                .post(getStaffAvailabilityPostRequestBody);

        List<StaffAvailabilityResponseDto> filteredResponse = response.getValue().stream()
                .map(staffAvailabilityItem -> {
                    StaffAvailabilityResponseDto dto = new StaffAvailabilityResponseDto();
                    dto.setStaffId(staffAvailabilityItem.getStaffId());

                    List<AvailabilityItem> availabilityItems = filterAvailabilityStaff(staffAvailabilityItem.getAvailabilityItems().stream()
                            .map(item -> {
                                com.example.Authenticator.entity.AvailabilityItem availabilityItem = new AvailabilityItem();
                                availabilityItem.setStatus(item.getStatus().toString());

                                DateTimeWrapper startDateTimeWrapper = new DateTimeWrapper();
                                if (item.getStartDateTime() != null) {
                                    startDateTimeWrapper.setDateTime(item.getStartDateTime().getDateTime());
                                    startDateTimeWrapper.setTimeZone(item.getStartDateTime().getTimeZone());
                                }
                                availabilityItem.setStartDateTime(startDateTimeWrapper);

                                DateTimeWrapper endDateTimeWrapper = new DateTimeWrapper();
                                if (item.getEndDateTime() != null) {
                                    endDateTimeWrapper.setDateTime(item.getEndDateTime().getDateTime());
                                    endDateTimeWrapper.setTimeZone(item.getEndDateTime().getTimeZone());
                                }
                                availabilityItem.setEndDateTime(endDateTimeWrapper);

                                return availabilityItem;
                            })
                            .collect(Collectors.toList()));

                    dto.setAvailabilityItems(availabilityItems);
                    return dto;
                })
                .collect(Collectors.toList());

        return filteredResponse;
    }

    @NotNull
    private static GetStaffAvailabilityPostRequestBody getGetStaffAvailabilityPostRequestBody(DateTimeWrapper startDateTime, DateTimeWrapper endDateTime, List<String> staffIds) {
        GetStaffAvailabilityPostRequestBody getStaffAvailabilityPostRequestBody =
                new GetStaffAvailabilityPostRequestBody();

        getStaffAvailabilityPostRequestBody.setStaffIds(staffIds);

        DateTimeTimeZone startDateTimeZone = new DateTimeTimeZone();
        startDateTimeZone.setDateTime(startDateTime.getDateTime());
        startDateTimeZone.setTimeZone(startDateTime.getTimeZone());
        getStaffAvailabilityPostRequestBody.setStartDateTime(startDateTimeZone);

        DateTimeTimeZone endDateTimeZone = new DateTimeTimeZone();
        endDateTimeZone.setDateTime(endDateTime.getDateTime());
        endDateTimeZone.setTimeZone(endDateTime.getTimeZone());
        getStaffAvailabilityPostRequestBody.setEndDateTime(endDateTimeZone);
        return getStaffAvailabilityPostRequestBody;
    }

    private List<com.example.Authenticator.entity.AvailabilityItem> filterAvailabilityStaff(List<com.example.Authenticator.entity.AvailabilityItem> availabilityItems) {
        return availabilityItems.stream()
                .filter(item -> "Available".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList());
    }

    public Duration getServiceDefaultDuration(String bookingBusinessId, String serviceId) {
        BookingService bookingService = createGraphClient()
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(bookingBusinessId)
                .services()
                .byBookingServiceId(serviceId)
                .get();

        return bookingService.getDefaultDuration().getDuration();
    }

    public List<TimeSlot> splitAvailabilityByServiceDuration(
            String bookingBusinessId,
            String serviceId,
            DateTimeWrapper startDateTime,
            DateTimeWrapper endDateTime) {

        Duration serviceDuration = getServiceDefaultDuration(bookingBusinessId, serviceId);

        List<StaffAvailabilityResponseDto> staffAvailabilityList = listStaffAvailability(
                bookingBusinessId, serviceId, startDateTime, endDateTime);

        List<TimeSlot> dividedTimeSlots = new ArrayList<>();

        for (StaffAvailabilityResponseDto staffAvailability : staffAvailabilityList) {
            String staffId = staffAvailability.getStaffId();
            for (AvailabilityItem availabilityItem : staffAvailability.getAvailabilityItems()) {
                if ("Available".equalsIgnoreCase(availabilityItem.getStatus())) {

                    LocalDateTime start = LocalDateTime.parse(availabilityItem.getStartDateTime().getDateTime(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime end = LocalDateTime.parse(availabilityItem.getEndDateTime().getDateTime(), DateTimeFormatter.ISO_DATE_TIME);

                    LocalDateTime slotStart = start;
                    while (slotStart.plus(serviceDuration).isBefore(end) || slotStart.plus(serviceDuration).isEqual(end)) {
                        LocalDateTime slotEnd = slotStart.plus(serviceDuration);


                        dividedTimeSlots.add(new TimeSlot(slotStart, slotEnd, staffId));

                        slotStart = slotEnd;
                    }
                }
            }
        }
        return dividedTimeSlots;
    }

    public BookingAppointment createAppointment(String businessId, AppointmentDto appointmentDto) {

        GraphServiceClient graphCliente = createGraphClient();

        BookingAppointment appointment = new BookingAppointment();

        DateTimeTimeZone startDateTimeZone = new DateTimeTimeZone();
        startDateTimeZone.setDateTime(appointmentDto.getStartDateTime().getDateTime());
        startDateTimeZone.setTimeZone(appointmentDto.getStartDateTime().getTimeZone());

        DateTimeTimeZone endDateTimeZone = new DateTimeTimeZone();
        endDateTimeZone.setDateTime(appointmentDto.getEndDateTime().getDateTime());
        endDateTimeZone.setTimeZone(appointmentDto.getEndDateTime().getTimeZone());

        appointment.setStartDateTime(startDateTimeZone);
        appointment.setEndDateTime(endDateTimeZone);

        appointment.setServiceId(appointmentDto.getServiceId());
        appointment.setStaffMemberIds(appointmentDto.getStaffMemberIds());

        List<BookingCustomerInformation> customerInfos = appointmentDto.getCustomers().stream()
                .map(customer -> {
                    BookingCustomerInformation bookingCustomer = new BookingCustomerInformation();
                    bookingCustomer.setCustomerId(customer.getCustomerId());
                    bookingCustomer.setName(customer.getName());
                    bookingCustomer.setEmailAddress(customer.getEmailAddress());
                    bookingCustomer.setPhone(customer.getPhone());
                    bookingCustomer.setTimeZone(customer.getTimeZone());
                    bookingCustomer.setOdataType(customer.getODataType());
                    return bookingCustomer;
                }).collect(Collectors.toList());

        appointment.setCustomers(new ArrayList<>(customerInfos));
        appointment.setIsLocationOnline(true);

        BookingAppointment createdAppointment = graphCliente
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .appointments()
                .post(appointment);

        return createdAppointment;
    }

    public void cancelAppointment(String businessId, String appointmentId) {
        GraphServiceClient graphClient = createGraphClient();

        com.microsoft.graph.solutions.bookingbusinesses.item.appointments.item.cancel.CancelPostRequestBody cancelPostRequestBody = new com.microsoft.graph.solutions.bookingbusinesses.item.appointments.item.cancel.CancelPostRequestBody();
        cancelPostRequestBody.setCancellationMessage("Your appointment has been successfully cancelled. Please call us again.");
        graphClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .appointments()
                .byBookingAppointmentId(appointmentId)
                .cancel()
                .post(cancelPostRequestBody);
    }

    public BookingAppointment updateAppointment(String businessId, String appointmentId, AppointmentDto appointmentDto) {
        GraphServiceClient graphClient = createGraphClient();

        BookingAppointment existingAppointment = graphClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .appointments()
                .byBookingAppointmentId(appointmentId)
                .get();

        DateTimeTimeZone startDateTimeZone = new DateTimeTimeZone();
        startDateTimeZone.setDateTime(appointmentDto.getStartDateTime().getDateTime());
        startDateTimeZone.setTimeZone(appointmentDto.getStartDateTime().getTimeZone());

        DateTimeTimeZone endDateTimeZone = new DateTimeTimeZone();
        endDateTimeZone.setDateTime(appointmentDto.getEndDateTime().getDateTime());
        endDateTimeZone.setTimeZone(appointmentDto.getEndDateTime().getTimeZone());

        existingAppointment.setStartDateTime(startDateTimeZone);
        existingAppointment.setEndDateTime(endDateTimeZone);
        existingAppointment.setStaffMemberIds(appointmentDto.getStaffMemberIds());

        BookingAppointment updatedAppointment = graphClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .appointments()
                .byBookingAppointmentId(appointmentId)
                .patch(existingAppointment);

        return updatedAppointment;
    }
}