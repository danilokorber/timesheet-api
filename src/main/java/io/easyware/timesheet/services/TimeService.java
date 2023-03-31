package io.easyware.timesheet.services;

import io.easyware.timesheet.entities.TimeRecord;
import lombok.extern.java.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log
@ApplicationScoped
public class TimeService {

    @Inject
    EntityManager entityManager;

    public Optional<TimeRecord> findById(String id) {
        return Optional.ofNullable(entityManager.find(TimeRecord.class, id));
    }

    public void save(TimeRecord tr) {
        entityManager.persist(tr);
    }

    public void delete(TimeRecord tr) {
        entityManager.remove(tr);
    }

    @Transactional
    public List<TimeRecord> start(String employeeId) {
        Optional<TimeRecord> otr = openRecordForToday(employeeId);
        TimeRecord tr;
        OffsetDateTime now = OffsetDateTime.now();

        tr = otr.orElseGet(() -> new TimeRecord(employeeId));
        if (tr.getStart() == null && (tr.getEnd() == null || tr.getEnd().isAfter(now))) {
            tr.setStart(now);
        }

        entityManager.persist(tr);
        return todaysRecords(employeeId);
    }

    @Transactional
    public List<TimeRecord> end(String employeeId) {
        Optional<TimeRecord> otr = openRecordForToday(employeeId);
        TimeRecord tr;
        OffsetDateTime now = OffsetDateTime.now();

        tr = otr.orElseGet(() -> new TimeRecord(employeeId));
        if (tr.getEnd() == null) {
            tr.setEnd(now);
        }

        entityManager.persist(tr);
        return todaysRecords(employeeId);
    }

    public List<TimeRecord> getTimeRecordsForMonth(int year, int month, String employeeId) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);

        OffsetDateTime startOfMonthOffset = startOfMonth.atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfMonthOffset = endOfMonth.atOffset(ZoneOffset.UTC);

        TypedQuery<TimeRecord> query = entityManager.createQuery("SELECT tr FROM TimeRecord tr WHERE tr.employeeId = :employeeId AND tr.start BETWEEN :start AND :end", TimeRecord.class)
                .setParameter("employeeId", employeeId)
                .setParameter("start", startOfMonthOffset)
                .setParameter("end", endOfMonthOffset);

        return query.getResultList();
    }

    public List<TimeRecord> getTimeRecordsForCurrentWeek(String employeeId) {
        LocalDateTime sunday = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime nextSunday = sunday.plusWeeks(1);
        OffsetDateTime sundayOffset = sunday.atOffset(ZoneOffset.UTC);
        OffsetDateTime nextSundayOffset = nextSunday.atOffset(ZoneOffset.UTC);

        log.info("Getting time records from " + sundayOffset.toString() + " to " + nextSundayOffset.toString() + " for employee " + employeeId);

        TypedQuery<TimeRecord> query = entityManager.createQuery("SELECT tr FROM TimeRecord tr WHERE tr.employeeId = :employeeId AND tr.start BETWEEN :start AND :end", TimeRecord.class)
                .setParameter("employeeId", employeeId)
                .setParameter("start", sundayOffset)
                .setParameter("end", nextSundayOffset);

//        TypedQuery<TimeRecord> query = entityManager.createQuery(
//                "SELECT tr FROM TimeRecord tr WHERE tr.employeeId = :employeeId AND WEEK(tr.startTime) = WEEK(CURDATE() - INTERVAL (DAYOFWEEK(CURDATE()) - 1) DAY)",
//                TimeRecord.class
//        );
//        query.setParameter("employeeId", employeeId);

        return query.getResultList();
    }

    public Optional<TimeRecord> openRecordForToday(String employeeId) {
        List<TimeRecord> results = openRecords(employeeId);
        if (!results.isEmpty()) {
            return Optional.of(results.get(0));
        } else {
            return Optional.empty();
        }
    }

    public List<TimeRecord> openRecords(String employeeId) {
        String jpql = "SELECT tr FROM TimeRecord tr WHERE (" +
                "(tr.start >= :startDate AND tr.start < :endDate) OR (tr.end >= :startDate AND tr.end < :endDate)) AND " +
                "(tr.start IS NULL OR tr.end IS NULL) AND " +
                "employeeId = :employeeId";
        return queryTimeRecords(jpql, employeeId);
    }

    public List<TimeRecord> todaysRecords(String employeeId) {
        String jpql = "SELECT tr FROM TimeRecord tr WHERE (" +
                "(tr.start >= :startDate AND tr.start < :endDate) OR (tr.end >= :startDate AND tr.end < :endDate)) AND " +
                "employeeId = :employeeId";
        return queryTimeRecords(jpql, employeeId);
    }

    private List<TimeRecord> queryTimeRecords(String jpql, String employeeId) {
        // Get the current date
        LocalDate today = LocalDate.now();

        TypedQuery<TimeRecord> query = entityManager.createQuery(jpql + " ORDER BY start ASC", TimeRecord.class); // Add the "ORDER BY" clause to sort by StartDate
        query.setParameter("startDate", today.atStartOfDay().atOffset(ZoneOffset.UTC));
        query.setParameter("endDate", today.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
        query.setParameter("employeeId", employeeId);

        // Execute the query and retrieve the results
        List<TimeRecord> timeRecords = query.getResultList();

        // Sort the results by StartDate
        Collections.sort(timeRecords, Comparator.comparing(TimeRecord::getStart));

        return timeRecords;
    }

    public String sumHours(List<TimeRecord> timeRecords) {
        long totalMinutes = 0;
        for (TimeRecord timeRecord : timeRecords) {
            String duration = timeRecord.getDuration();
            if (duration != null) {
                String[] parts = duration.split(":");
                long hours = Long.parseLong(parts[0]);
                long minutes = Long.parseLong(parts[1]);
                totalMinutes += hours * 60 + minutes;
            }
        }

        long totalHours = totalMinutes / 60;
        long remainingMinutes = totalMinutes % 60;
        return String.format("%d:%02d", totalHours, remainingMinutes);
    }

    public Map<String, Object> report(List<TimeRecord> timeRecords) {
        Map<String, Object> response = new HashMap<>();

        // Sort the results by StartDate
        timeRecords.sort(Comparator.comparing(TimeRecord::getStart));

        response.put("timeRecords", timeRecords);
        response.put("totalDuration", sumHours(timeRecords));
        return response;
    }
}
