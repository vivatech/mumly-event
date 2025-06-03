package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    @Query("SELECT a.date, " +
            "SUM(CASE WHEN a.present = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN a.present = false THEN 1 ELSE 0 END) " +
            "FROM Attendance a " +
            "WHERE a.eventRegistration.id = ?1 " +
            "AND a.date BETWEEN ?2 AND ?3 " +
            "GROUP BY a.date")
    List<Object[]> getAttendanceSummary(Integer participantId, LocalDate startDate, LocalDate endDate);


    List<Attendance> findByEventRegistrationId(Integer participantId);
}
