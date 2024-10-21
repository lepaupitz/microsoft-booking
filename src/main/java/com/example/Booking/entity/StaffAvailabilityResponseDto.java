package com.example.Booking.entity;

import lombok.Data;

import java.util.List;

@Data
public class StaffAvailabilityResponseDto {

    private String staffId;
    private List<AvailabilityItem> availabilityItems;

}
