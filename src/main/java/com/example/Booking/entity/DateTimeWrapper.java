package com.example.Booking.entity;

import com.microsoft.graph.models.DateTimeTimeZone;
import lombok.Data;

@Data
public class DateTimeWrapper extends DateTimeTimeZone {

    private String dateTime;
    private String timeZone;

}