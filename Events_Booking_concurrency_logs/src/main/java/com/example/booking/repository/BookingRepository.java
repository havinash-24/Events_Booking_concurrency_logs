package com.example.booking.repository;

import com.example.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    @Query(
            value = """
                    UPDATE bookings SET status = 'CANCELLED',
                    cancelled_at = now()
                    WHERE id = :bookingId
                    AND status = 'CONFIRMED'
                    RETURNING event_id
                    """,
            nativeQuery = true
    )
    Long cancelBookingNative(@Param("bookingId") Long bookingId);

    Optional<Booking> findByIdempotencyKey(String idempotencykey);

    Page<Booking> findByEventId(Long eventId, Pageable pageable);
}
