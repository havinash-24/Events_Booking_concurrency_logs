package com.example.booking.controller;

import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookEvent(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey){

        try {
            return bookingService.createBooking(idempotencyKey, request);
        } catch (DataIntegrityViolationException e) {
            // The original transaction has failed and rolled back (restoring the seat).
            // We now recover the existing booking in a fresh transaction.
            return bookingService.recoverExistingBooking(idempotencyKey);
        }
    }

    @PostMapping("/{bookingId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }

    @GetMapping("/events/{eventId}")
    public Page<BookingResponse> getBookingsForEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return bookingService.getBookingsForEvent(eventId, page, size);
    }
}
