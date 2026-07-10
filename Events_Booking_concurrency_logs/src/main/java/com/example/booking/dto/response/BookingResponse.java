package com.example.booking.dto.response;

import com.example.booking.entity.BookingStatus;

public record BookingResponse (
        Long bookingId,
        Long eventId,
        Long userId,
        BookingStatus status
){
}
