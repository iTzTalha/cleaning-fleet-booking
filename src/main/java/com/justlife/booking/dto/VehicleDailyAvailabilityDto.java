package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Daily availability grouped by vehicle")
public record VehicleDailyAvailabilityDto(

        @Schema(example = "1")
        Long vehicleId,

        @Schema(example = "Vehicle-1")
        String vehicleName,

        @Schema(description = "Cleaners and their daily availability")
        List<CleanerDailyAvailabilityDto> cleaners
) {}