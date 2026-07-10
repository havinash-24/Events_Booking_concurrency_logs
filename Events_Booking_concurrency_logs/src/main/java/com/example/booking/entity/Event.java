package com.example.booking.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name ="events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_seats",nullable = false)//snake case in production databases
    private Integer totalSeats; //this is camelCase style for java

    @Column(name = "available_seats",nullable = false)
    private Integer availableSeats;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    public Event(){

    }
    public String getName(){
        return name;
    }
    public Long getId(){
        return id;
    }
    public Integer getTotalSeats(){

        return totalSeats;
    }
    public Integer getAvailableSeats(){

        return availableSeats;
    }
    public LocalDateTime getCreatedAt(){

        return createdAt;
    }
    public void setName(String name){

        this.name = name;
    }
    public void setTotalSeats(Integer totalSeats){

        this.totalSeats = totalSeats;
    }
    public void setAvailableSeats(Integer availableSeats){

        this.availableSeats = availableSeats;
    }
    public void setCreatedAt(LocalDateTime createdAt){

        this.createdAt = createdAt;
    }
}
