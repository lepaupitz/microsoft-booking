package com.example.Booking.entity;

import lombok.Data;

import java.util.List;

@Data
public class AppointmentDto {

    private DateTimeWrapper startDateTime;
    private DateTimeWrapper endDateTime;
    private String serviceId;
    private List<String> staffMemberIds;
    private List<Customers> customers;
    private boolean isLocationOnline;

}