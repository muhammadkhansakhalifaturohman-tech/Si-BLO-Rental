package com.siblo.rent.repository;

import com.siblo.rent.entity.TimeSlot;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByCourtIdAndDateOrderByStartTime(Long courtId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id IN :ids ORDER BY t.startTime")
    List<TimeSlot> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(DISTINCT t.court.id) FROM TimeSlot t WHERE t.date = :date AND t.status = 'AVAILABLE'")
    long countCourtsWithAvailableSlots(@Param("date") LocalDate date);

    boolean existsByCourtIdAndDateAndStatus(Long courtId, LocalDate date, SlotStatus status);

    boolean existsByCourtIdAndDateAndStartTime(Long courtId, LocalDate date, LocalTime startTime);

    void deleteByCourtIdAndDateAfter(Long courtId, LocalDate date);

    List<TimeSlot> findByCourtIdAndDateAfter(Long courtId, LocalDate date);

    long countByCourtIdAndDateAfter(Long courtId, LocalDate date);
}
