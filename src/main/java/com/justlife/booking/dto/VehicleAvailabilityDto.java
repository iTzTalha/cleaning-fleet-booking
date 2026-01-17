package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Available cleaners for a specific time window grouped by vehicle")
public record VehicleAvailabilityDto (

        @Schema(example = "1")
        Long vehicleId,

        @Schema(example = "Vehicle-1")
        String vehicleName,

        @Schema(description = "Cleaners available for the requested time window")
        List<CleanerDto> cleaners
){}