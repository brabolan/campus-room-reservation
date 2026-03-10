package com.campusroomreservation.project1.data;

import com.campusroomreservation.project1.model.Amenity;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends CrudRepository<Amenity, Long> {
    
    Optional<Amenity> findByName(String name);
}
