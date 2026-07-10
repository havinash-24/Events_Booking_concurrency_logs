package com.example.booking.entity;

import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings",
uniqueConstraints = @UniqueConstraint(name = "uk_bookings_idempotency_key",columnNames = "idempotency_key"))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",nullable = false)
    private Event event;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name ="cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name ="idempotency_key",unique = true)
    private String idempotencyKey;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BookingStatus getStatus(){ return status; }
    public void setStatus(BookingStatus status){ this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCancelledAt(){ return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt){ this.cancelledAt = cancelledAt; }

    // NEW METHODS ADDED HERE
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
