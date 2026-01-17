package com.justlife.booking.controller;

import com.justlife.booking.dto.ErrorResponseDto;
import com.justlife.booking.dto.VehicleAvailabilityDto;
import com.justlife.booking.dto.VehicleDailyAvailabilityDto;
import com.justlife.booking.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("availability")
@RequiredArgsConstructor
@Tag(
        name = "Availability",
        description = "APIs for querying cleaner availability grouped by vehicles"
)
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Operation(
            summary = "Get daily availability",
            description = """
                    Returns cleaner availability for the entire working day,
                    grouped by vehicle, including free time slots for each cleaner.
                    
                    Only future or current working days are allowed.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date or non-working day",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/date")
    public List<VehicleDailyAvailabilityDto> getDailyAvailabilityByVehicle(
            @Parameter(
                    description = "Date for which availability is requested",
                    example = "2026-01-20",
                    required = true
            )
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return availabilityService.getDailyAvailabilityByVehicle(date);
    }

    @Operation(
            summary = "Get availability for a specific time window",
            description = """
                    Returns available cleaners grouped by vehicle for a specific
                    date, start time, and duration.
                    
                    Cleaners with conflicting bookings (including buffer time)
                    are excluded.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (past time, non-working day, or outside working hours)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/cleaners")
    public List<VehicleAvailabilityDto> getAvailabilityByVehicle(
            @Parameter(
                    description = "Date of the requested booking",
                    example = "2026-01-20",
                    required = true
            )
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @Parameter(
                    description = "Requested booking start time",
                    example = "10:00",
                    required = true
            )
            @RequestParam("startTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @Parameter(
                    description = "Booking duration in minutes",
                    example = "120",
                    required = true
            )
            @RequestParam("durationMinutes")
            int durationMinutes
    ) {
        return availabilityService.getAvailabilityByVehicle(
                date,
                startTime,
                durationMinutes
        );
    }
}