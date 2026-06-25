package com.siblo.rent.repository;

import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status IN :statuses AND b.date >= :today ORDER BY b.date, b.startTime")
    List<Booking> findUpcoming(@Param("userId") Long userId, @Param("statuses") List<BookingStatus> statuses, @Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND (b.status IN ('COMPLETED','CANCELLED') OR (b.status = 'CONFIRMED' AND b.date < :today)) ORDER BY b.date DESC")
    List<Booking> findPast(@Param("userId") Long userId, @Param("today") LocalDate today);

    List<Booking> findByCourtIdAndDate(Long courtId, LocalDate date);

    long countByUserIdAndStatus(Long userId, BookingStatus status);

    long countByStatus(BookingStatus status);

    List<Booking> findByStatusAndPaymentExpiresAtBefore(BookingStatus status, LocalDateTime expiresAt);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.date = :date AND b.status IN ('CONFIRMED', 'COMPLETED')")
    Long sumRevenueByDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.date = :date ORDER BY b.startTime")
    List<Booking> findByDateOrderByStartTime(@Param("date") LocalDate date);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'CONFIRMED'
          AND (b.date < :today OR (b.date = :today AND b.endTime <= :now))
        """)
    List<Booking> findConfirmedReadyToComplete(@Param("today") LocalDate today, @Param("now") LocalTime now);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'CONFIRMED'
          AND b.date = :today
          AND b.startTime <= :now AND b.endTime > :now
        """)
    List<Booking> findConfirmedReadyToActivate(@Param("today") LocalDate today, @Param("now") LocalTime now);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'ACTIVE'
          AND (b.date < :today OR (b.date = :today AND b.endTime <= :now))
        """)
    List<Booking> findActiveReadyToComplete(@Param("today") LocalDate today, @Param("now") LocalTime now);
}
