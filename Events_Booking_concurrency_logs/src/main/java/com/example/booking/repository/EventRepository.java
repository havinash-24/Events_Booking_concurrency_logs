package com.example.booking.repository;

import com.example.booking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event,Long> {

    @Query(
            value = """
                    UPDATE events SET available_seats = available_seats - 1 WHERE id = :eventId AND available_seats > 0
                    RETURNING available_seats""",
            nativeQuery = true
    )
    Integer reserveSeatNative(@Param("eventId") Long eventId);

    @Query(
            value = """
                    UPDATE events SET available_seats = available_seats + 1 WHERE id = :eventId RETURNING available_seats
                    """,
            nativeQuery = true
    )
    Integer refundSeatNative(@Param ("eventId") Long eventId);
}