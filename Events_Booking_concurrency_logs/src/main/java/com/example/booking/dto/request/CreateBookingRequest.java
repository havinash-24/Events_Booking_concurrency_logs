package com.example.booking.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest (
        @NotNull(message = "Event ID is required")
        Long eventId,
        @NotNull(message = "User ID is required")
        Long userId
){
}
