package com.example.Authenticator.entity;

import lombok.Data;

@Data
public class StaffAvailabilityRequestDto {

    private String businessId;
    private String serviceId;
    private DateTimeWrapper startDateTime;
    private DateTimeWrapper endDateTime;

}
