package com.campusroomreservation.project1;

import com.campusroomreservation.project1.data.ReservationRepository;
import com.campusroomreservation.project1.data.RoomRepository;
import com.campusroomreservation.project1.model.Amenity;
import com.campusroomreservation.project1.model.Reservation;
import com.campusroomreservation.project1.model.Room;
import com.campusroomreservation.project1.services.RoomSearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomSearchServiceTests {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RoomSearchService roomSearchService;

    private Room projectorRoom;
    private Room whiteboardRoom;
    private Room fullAmenityRoom;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    public void setUp() {
        Amenity projector = new Amenity();
        projector.setName("PROJECTOR");

        Amenity whiteboard = new Amenity();
        whiteboard.setName("WHITEBOARD");

        projectorRoom = new Room();
        projectorRoom.setId(1L);
        projectorRoom.setRoomNumber("101");
        projectorRoom.setCapacity(10);
        projectorRoom.setAmenities(Set.of(projector));

        whiteboardRoom = new Room();
        whiteboardRoom.setId(2L);
        whiteboardRoom.setRoomNumber("102");
        whiteboardRoom.setCapacity(12);
        whiteboardRoom.setAmenities(Set.of(whiteboard));

        fullAmenityRoom = new Room();
        fullAmenityRoom.setId(3L);
        fullAmenityRoom.setRoomNumber("103");
        fullAmenityRoom.setCapacity(15);
        fullAmenityRoom.setAmenities(Set.of(projector, whiteboard));

        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusHours(2);
    }

    // Test for null start time
    @Test
    void searchAvailableRooms_shouldThrowException_whenStartTimeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomSearchService.searchAvailableRooms(1L, 5, false, false, null, endTime);
        });
        assertEquals("Start time and end time must be provided", exception.getMessage());
        verifyNoInteractions(roomRepository, reservationRepository);
    }

    // Test for null end time
    @Test
    void searchAvailableRooms_shouldThrowException_whenEndTimeIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomSearchService.searchAvailableRooms(1L, 5, false, false, startTime, null);
        });
        assertEquals("Start time and end time must be provided", exception.getMessage());
        verifyNoInteractions(roomRepository, reservationRepository);
    }

    // Test for start time equal to end time
    @Test
    void searchAvailableRooms_shouldThrowException_whenStartTimeEqualsEndTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomSearchService.searchAvailableRooms(1L, 5, false, false, startTime, startTime);
        });
        assertEquals("Start time must be before end time", exception.getMessage());
        verifyNoInteractions(roomRepository, reservationRepository);
    }

    // Test for start time after end time
    @Test
    void searchAvailableRooms_shouldThrowException_whenStartTimeIsAfterEndTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomSearchService.searchAvailableRooms(1L, 5, false, false, endTime, startTime);
        });
        assertEquals("Start time must be before end time", exception.getMessage());
        verifyNoInteractions(roomRepository, reservationRepository);
    }

    // Test for no amenities required and no overlapping reservations
    @Test
    void searchAvailableRooms_shouldReturnMatchingRooms_whenNoAmenitiesRequiredAndNoOverlap() {
        when(roomRepository.findByBuildingIdAndCapacityGreaterThanEqual(1L,5 )).thenReturn(List.of(projectorRoom, whiteboardRoom, fullAmenityRoom));

        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(1L, endTime, startTime)).thenReturn(Collections.emptyList());
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(2L, endTime, startTime)).thenReturn(Collections.emptyList());
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(3L, endTime, startTime)).thenReturn(Collections.emptyList());

        List<Room> availableRooms = roomSearchService.searchAvailableRooms(1L, 5, false, false, startTime, endTime);

        assertEquals(3, availableRooms.size());
        assertTrue(availableRooms.contains(projectorRoom));
        assertTrue(availableRooms.contains(whiteboardRoom));
        assertTrue(availableRooms.contains(fullAmenityRoom));
    }

    // Test for projector required and no overlapping reservations
    @Test
    void searchAvailableRooms_shouldReturnMatchingRooms_whenProjectorRequiredAndNoOverlap() {
        when(roomRepository.findByBuildingIdAndCapacityGreaterThanEqual(1L,5 )).thenReturn(List.of(projectorRoom, whiteboardRoom, fullAmenityRoom));

        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(1L, endTime, startTime)).thenReturn(Collections.emptyList());
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(3L, endTime, startTime)).thenReturn(Collections.emptyList());

        List<Room> availableRooms = roomSearchService.searchAvailableRooms(1L, 5, true, false, startTime, endTime);

        assertEquals(2, availableRooms.size());
        assertTrue(availableRooms.contains(projectorRoom));
        assertTrue(availableRooms.contains(fullAmenityRoom));
        assertFalse(availableRooms.contains(whiteboardRoom));
    }

    // Test for whiteboard required and no overlapping reservations
    @Test
    void searchAvailableRooms_shouldReturnMatchingRooms_whenWhiteboardRequiredAndNoOverlap() {
        when(roomRepository.findByBuildingIdAndCapacityGreaterThanEqual(1L,5 )).thenReturn(List.of(projectorRoom, whiteboardRoom, fullAmenityRoom));

        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(2L, endTime, startTime)).thenReturn(Collections.emptyList());
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(3L, endTime, startTime)).thenReturn(Collections.emptyList());

        List<Room> availableRooms = roomSearchService.searchAvailableRooms(1L, 5, false, true, startTime, endTime);

        assertEquals(2, availableRooms.size());
        assertTrue(availableRooms.contains(whiteboardRoom));
        assertTrue(availableRooms.contains(fullAmenityRoom));
        assertFalse(availableRooms.contains(projectorRoom));
    }

    // Test for both projector and whiteboard required and no overlapping reservations
    @Test
    void searchAvailableRooms_shouldReturnMatchingRooms_whenProjectorAndWhiteboardRequiredAndNoOverlap() {
        when(roomRepository.findByBuildingIdAndCapacityGreaterThanEqual(1L,5 )).thenReturn(List.of(projectorRoom, whiteboardRoom, fullAmenityRoom));

        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(3L, endTime, startTime)).thenReturn(Collections.emptyList());

        List<Room> availableRooms = roomSearchService.searchAvailableRooms(1L, 5, true, true, startTime, endTime);

        assertEquals(1, availableRooms.size());
        assertTrue(availableRooms.contains(fullAmenityRoom));
        assertFalse(availableRooms.contains(projectorRoom));
        assertFalse(availableRooms.contains(whiteboardRoom));
    }

    // Test for excluding rooms with overlapping reservations
    @Test
    void searchAvailableRooms_shouldExcludeRoomsWithOverlappingReservations() {
        when(roomRepository.findByBuildingIdAndCapacityGreaterThanEqual(1L,5 )).thenReturn(List.of(projectorRoom, fullAmenityRoom));

        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(1L, endTime, startTime)).thenReturn(List.of(new Reservation()));
        when(reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(3L, endTime, startTime)).thenReturn(Collections.emptyList());

        List<Room> availableRooms = roomSearchService.searchAvailableRooms(1L, 5, false, false, startTime, endTime);

        assertEquals(1, availableRooms.size());
        assertTrue(availableRooms.contains(fullAmenityRoom));
        assertFalse(availableRooms.contains(projectorRoom));
    }
}