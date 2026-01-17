package com.justlife.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cleaner summary")
public record CleanerDto(

        @Schema(example = "1")
        Long id,

        @Schema(example = "John Doe")
        String name
) {
}