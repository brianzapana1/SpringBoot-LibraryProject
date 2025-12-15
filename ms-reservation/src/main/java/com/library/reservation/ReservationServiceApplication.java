package com.library.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.library.reservation", "bo.edu.ucb.microservices.util"})
public class ReservationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}
