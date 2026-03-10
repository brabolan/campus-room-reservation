package com.campusroomreservation.project1.data;

import com.campusroomreservation.project1.model.Building;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuildingRepository extends CrudRepository<Building, Long> {
    
    Optional<Building> findByName(String name);
}
