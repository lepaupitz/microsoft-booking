package com.example.Booking.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TimeSlotTest {

    @Test
    void timeSlotEqualityWithSameStartAndEnd() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot1 = new TimeSlot(start, end);
        TimeSlot slot2 = new TimeSlot(start, end);

        assertEquals(slot1, slot2);
    }

    @Test
    void timeSlotInequalityWithDifferentStart() {
        LocalDateTime start1 = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2023, 10, 1, 9, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot1 = new TimeSlot(start1, end);
        TimeSlot slot2 = new TimeSlot(start2, end);

        assertNotEquals(slot1, slot2);
    }

    @Test
    void timeSlotInequalityWithDifferentEnd() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end1 = LocalDateTime.of(2023, 10, 1, 11, 0);
        LocalDateTime end2 = LocalDateTime.of(2023, 10, 1, 12, 0);
        TimeSlot slot1 = new TimeSlot(start, end1);
        TimeSlot slot2 = new TimeSlot(start, end2);

        assertNotEquals(slot1, slot2);
    }

    @Test
    void timeSlotToStringFormat() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot = new TimeSlot(start, end);

        assertEquals("TimeSlot{, startDateTime=2023-10-01T10:00, endDateTime=2023-10-01T11:00}", slot.toString());
    }

    @Test
    void timeSlotHashCodeConsistency() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot = new TimeSlot(start, end);

        int initialHashCode = slot.hashCode();
        assertEquals(initialHashCode, slot.hashCode());
    }

    @Test
    void getStartReturnsCorrectStartDateTime() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot = new TimeSlot(start, end);

        assertEquals(start, slot.getStart());
    }

    @Test
    void setStartUpdatesStartDateTime() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime newStart = LocalDateTime.of(2023, 10, 1, 9, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot = new TimeSlot(start, end);
        slot.setStart(newStart);

        assertEquals(newStart, slot.getStart());
    }

    @Test
    void getEndReturnsCorrectEndDateTime() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        TimeSlot slot = new TimeSlot(start, end);

        assertEquals(end, slot.getEnd());
    }

    @Test
    void setEndUpdatesEndDateTime() {
        LocalDateTime start = LocalDateTime.of(2023, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 1, 11, 0);
        LocalDateTime newEnd = LocalDateTime.of(2023, 10, 1, 12, 0);
        TimeSlot slot = new TimeSlot(start, end);
        slot.setEnd(newEnd);

        assertEquals(newEnd, slot.getEnd());
    }

}
