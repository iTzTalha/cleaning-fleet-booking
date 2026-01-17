package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Request payload for rescheduling a booking")
public record BookingRescheduleRequestDto (

        @Schema(
                description = "New booking date",
                example = "2026-01-21"
        )
        LocalDate date,

        @Schema(
                description = "New booking start time",
                example = "12:00"
        )
        LocalTime startTime
){}