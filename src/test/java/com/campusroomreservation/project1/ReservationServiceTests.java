package com.campusroomreservation.project1;

import com.campusroomreservation.project1.data.ReservationRepository;
import com.campusroomreservation.project1.data.RoomRepository;
import com.campusroomreservation.project1.data.UserRepository;
import com.campusroomreservation.project1.model.Reservation;
import com.campusroomreservation.project1.model.Room;
import com.campusroomreservation.project1.model.User;
import com.campusroomreservation.project1.services.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTests {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Room room;
    private User user;
    private Reservation reservation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    public void setUp() {
        room = new Room();
        room.setId(1L);
        room.setRoomNumber("202");

        user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setPassword("password");

        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusHours(2);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setRoom(room);
        reservation.setUser(user);
    }

    // Test booking a room with valid input and no overlapping reservations
    @Test
    void bookRoom_shouldSaveReservation_whenInputIsValidAndNoOverlap() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(1L, endTime, startTime)).thenReturn(Collections.emptyList());

        Reservation savedReservation = new Reservation();
        savedReservation.setRoom(room);
        savedReservation.setUser(user);
        savedReservation.setStartTime(startTime);
        savedReservation.setEndTime(endTime);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        Reservation result = reservationService.bookRoom(1L, "user", startTime, endTime);

        assertNotNull(result);
        assertEquals(savedReservation.getId(), result.getId());
        assertEquals(savedReservation.getRoom(), result.getRoom());
        assertEquals(savedReservation.getUser(), result.getUser());
        assertEquals(savedReservation.getStartTime(), result.getStartTime());
        assertEquals(savedReservation.getEndTime(), result.getEndTime());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    // Test booking a room with null start time
    @Test
    void bookRoom_shouldThrowException_whenStartTimeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookRoom(1L, "user", null, endTime);
        });
        assertEquals("Start time and end time must be provided", exception.getMessage());
        verifyNoInteractions(roomRepository, userRepository, reservationRepository);
    }

    // Test booking a room with null end time
    @Test
    void bookRoom_shouldThrowException_whenEndTimeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookRoom(1L, "user", startTime, null);
        });
        assertEquals("Start time and end time must be provided", exception.getMessage());
        verifyNoInteractions(roomRepository, userRepository, reservationRepository);
    }

    // Test booking a room with end time before start time
    @Test
    void bookRoom_shouldThrowException_whenEndTimeIsBeforeStartTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookRoom(1L, "user", startTime, startTime.minusHours(1));
        });
        assertEquals("Start time must be before end time", exception.getMessage());
        verifyNoInteractions(roomRepository, userRepository, reservationRepository);
    }

    // Test booking a room that does not exist
    @Test
    void bookRoom_shouldThrowException_whenRoomDoesNotExist() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookRoom(1L, "user", startTime, endTime);
        });
        assertEquals("Room not found with id: 1", exception.getMessage());
        verify(roomRepository, times(1)).findById(1L);
        verifyNoInteractions(userRepository, reservationRepository);
    }

    // Test booking a room when the user does not exist
    @Test
    void bookRoom_shouldThrowException_whenUserDoesNotExist() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookRoom(1L, "user", startTime, endTime);
        });
        assertEquals("User not found with username: user", exception.getMessage());
        verify(roomRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername("user");
        verifyNoInteractions(reservationRepository);
    }

    // Test booking a room that is already booked
    @Test
    void bookRoom_shouldThrowException_whenRoomIsAlreadyBooked() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(1L, endTime, startTime)).thenReturn(List.of(new Reservation()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookRoom(1L, "user", startTime, endTime);
        });
        
        assertEquals("The room is already booked for the given time slot", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    // Test getting reservations for a user
    @Test
    void getReservationForUser_shouldReturnReservations() {
        List<Reservation> reservations = List.of(reservation);

        when(reservationRepository.findByUserUsernameOrderByStartTimeAsc("user"))
        .thenReturn(reservations);

        List<Reservation> result = reservationService.getReservationsForUser("user");
        assertEquals(reservations, result);
        verify(reservationRepository, times(1)).findByUserUsernameOrderByStartTimeAsc("user");
    }

    // Test cancelling a reservation when the user owns the reservation
    @Test
    void cancelReservation_shouldDeleteReservation_whenUserOwnsReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L, "user");

        verify(reservationRepository, times(1)).delete(reservation);
    }

    // Test cancelling a reservation when the user does not own the reservation
    @Test
    void cancelReservation_shouldThrowException_whenUserDoesNotOwnReservation() {
        User other = new User();
        other.setId(2L);
        other.setUsername("otherUser");

        reservation.setUser(other);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(1L, "user");
        });

        assertEquals("User is not authorized to cancel this reservation", exception.getMessage());
        verify(reservationRepository, never()).delete(any());
    }

    // Test cancelling a reservation when the reservation does not exist
    @Test
    void cancelReservation_shouldThrowException_whenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(1L, "user");
        });

        assertEquals("Reservation not found with id: 1", exception.getMessage());
        verify(reservationRepository, never()).delete(any());
    }
}