package com.campusroomreservation.project1.controllers;

import com.campusroomreservation.project1.services.ReservationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations/book")
    public String bookRoom(@RequestParam Long roomId, @RequestParam String start, @RequestParam String end, Authentication authentication) {

        String username = authentication.getName();
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);

        reservationService.bookRoom(roomId, username, startTime, endTime);

        return "redirect:/dashboard"; // Redirect to a dashboard or confirmation page after booking

    } 
}
