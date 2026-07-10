//package com.example.booking.service;
//
//import com.example.booking.dto.request.CreateBookingRequest;
//import com.example.booking.dto.response.BookingResponse;
//import com.example.booking.entity.Booking;
//import com.example.booking.entity.BookingStatus;
//import com.example.booking.entity.Event;
//import com.example.booking.exception.BookingNotCancellableException;
//import com.example.booking.exception.ResourceNotFoundException;
//import com.example.booking.exception.SoldOutException;
//import com.example.booking.repository.BookingRepository;
//import com.example.booking.repository.EventRepository;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.Optional;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//@Service
//public class BookingService {
//    private final BookingRepository bookingRepository;
//    private final EventRepository eventRepository;
//
//    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository){
//        this.bookingRepository = bookingRepository;
//        this.eventRepository = eventRepository;
//    }
//
//    @Transactional
//    public BookingResponse createBooking(String idempotencyKey, CreateBookingRequest request){
//
//        if(idempotencyKey != null){
//            Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(idempotencyKey);
//            if(existingBooking.isPresent()) {
//                Booking b = existingBooking.get();
//                return new BookingResponse(
//                        b.getId(),
//                        b.getEvent().getId(),
//                        b.getUserId(),
//                        b.getStatus()
//                );
//            }
//        }
//
//        Integer remainingSeats = eventRepository.reserveSeatNative(request.eventId());
//
//        if(remainingSeats == null){
//            throw new SoldOutException("Event is sold out or does not exist!");
//        }
//
//        Event event = eventRepository.findById(request.eventId())
//                .orElseThrow(()-> new ResourceNotFoundException("Event not found"));
//
//        Booking booking = new Booking();
//        booking.setEvent(event);
//        booking.setUserId(request.userId());
//        booking.setStatus(BookingStatus.CONFIRMED);
//
//        if (idempotencyKey != null) {
//            booking.setIdempotencyKey(idempotencyKey);
//        }
//
//        // We let the DataIntegrityViolationException bubble up to the controller.
//        // Spring will automatically roll back the transaction, restoring the seat.
//        Booking savedBooking = bookingRepository.save(booking);
//
//        return new BookingResponse(
//                savedBooking.getId(),
//                event.getId(),
//                savedBooking.getUserId(),
//                savedBooking.getStatus()
//        );
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public BookingResponse recoverExistingBooking(String idempotencyKey) {
//        Booking b = bookingRepository.findByIdempotencyKey(idempotencyKey)
//                .orElseThrow(() -> new IllegalStateException("Duplicate key race but no booking found"));
//
//        return new BookingResponse(
//                b.getId(),
//                b.getEvent().getId(),
//                b.getUserId(),
//                b.getStatus()
//        );
//    }
//
//    @Transactional
//    public void cancelBooking(Long bookingId){
//        Long eventId = bookingRepository.cancelBookingNative(bookingId);
//
//        if(eventId == null){
//            throw new BookingNotCancellableException("Booking cannot be cancelled. Event does not exist or is already cancelled.");
//        }
//        eventRepository.refundSeatNative(eventId);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<BookingResponse> getBookingsForEvent(Long eventId,int page,int size){
//        PageRequest pageRequest = PageRequest.of(page,size, Sort.by("createdAt").descending());
//        Page<Booking> bookingPage = bookingRepository.findByEventId(eventId,pageRequest);
//
//        return bookingPage.map(booking -> new BookingResponse(
//                booking.getId(),
//                booking.getEvent().getId(),
//                booking.getUserId(),
//                booking.getStatus()
//        ));
//    }
//}

package com.example.booking.service;

import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Event;
import com.example.booking.exception.BookingNotCancellableException;
import com.example.booking.exception.DuplicateBookingException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.exception.SoldOutException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.EventRepository;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository){
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    // Helper method to extract the exact Postgres constraint name
    private String extractConstraintName(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof PSQLException psqlEx && psqlEx.getServerErrorMessage() != null) {
            return psqlEx.getServerErrorMessage().getConstraint();
        }
        return null;
    }
    @Transactional
    public BookingResponse createBooking(String idempotencyKey, CreateBookingRequest request){

        if(idempotencyKey != null){
            Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(idempotencyKey);
            if(existingBooking.isPresent()) {
                Booking b = existingBooking.get();
                return new BookingResponse(
                        b.getId(),
                        b.getEvent().getId(),
                        b.getUserId(),
                        b.getStatus()
                );
            }
        }

        Integer remainingSeats = eventRepository.reserveSeatNative(request.eventId());

        if(remainingSeats == null){
            throw new SoldOutException("Event is sold out or does not exist!");
        }

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(()-> new ResourceNotFoundException("Event not found"));

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUserId(request.userId());
        booking.setStatus(BookingStatus.CONFIRMED);

        if (idempotencyKey != null) {
            booking.setIdempotencyKey(idempotencyKey);
        }

        try {
            Booking savedBooking = bookingRepository.save(booking);
            return new BookingResponse(
                    savedBooking.getId(),
                    event.getId(),
                    savedBooking.getUserId(),
                    savedBooking.getStatus()
            );
        } catch (DataIntegrityViolationException e) {
            String constraint = extractConstraintName(e);

            // Branch 1: User is trying to double-book. We check both potential constraint names just to be safe.
            if ("idx_unique_active_booking".equals(constraint) || "idx_unique_confirmed_booking".equals(constraint)) {
                throw new DuplicateBookingException("You already have an active booking for this event.");
            }

            // Safety Guard: If a unique constraint fired but there is no idempotency key,
            // it HAS to be a double-booking business violation.
            if (idempotencyKey == null) {
                throw new DuplicateBookingException("You already have an active booking for this event.");
            }

            // Branch 2: Genuine idempotency retry race condition
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingResponse recoverExistingBooking(String idempotencyKey) {
        // Ultimate Safety Guard: Never query the database for a null unique key
        if (idempotencyKey == null) {
            throw new IllegalStateException("Cannot recover a booking without an idempotency key");
        }

        Booking b = bookingRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Duplicate key race but no booking found"));

        return new BookingResponse(
                b.getId(),
                b.getEvent().getId(),
                b.getUserId(),
                b.getStatus()
        );
    }

    @Transactional
    public void cancelBooking(Long bookingId){
        Long eventId = bookingRepository.cancelBookingNative(bookingId);

        if(eventId == null){
            throw new BookingNotCancellableException("Booking cannot be cancelled. Event does not exist or is already cancelled.");
        }
        eventRepository.refundSeatNative(eventId);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsForEvent(Long eventId,int page,int size){
        PageRequest pageRequest = PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Booking> bookingPage = bookingRepository.findByEventId(eventId,pageRequest);

        return bookingPage.map(booking -> new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getUserId(),
                booking.getStatus()
        ));
    }
}
