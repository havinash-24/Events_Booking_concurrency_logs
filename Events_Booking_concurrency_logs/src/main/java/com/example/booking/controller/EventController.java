//package com.example.booking.controller;
//
//import com.example.booking.dto.request.CreateEventRequest;
//import com.example.booking.dto.response.EventResponse;
//import com.example.booking.service.EventService;
//import jakarta.validation.Valid;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/events")
//public class EventController {
//    private final EventService eventService;
//
//    public EventController(EventService eventService){
//        this.eventService = eventService;
//    }
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request){
//        return eventService.createEvent(request);
//    }
//    @GetMapping
//    public List<EventResponse> getAllEvents(){
//        return eventService.getAllEvents();
//    }
//
//    @GetMapping("/{id}")
//    public EventResponse getEventIdById(@PathVariable Long id){
//        return eventService.getEventById(id);
//    }
//
//
//}
package com.example.booking.controller;

import com.example.booking.dto.request.CreateEventRequest;
import com.example.booking.dto.response.EventResponse;
import com.example.booking.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request){
        return eventService.createEvent(request);
    }

    @GetMapping
    public List<EventResponse> getAllEvents(){
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id){
        return eventService.getEventById(id);
    }
}

