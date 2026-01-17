package com.justlife.booking.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record TimeSlot(LocalTime start, LocalTime end) {

    public List<TimeSlot> subtract(LocalTime blockStart, LocalTime blockEnd) {

        List<TimeSlot> result = new ArrayList<>();

        if (blockEnd.isBefore(start) || blockStart.isAfter(end)) {
            result.add(this);
            return result;
        }

        if (blockStart.isAfter(start)) {
            result.add(new TimeSlot(start, blockStart));
        }

        if (blockEnd.isBefore(end)) {
            result.add(new TimeSlot(blockEnd, end));
        }

        return result;
    }
}