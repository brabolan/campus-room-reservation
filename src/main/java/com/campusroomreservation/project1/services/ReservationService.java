package com.campusroomreservation.project1.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.campusroomreservation.project1.data.ReservationRepository;
import com.campusroomreservation.project1.data.RoomRepository;
import com.campusroomreservation.project1.data.UserRepository;
import com.campusroomreservation.project1.model.Reservation;
import com.campusroomreservation.project1.model.Room;
import com.campusroomreservation.project1.model.User;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    // Method to book a room for user and time slot
    @Transactional
    public Reservation bookRoom(Long roomId, String username, LocalDateTime startTime, LocalDateTime endTime) {

        validateTimeSlot(startTime, endTime);

        // Fetch room and user entities
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        

        // Overlap check with time slots
        boolean hasOverlap = !reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId, endTime, startTime).isEmpty();

        if (hasOverlap) {
            throw new IllegalArgumentException("The room is already booked for the given time slot");
        }

        // Create and save reservation
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setUser(user);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        return reservationRepository.save(reservation);                         

    }

    // Method to retrieve a users reservations ordered by start time
    @Transactional
    public List<Reservation> getReservationsForUser(String username) {
        return reservationRepository.findByUserUsernameOrderByStartTimeAsc(username);
    }

    // Method to cancel a reservation
    @Transactional
    public void cancelReservation(Long reservationId, String username) {
        Reservation reservation  = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));
        
        if (!reservation.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("User is not authorized to cancel this reservation");
        }

        reservationRepository.delete(reservation);
    }

    // Method to retrieve a users reservations ordered by start time - to be implemented

    // Method to validate time slot
    private void validateTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must be provided");
        }
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
    
}
