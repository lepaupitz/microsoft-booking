package com.example.Authenticator.entity;

import lombok.Data;

import java.util.List;

@Data
public class StaffAvailabilityResponseDto {

    private String staffId;
    private List<AvailabilityItem> availabilityItems;

}
