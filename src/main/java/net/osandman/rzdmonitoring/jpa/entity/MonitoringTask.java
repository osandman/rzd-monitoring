package net.osandman.rzdmonitoring.jpa.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tasks", schema = "rzd_monitoring")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class MonitoringTask {

    @Id
    @Column(name = "task_id")
    private String taskId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "username")
    private String username;

    @Column(name = "from_code", nullable = false)
    private String fromCode;

    @Column(name = "from_station", nullable = false)
    private String fromStation;

    @Column(name = "to_code", nullable = false)
    private String toCode;

    @Column(name = "to_station", nullable = false)
    private String toStation;

    @Column(name = "departure_date", nullable = false)
    private String departureDate;

    @Type(JsonType.class)
    @Column(name = "train_departure_map", columnDefinition = "jsonb")
    private Map<String, LocalDateTime> trainDepartureDateMap;

    @Type(JsonType.class)
    @Column(name = "seat_filters", columnDefinition = "jsonb")
    private Set<String> seatFilters;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    @Builder.Default
    private TaskState state = TaskState.ACTIVE;

    @Column(name = "interval_minutes", nullable = false)
    @Builder.Default
    private Long intervalMinutes = 10L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "closed_at")
    private ZonedDateTime closedAt;

    @Column(name = "last_execution_at")
    private ZonedDateTime lastExecutionAt;

    @Column(name = "execution_count")
    @Builder.Default
    private Long executionCount = 0L;

    @Column(name = "error_count")
    @Builder.Default
    private Long errorCount = 0L;

    @Column(name = "last_error_message")
    private String lastErrorMessage;

    // Проверка закрыта ли задача
    public boolean isClosed() {
        return closedAt != null;
    }

    // Проверка активна ли задача (не закрыта и активна)
    public boolean isActiveAndNotClosed() {
        return !isClosed() && state == TaskState.ACTIVE;
    }

    // Закрыть задачу
    public void close() {
        this.closedAt = ZonedDateTime.now();
    }

    // Конвертация в TicketsTask
    public TicketsTask toTicketsTask() {
        return TicketsTask.builder()
            .chatId(this.chatId)
            .userName(this.username)
            .taskId(this.taskId)
            .date(this.departureDate)
            .fromCode(this.fromCode)
            .fromStation(this.fromStation)
            .toCode(this.toCode)
            .toStation(this.toStation)
            .trainDepartureDateMap(this.trainDepartureDateMap)
            .filters(this.seatFilters)
            .build();
    }

    // Создание из TicketsTask
    public static MonitoringTask fromTicketsTask(TicketsTask ticketsTask, Set<String> seatFilters, Long intervalMinutes) {
        return MonitoringTask.builder()
            .taskId(ticketsTask.taskId())
            .chatId(ticketsTask.chatId())
            .username(ticketsTask.userName())
            .fromCode(ticketsTask.fromCode())
            .fromStation(ticketsTask.fromStation())
            .toCode(ticketsTask.toCode())
            .toStation(ticketsTask.toStation())
            .departureDate(ticketsTask.date())
            .trainDepartureDateMap(ticketsTask.trainDepartureDateMap())
            .seatFilters(seatFilters)
            .intervalMinutes(intervalMinutes)
            .state(TaskState.ACTIVE)
            .closedAt(null)
            .build();
    }
}
