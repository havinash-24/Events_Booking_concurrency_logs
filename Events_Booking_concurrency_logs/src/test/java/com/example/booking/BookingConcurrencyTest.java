package com.example.booking;

import com.example.booking.controller.BookingController;
import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.entity.Event;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.EventRepository;
import com.example.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class BookingConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private BookingService bookingService;

    // We autowire the controller to test the retry/recovery logic
    // that catches the DataIntegrityViolationException
    @Autowired
    private BookingController bookingController;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void testConcurrentBookings_naiveApproach() throws InterruptedException{
        Event event = new Event();
        event.setName("High Demand Concert");
        event.setTotalSeats(10);
        event.setAvailableSeats(10);
        event.setCreatedAt(LocalDateTime.now());
        event = eventRepository.save(event);

        Long eventId = event.getId();

        int numberOfThreads = 200;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);

        for(int i = 0; i < numberOfThreads; i++){
            Long userId = (long) i;
            executor.submit(()->{
                try {
                    startLatch.await();
                    // We test the service directly here for the standard seat race
                    bookingService.createBooking(null, new CreateBookingRequest(eventId, userId));
                    successfulBookings.incrementAndGet();
                }catch(Exception e){
                    failedBookings.incrementAndGet();
                }finally{
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        Event updatedEvent = eventRepository.findById(eventId).orElseThrow();

        // Assertions to prove our atomic queries successfully handled the race conditions
        assertEquals(10, successfulBookings.get(), "There should only be exactly 10 successful bookings");
        assertEquals(0, updatedEvent.getAvailableSeats(), "Available seats should be exactly 0");
    }

    @Test
    void testConcurrentBookings_sameIdempotencyKey() throws InterruptedException {
        Event event = new Event();
        event.setName("Idempotent Retry Concert");
        event.setTotalSeats(50);
        event.setAvailableSeats(50);
        event.setCreatedAt(LocalDateTime.now());
        event = eventRepository.save(event);

        Long eventId = event.getId();
        String sharedIdempotencyKey = "retry-key-999";

        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulResponses = new AtomicInteger(0);
        AtomicInteger failedResponses = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            Long userId = 1L; // Same user retrying
            executor.submit(() -> {
                try {
                    startLatch.await();
                    // We call the controller so it can catch the duplicate constraint exception and recover
                    bookingController.bookEvent(new CreateBookingRequest(eventId, userId), sharedIdempotencyKey);
                    successfulResponses.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Request failed: " + e.getMessage());
                    failedResponses.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Unleash the threads
        doneLatch.await();
        executor.shutdown();

        Event updatedEvent = eventRepository.findById(eventId).orElseThrow();

        // 1. Every thread should have gotten a successful 201 Created or 200 OK equivalent response
        assertEquals(50, successfulResponses.get(), "All 50 concurrent requests should return successfully");
        assertEquals(0, failedResponses.get(), "No requests should fail with 500 errors");

        // 2. Only ONE seat should have actually been consumed
        assertEquals(49, updatedEvent.getAvailableSeats(), "Exactly 1 seat should be consumed despite 50 requests");

        // 3. Only ONE row should exist in the bookings table for this idempotency key
        long duplicates = bookingRepository.findAll().stream()
                .filter(b -> sharedIdempotencyKey.equals(b.getIdempotencyKey()))
                .count();
        assertEquals(1, duplicates, "Exactly 1 booking row should be created");
    }
}
