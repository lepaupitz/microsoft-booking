package com.example.Authenticator.entity;


import lombok.Data;

@Data
public class AvailabilityItem {

    private String status;
    private DateTimeWrapper startDateTime;
    private DateTimeWrapper endDateTime;


}
