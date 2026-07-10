package com.example.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateEventRequest (
    @NotBlank(message = "Event name is required")
    String name,

    @Positive(message = "Total seats must be greater than zero")
    Integer totalSeats
){
}
