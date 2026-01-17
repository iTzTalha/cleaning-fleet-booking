package com.justlife.booking.controller;

import com.justlife.booking.dto.BookingRequestDto;
import com.justlife.booking.dto.BookingRescheduleRequestDto;
import com.justlife.booking.dto.BookingResponseDto;
import com.justlife.booking.dto.ErrorResponseDto;
import com.justlife.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management APIs")
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Create a booking",
            description = "Creates a new booking by automatically allocating available cleaners"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Booking created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid booking request",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict (booking overlaps or cannot be scheduled)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto createBooking(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Booking creation request",
                    required = true
            )
            BookingRequestDto request
    ) {
        return bookingService.createBooking(request);
    }

    @Operation(
            summary = "Cancel a booking",
            description = "Cancels a booking if it has not started yet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Booking already started",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> cancelBooking(
            @Parameter(
                    description = "Booking ID",
                    example = "100",
                    required = true
            )
            @PathVariable Long bookingId
    ) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reschedule a booking",
            description = "Reschedules an existing booking to a new date and time"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking rescheduled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Booking cannot be rescheduled",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PutMapping("/{bookingId}/reschedule")
    public BookingResponseDto rescheduleBooking(
            @Parameter(
                    description = "Booking ID",
                    example = "100",
                    required = true
            )
            @PathVariable Long bookingId,

            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reschedule request",
                    required = true
            )
            BookingRescheduleRequestDto request
    ) {
        return bookingService.rescheduleBooking(bookingId, request);
    }
}