package com.campusroomreservation.project1.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.campusroomreservation.project1.services.ReservationService;
import org.springframework.ui.Model;

@Controller
public class DashboardController {
    
    private final ReservationService reservationService;

    public DashboardController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("reservations", reservationService.getReservationsForUser(username));
        return "dashboard"; // Return the name of the dashboard view
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable("id") Long reservationId, Authentication authentication) {
        String username = authentication.getName();
        reservationService.cancelReservation(reservationId, username);
        return "redirect:/dashboard";
    }
}
