package com.siblo.rent.repository;

import com.siblo.rent.entity.Court;
import com.siblo.rent.entity.Court.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {

    List<Court> findBySportId(Long sportId);

    List<Court> findByStatus(CourtStatus status);

    @Query("SELECT c FROM Court c WHERE c.status = 'ACTIVE' AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.sport.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.venue.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Court> searchCourts(@Param("search") String search);

    @Query("SELECT c FROM Court c WHERE c.status = 'ACTIVE' AND (:sportId IS NULL OR c.sport.id = :sportId)")
    List<Court> findActiveCourts(@Param("sportId") Long sportId);

    long countByStatus(CourtStatus status);
}
