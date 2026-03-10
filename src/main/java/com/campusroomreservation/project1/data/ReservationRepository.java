package com.campusroomreservation.project1.data;

import com.campusroomreservation.project1.model.Reservation;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(Long roomId, LocalDateTime requestedEndTime, LocalDateTime requestedStartTime);

    List<Reservation> findByUserUsernameOrderByStartTimeAsc(String username);
    
}
