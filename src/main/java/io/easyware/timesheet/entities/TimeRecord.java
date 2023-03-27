package io.easyware.timesheet.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Duration;
import java.time.OffsetDateTime;

@Entity
@Data
@Log
@Table(name = "time_record")
@NoArgsConstructor
public class TimeRecord {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "start_time")
    private OffsetDateTime start;

    @Column(name = "end_time")
    private OffsetDateTime end;

    public TimeRecord(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDuration() {
        if (start != null && end != null) {
            Duration duration = Duration.between(start, end);
            long minutes = duration.toMinutes();

            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            return String.format("%d:%02d", hours, remainingMinutes);

        }
        return null;
    }

}
