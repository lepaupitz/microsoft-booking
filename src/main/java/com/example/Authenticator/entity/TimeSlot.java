package com.example.Authenticator.entity;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TimeSlot {

    private String staffId;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot(LocalDateTime start, LocalDateTime end, String staffId) {
        this.start = start;
        this.end = end;
        this.staffId = staffId;
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

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }


    @Override
    public String toString() {
        return "TimeSlot{" +
                "staffId='" + staffId + '\'' +
                ", startDateTime=" + start +
                ", endDateTime=" + end +
                '}';
    }
}
