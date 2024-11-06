package com.example.Booking.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;


@Data
public class TimeSlot {

    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;

    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }


    @Override
    public String toString() {
        return "TimeSlot{" +
                ", startDateTime=" + start +
                ", endDateTime=" + end +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(start, timeSlot.start) && Objects.equals(end, timeSlot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
