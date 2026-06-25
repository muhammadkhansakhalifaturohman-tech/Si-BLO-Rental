package com.siblo.rent.repository;

import com.siblo.rent.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query("SELECT DISTINCT v FROM Venue v LEFT JOIN FETCH v.courts c LEFT JOIN FETCH c.sport")
    List<Venue> findAllWithCourts();
}
