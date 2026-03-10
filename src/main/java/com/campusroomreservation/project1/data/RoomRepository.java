package com.campusroomreservation.project1.data;

import com.campusroomreservation.project1.model.Room;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<Room, Long> {
    
    List<Room> findByBuildingIdAndCapacityGreaterThanEqual(Long buildingId, int minCapacity);
}
