package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Request payload for creating a booking")
public record BookingRequestDto(
        @Schema(
                description = "Booking date (ISO-8601)",
                example = "2026-01-20"
        )
        LocalDate date,

        @Schema(
                description = "Booking start time",
                example = "10:00"
        )
        LocalTime startTime,

        @Schema(
                description = "Duration in minutes",
                example = "120"
        )
        int durationMinutes,

        @Schema(
                description = "Number of cleaners required",
                example = "2",
                minimum = "1"
        )
        int cleanerCount
) {}