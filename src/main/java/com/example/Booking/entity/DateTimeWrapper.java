package com.example.Booking.entity;

import com.microsoft.graph.models.DateTimeTimeZone;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Data
public class DateTimeWrapper extends DateTimeTimeZone {

    private String dateTime;
    private String timeZone;

}
