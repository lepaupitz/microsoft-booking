package com.example.Booking.service;

import com.example.Booking.config.MicrosoftGraphClientConfig;
import com.example.Booking.entity.AvailabilityItem;
import com.example.Booking.entity.TimeSlot;
import com.example.Booking.entity.*;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.solutions.bookingbusinesses.item.getstaffavailability.GetStaffAvailabilityPostRequestBody;
import com.microsoft.graph.solutions.bookingbusinesses.item.getstaffavailability.GetStaffAvailabilityPostResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class MicrosoftGraphService {

    private final MicrosoftGraphClientConfig graphClientConfig;

    public GraphServiceClient createGraphClient() {
        return graphClientConfig.graphClient();
    }

    public List<String> listStaffMemberIdServices(String businessId, String serviceId) {

        BookingService businessService = createGraphClient()
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .services()
                .byBookingServiceId(serviceId)
                .get();

        return businessService.getStaffMemberIds();
    }

    public List<StaffAvailabilityResponseDto> listStaffAvailabilityAvailable(
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

        return getStaffAvailabilityAvailable(response);
    }

    public List<StaffAvailabilityResponseDto> getStaffAvailabilityAvailable(GetStaffAvailabilityPostResponse response) {
        List<StaffAvailabilityResponseDto> filteredResponse = response.getValue().stream()
                .map(staffAvailabilityItem -> {
                    StaffAvailabilityResponseDto dto = new StaffAvailabilityResponseDto();
                    dto.setStaffId(staffAvailabilityItem.getStaffId());

                    List<AvailabilityItem> availabilityItems = filterAvailabilityStaffAvailable(staffAvailabilityItem.getAvailabilityItems().stream()
                            .map(MicrosoftGraphService::apply)
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

    private List<com.example.Booking.entity.AvailabilityItem> filterAvailabilityStaffAvailable(List<com.example.Booking.entity.AvailabilityItem> availabilityItems) {
        return availabilityItems.stream()
                .filter(item -> "Available".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList());
    }

    public Duration getTimeSlotInterval(String bookingBusinessId, String serviceId) {
        BookingService bookingService = createGraphClient()
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(bookingBusinessId)
                .services()
                .byBookingServiceId(serviceId)
                .get();

        return bookingService.getSchedulingPolicy().getTimeSlotInterval().getDuration();
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

    public List<TimeSlot> removeDuplicateTimeSlots(List<TimeSlot> timeSlots) {
        Set<TimeSlot> uniqueTimeSlots = new HashSet<>(timeSlots);
        return new ArrayList<>(uniqueTimeSlots);
    }

    public List<TimeSlot> splitAvailabilityByServiceDuration(
            String bookingBusinessId,
            String serviceId,
            DateTimeWrapper startDateTime,
            DateTimeWrapper endDateTime) {

        Duration serviceDuration = getTimeSlotInterval(bookingBusinessId, serviceId);

        List<StaffAvailabilityResponseDto> staffAvailabilityList = listStaffAvailabilityAvailable(
                bookingBusinessId, serviceId, startDateTime, endDateTime);

        List<TimeSlot> dividedTimeSlots = new ArrayList<>();

        for (StaffAvailabilityResponseDto staffAvailability : staffAvailabilityList) {
            for (AvailabilityItem availabilityItem : staffAvailability.getAvailabilityItems()) {
                if ("Available".equalsIgnoreCase(availabilityItem.getStatus())) {

                    LocalDateTime start = LocalDateTime.parse(availabilityItem.getStartDateTime().getDateTime(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime end = LocalDateTime.parse(availabilityItem.getEndDateTime().getDateTime(), DateTimeFormatter.ISO_DATE_TIME);

                    LocalDateTime slotStart = start;
                    while (slotStart.plus(serviceDuration).isBefore(end) || slotStart.plus(serviceDuration).isEqual(end)) {
                        LocalDateTime slotEnd = slotStart.plus(serviceDuration);


                        dividedTimeSlots.add(new TimeSlot(slotStart, slotEnd));

                        slotStart = slotEnd;
                    }
                }
            }
        }
        return removeDuplicateTimeSlots(dividedTimeSlots);
    }

    public BookingAppointment createAppointment(String businessId, AppointmentDto appointmentDto) {
        GraphServiceClient graphClient = createGraphClient();

        BookingAppointment appointment = new BookingAppointment();

        DateTimeTimeZone startDateTimeZone = new DateTimeTimeZone();
        startDateTimeZone.setDateTime(appointmentDto.getStartDateTime().getDateTime());
        startDateTimeZone.setTimeZone(appointmentDto.getStartDateTime().getTimeZone());

        DateTimeTimeZone endDateTimeZone = new DateTimeTimeZone();
        LocalDateTime startDateTime = LocalDateTime.parse(appointmentDto.getStartDateTime().getDateTime());
        Duration duration = getServiceDefaultDuration(businessId, appointmentDto.getServiceId());
        LocalDateTime endDateTime = startDateTime.plus(duration);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");;
        String endDateTimeString = endDateTime.format(formatter);

        endDateTimeZone.setDateTime(endDateTimeString);
        endDateTimeZone.setTimeZone(appointmentDto.getStartDateTime().getTimeZone());

        DateTimeWrapper endDateTimeWrapper = new DateTimeWrapper();
        endDateTimeWrapper.setDateTime(endDateTimeString);
        endDateTimeWrapper.setTimeZone(appointmentDto.getStartDateTime().getTimeZone());

        DateTimeWrapper fixedStartDateTimeWrapper = fixTimeStartDate(appointmentDto.getStartDateTime());
        DateTimeWrapper fixedendDateTimeWrapper = fixTimeEndDate(endDateTimeWrapper);



        appointment.setStartDateTime(startDateTimeZone);
        appointment.setEndDateTime(endDateTimeZone);
        appointment.setServiceId(appointmentDto.getServiceId());

        List<String> availableStaffId = findAvailableStaffForBooking(
                businessId,
                appointmentDto.getServiceId(),
                fixedStartDateTimeWrapper,
                fixedendDateTimeWrapper);

        appointment.setStaffMemberIds(availableStaffId);

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
                }).toList();

        appointment.setCustomers(new ArrayList<>(customerInfos));
        appointment.setIsLocationOnline(true);

        return graphClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .appointments()
                .post(appointment);

    }

    public void cancelAppointment(String businessId, String appointmentId) {
        GraphServiceClient graphClient = createGraphClient();

        com.microsoft.graph.solutions.bookingbusinesses.item.appointments.item.cancel.CancelPostRequestBody cancelPostRequestBody = new com.microsoft.graph.solutions.bookingbusinesses.item.appointments.item.cancel.CancelPostRequestBody();
        cancelPostRequestBody.setCancellationMessage("Your appointment has been successfully cancelled.");
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
        existingAppointment.setIsLocationOnline(true);

        return graphClient
                .solutions()
                .bookingBusinesses()
                .byBookingBusinessId(businessId)
                .appointments()
                .byBookingAppointmentId(appointmentId)
                .patch(existingAppointment);
    }

    public List<StaffAvailabilityResponseDto> listStaffAvailabilityBusy(
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

        return getStaffAvailabilityBusy(Objects.requireNonNull(response));
    }

    private List<StaffAvailabilityResponseDto> getStaffAvailabilityBusy(GetStaffAvailabilityPostResponse response) {
        return Objects.requireNonNull(response.getValue()).stream()
                .map(staffAvailabilityItem -> {
                    StaffAvailabilityResponseDto dto = new StaffAvailabilityResponseDto();
                    dto.setStaffId(staffAvailabilityItem.getStaffId());

                    List<AvailabilityItem> availabilityItems = filterAvailabilityStaffBusy(Objects.requireNonNull(staffAvailabilityItem.getAvailabilityItems()).stream()
                            .map(MicrosoftGraphService::apply)
                            .collect(Collectors.toList()));

                    dto.setAvailabilityItems(availabilityItems);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<com.example.Booking.entity.AvailabilityItem> filterAvailabilityStaffBusy(List<com.example.Booking.entity.AvailabilityItem> availabilityItems) {
        return availabilityItems.stream()
                .filter(item -> "Busy".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList());
    }

    private List<Map.Entry<String, Long>> listStaffByBusyCount(List<StaffAvailabilityResponseDto> staffAvailabilityList) {

        return staffAvailabilityList.stream()
                .collect(Collectors.toMap(
                        StaffAvailabilityResponseDto::getStaffId,
                        dto -> dto.getAvailabilityItems().stream()
                                .filter(item -> "Busy".equalsIgnoreCase(item.getStatus()))
                                .count()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
    }

    public List<String> listStaffMemberIdServicesByAvailability(String businessId, String serviceId, DateTimeWrapper startDateTime, DateTimeWrapper endDateTime) {
        List<StaffAvailabilityResponseDto> staffAvailabilityList = listStaffAvailabilityBusy(businessId, serviceId, startDateTime, endDateTime);
        List<Map.Entry<String, Long>> staffByBusyCount = listStaffByBusyCount(staffAvailabilityList);
        return staffByBusyCount.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> findAvailableStaffForBooking(
            String businessId,
            String serviceId,
            DateTimeWrapper startDateTime,
            DateTimeWrapper endDateTime) {

        List<String> staffIds = listStaffMemberIdServicesByAvailability(businessId, serviceId, startDateTime, endDateTime);

        for (String staffId : staffIds) {
            List<StaffAvailabilityResponseDto> availableStaff = listStaffAvailabilityAvailable(
                    businessId,
                    serviceId,
                    startDateTime,
                    endDateTime);

            boolean isAvailable = availableStaff.stream()
                    .anyMatch(dto -> dto.getStaffId().equals(staffId)
                            && !dto.getAvailabilityItems().isEmpty());

            if (isAvailable) {
                return Collections.singletonList(staffId);
            }
        }

        return null;
    }

    private static AvailabilityItem apply(com.microsoft.graph.models.AvailabilityItem item) {
        AvailabilityItem availabilityItem = new AvailabilityItem();
        availabilityItem.setStatus(Objects.requireNonNull(item.getStatus()).toString());

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
    }

    public static DateTimeWrapper fixTimeStartDate(DateTimeWrapper dateTimeWrapper) {
        LocalDateTime originalDateTime = LocalDateTime.parse(dateTimeWrapper.getDateTime(), DateTimeFormatter.ISO_DATE_TIME);

        LocalDateTime fixedDateTime = originalDateTime.withHour(1).withMinute(0).withSecond(0).withNano(0);

        DateTimeWrapper fixedStartDateTimeWrapper = new DateTimeWrapper();
        fixedStartDateTimeWrapper.setDateTime(fixedDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        fixedStartDateTimeWrapper.setTimeZone(dateTimeWrapper.getTimeZone());

        return fixedStartDateTimeWrapper;
    }

    public static DateTimeWrapper fixTimeEndDate(DateTimeWrapper dateTimeWrapper) {
        LocalDateTime originalDateTime = LocalDateTime.parse(dateTimeWrapper.getDateTime(), DateTimeFormatter.ISO_DATE_TIME);

        LocalDateTime fixedDateTime = originalDateTime.withHour(23).withMinute(0).withSecond(0).withNano(0);

        DateTimeWrapper fixedEndDateTimeWrapper = new DateTimeWrapper();
        fixedEndDateTimeWrapper.setDateTime(fixedDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        fixedEndDateTimeWrapper.setTimeZone(dateTimeWrapper.getTimeZone());

        return fixedEndDateTimeWrapper;
    }
}