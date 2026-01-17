package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Booking response")
public record BookingResponseDto(
        @Schema(example = "1")
        Long bookingId,

        @Schema(example = "2026-01-20")
        LocalDate date,

        @Schema(example = "10:00")
        LocalTime startTime,

        @Schema(example = "12:00")
        LocalTime endTime,

        @Schema(example = "1")
        Long vehicleId,

        @Schema(example = "Vehicle-1")
        String vehicleName,

        @Schema(description = "Assigned cleaners")
        List<CleanerDto> cleaners
) {}