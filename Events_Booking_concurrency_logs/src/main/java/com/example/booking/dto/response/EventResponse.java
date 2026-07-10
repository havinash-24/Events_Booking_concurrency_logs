package com.example.booking.dto.response;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        Integer totalSeats,
        Integer availableSeats,
        LocalDateTime createdAt
){
}
