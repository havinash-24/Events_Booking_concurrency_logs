package com.example.booking.service;

import com.example.booking.dto.request.CreateEventRequest;
import com.example.booking.dto.response.EventResponse;
import com.example.booking.entity.Event;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }
    public EventResponse createEvent(CreateEventRequest request){
        Event event = new Event();

        event.setName(request.name());
        event.setTotalSeats(request.totalSeats());
        event.setAvailableSeats(request.totalSeats());
        event.setCreatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getTotalSeats(),
                savedEvent.getAvailableSeats(),
                savedEvent.getCreatedAt()
        );
    }
    public List<EventResponse> getAllEvents(){
        return eventRepository.findAll()
                .stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getName(),
                        event.getTotalSeats(),
                        event.getAvailableSeats(),
                        event.getCreatedAt()
                ))
                .toList();
    }
    public EventResponse getEventById(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getCreatedAt()
        );
    }
}
