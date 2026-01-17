package com.justlife.booking.dto;

import com.justlife.booking.model.TimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Daily availability of a cleaner")
public record CleanerDailyAvailabilityDto(

        @Schema(example = "1")
        Long cleanerId,

        @Schema(example = "John Doe")
        String cleanerName,

        @Schema(description = "List of available time slots")
        List<TimeSlot> timeSlots
) {}