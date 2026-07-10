package com.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingApiConfig(){
        return new OpenAPI()
                .info(new Info()
                        .title("Concurrency-Safe Event Booking API")
                        .description("A high-performance booking System demonstrating atomic Postgres updates and race-condition defense")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Havinash")
                                .url("https://github.com/havinash-24")));
    }
}