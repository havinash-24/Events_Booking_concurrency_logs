package com.example.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SoldOutException extends RuntimeException{
    public SoldOutException(String message){
        super(message);
    }
}
