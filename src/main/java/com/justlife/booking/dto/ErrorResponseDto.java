package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard error response")
public record ErrorResponseDto(

        @Schema(
                description = "Time when the error occurred",
                example = "2026-01-20T10:15:30Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,

        @Schema(
                description = "HTTP error name",
                example = "Bad Request"
        )
        String error,

        @Schema(
                description = "Human readable error message",
                example = "Booking must be in the future"
        )
        String message,

        @Schema(
                description = "Field-level validation errors (only for validation failures)",
                example = """
                {
                  "startTime": "must not be null",
                  "date": "must be a future date"
                }
                """
        )
        Map<String, String> fieldErrors
) {}