package net.osandman.rzdmonitoring.jpa.repository;

import net.osandman.rzdmonitoring.jpa.entity.MonitoringTask;
import net.osandman.rzdmonitoring.jpa.entity.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoringTaskRepository extends JpaRepository<MonitoringTask, Long> {

    // Поиск только НЕ закрытых задач
    @Query("SELECT t FROM MonitoringTask t WHERE t.closedAt IS NULL")
    List<MonitoringTask> findAllActive();

    // Поиск по chatId только НЕ закрытых
    @Query("SELECT t FROM MonitoringTask t WHERE t.chatId = :chatId AND t.closedAt IS NULL")
    List<MonitoringTask> findActiveByChatId(@Param("chatId") Long chatId);

    // Поиск активных задач (НЕ закрытые)
    @Query("SELECT t FROM MonitoringTask t WHERE t.closedAt IS NULL AND t.state IN :states")
    List<MonitoringTask> findByStateInAndNotClosed(@Param("states") List<TaskState> states);

    // Все задачи по chatId (включая закрытые) - для статистики
    List<MonitoringTask> findByChatId(Long chatId);

    // Статус + НЕ закрыто
    @Query("SELECT t FROM MonitoringTask t WHERE t.chatId = :chatId AND t.state = :state AND t.closedAt IS NULL")
    List<MonitoringTask> findByChatIdAndStateAndNotClosed(@Param("chatId") Long chatId, @Param("state") TaskState state);

    @Modifying
    @Query("""
        UPDATE MonitoringTask t
        SET t.closedAt = :closedAt, t.state = :state
        WHERE t.taskId = :taskId AND t.closedAt IS NULL
        """)
    int closeTask(
        @Param("taskId") String taskId,
        @Param("closedAt") ZonedDateTime closedAt,
        @Param("state") TaskState taskState
    );

    @Modifying
    @Query("""
        UPDATE MonitoringTask t
        SET t.closedAt = :closedAt, t.state = :state
        WHERE t.chatId = :chatId AND t.closedAt IS NULL
        """)
    int closeAllTasksForChatId(
        @Param("chatId") Long chatId, @Param("closedAt") ZonedDateTime closedAt, @Param("state") TaskState taskState
    );

    @Modifying
    @Query("""
        UPDATE MonitoringTask t
        SET t.closedAt = :closedAt, t.state = :state
        WHERE t.closedAt IS NULL
        """)
    int closeAllTasks(@Param("closedAt") ZonedDateTime closedAt, @Param("state") TaskState taskState);

    // Обновление времени выполнения
    @Modifying
    @Query("""
        UPDATE MonitoringTask t
        SET t.lastExecutionAt = :executionTime, t.executionCount = t.executionCount + 1
        WHERE t.taskId = :taskId
        """)
    void updateLastExecution(@Param("taskId") String taskId, @Param("executionTime") ZonedDateTime executionTime);

    // Увеличение счетчика ошибок
    @Modifying
    @Query("""
        UPDATE MonitoringTask t
        SET t.errorCount = t.errorCount + 1, t.lastErrorMessage = :errorMessage
        WHERE t.taskId = :taskId
        """)
    void incrementErrorCount(@Param("taskId") String taskId, @Param("errorMessage") String errorMessage);

    // Статистика - количество закрытых задач
    @Query("SELECT COUNT(t) FROM MonitoringTask t WHERE t.chatId = :chatId AND t.closedAt IS NOT NULL")
    long countClosedTasksByChatId(@Param("chatId") Long chatId);

    // Найти задачу по ID, только если она НЕ закрыта
    @Query("SELECT t FROM MonitoringTask t WHERE t.taskId = :taskId AND t.closedAt IS NULL")
    Optional<MonitoringTask> findActiveTaskById(@Param("taskId") String taskId);
}
