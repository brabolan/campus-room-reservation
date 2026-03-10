package com.campusroomreservation.project1.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.campusroomreservation.project1.data.ReservationRepository;
import com.campusroomreservation.project1.data.RoomRepository;
import com.campusroomreservation.project1.model.Room;
import com.campusroomreservation.project1.model.Amenity;

import jakarta.transaction.Transactional;

@Service
public class RoomSearchService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomSearchService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    // Method that will search for available rooms by building

    @Transactional
    public List<Room> searchAvailableRooms(Long buildingId, int minCapacity, boolean hasProjector, boolean hasWhiteboard, LocalDateTime startTime, LocalDateTime endTime) {

        validateTimeSlot(startTime, endTime);

        // Step 1: Base filter by building and capacity

        List<Room> initialRooms = roomRepository.findByBuildingIdAndCapacityGreaterThanEqual(buildingId, minCapacity);

        // Step 2: Filter by amenities

        List<Room> roomsWithAmenities = new ArrayList<>();
        for (Room room : initialRooms) {
            if (amenityMatches(room, hasProjector, hasWhiteboard)) {
                roomsWithAmenities.add(room);
            }
        }

        // Step 3: Filter by availability

        List<Room> availableRooms = new ArrayList<>();
        for (Room room : roomsWithAmenities) {
            boolean hasOverlap = !reservationRepository.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
                    room.getId(), endTime, startTime).isEmpty();

            if (!hasOverlap) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    // Helper method for amenity filtering
    private boolean amenityMatches(Room room, boolean hasProjector, boolean hasWhiteboard) {
        if (!hasProjector && !hasWhiteboard) {
            return true; // No specific amenity requirement
        }

        Set<String> roomAmenityNames = new HashSet<>();
        if (room.getAmenities() != null) {
            for (Amenity amenity : room.getAmenities()) {
                if (amenity != null && amenity.getName() != null) {
                    roomAmenityNames.add(amenity.getName().toUpperCase());
                }
            }
        }

        if (hasProjector && !roomAmenityNames.contains("PROJECTOR")) {
            return false;
        }
        if (hasWhiteboard && !roomAmenityNames.contains("WHITEBOARD")) {
            return false;
        }
        return true;
    }

    // Helper method to validate time slot
    private void validateTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must be provided");
        }
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    
}
