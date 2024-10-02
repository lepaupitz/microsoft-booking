package com.example.Authenticator;

import lombok.Data;

import java.util.List;

@Data
public class AppointmentDTO {

    private DateTimeWrapper startDateTime;
    private DateTimeWrapper endDateTime;
    private String serviceId;
    private List<String> staffMemberIds;
    private List<Customers> customers;
    private boolean isLocationOnline;

}