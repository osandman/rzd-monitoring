package net.osandman.rzdmonitoring.jpa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.jpa.entity.MonitoringTask;
import net.osandman.rzdmonitoring.jpa.entity.TaskState;
import net.osandman.rzdmonitoring.jpa.repository.MonitoringTaskRepository;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringTaskService {

    private final MonitoringTaskRepository taskRepository;

    @Transactional
    public MonitoringTask saveTask(TicketsTask ticketsTask, Set<String> seatFilters, Long intervalMinutes) {
        MonitoringTask task = MonitoringTask.fromTicketsTask(ticketsTask, seatFilters, intervalMinutes);
        MonitoringTask saved = taskRepository.save(task);
        log.info("Задача сохранена в БД: {}", saved.getTaskId());
        return saved;
    }

    // Поиск только активных (НЕ закрытых) задач
    @Transactional(readOnly = true)
    public Optional<MonitoringTask> findActiveTaskById(String taskId) {
        return taskRepository.findActiveTaskById(taskId);
    }

    @Transactional(readOnly = true)
    public List<MonitoringTask> findActiveTasks() {
        return taskRepository.findByStateAndNotClosed(TaskState.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<MonitoringTask> findActiveTasksByChatId(Long chatId) {
        return taskRepository.findByChatIdAndStateAndNotClosed(chatId, TaskState.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<MonitoringTask> findNotClosedTasksByChatId(Long chatId) {
        return taskRepository.findActiveByChatId(chatId);
    }

    // Все задачи (включая закрытые) - для статистики и истории
    @Transactional(readOnly = true)
    public List<MonitoringTask> findAllTasksByChatId(Long chatId) {
        return taskRepository.findByChatId(chatId);
    }

    // Закрытие задачи вместо удаления
    @Transactional
    public boolean closeTask(String taskId) {
        int updated = taskRepository.closeTask(taskId, ZonedDateTime.now(), TaskState.CLOSED);
        if (updated > 0) {
            log.info("Задача закрыта: {}", taskId);
            return true;
        }
        return false;
    }

    // Закрытие всех задач пользователя
    @Transactional
    public int closeAllTasksForChatId(Long chatId) {
        int closed = taskRepository.closeAllTasksForChatId(chatId, ZonedDateTime.now());
        log.info("Закрыто {} задач для chatId: {}", closed, chatId);
        return closed;
    }

    @Transactional
    public void updateTaskState(String taskId, TaskState state) {
        taskRepository.findActiveTaskById(taskId).ifPresent(task -> {
            task.setState(state);
            taskRepository.save(task);
            log.info("Состояние задачи {} изменено на: {}", taskId, state);
        });
    }

    @Transactional
    public void updateAllActiveTasksInterval(Long intervalMinutes) {
        List<MonitoringTask> allActive = taskRepository.findAllActive();
        allActive.forEach(task -> task.setIntervalMinutes(intervalMinutes));
        taskRepository.saveAll(allActive);
        log.info("Интервал всех задач изменен на: {} минут", intervalMinutes);
    }

    @Transactional
    public void updateTaskExecution(String taskId) {
        taskRepository.updateLastExecution(taskId, ZonedDateTime.now());
    }

    @Transactional
    public void incrementTaskErrorCount(String taskId, String errorMessage) {
        taskRepository.incrementErrorCount(taskId, errorMessage);
        log.warn("Ошибка в задаче {}: {}", taskId, errorMessage);
    }

    // Статистика с учетом закрытых задач
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskStatistics(Long chatId) {
        List<MonitoringTask> allTasks = findAllTasksByChatId(chatId);
        List<MonitoringTask> activeTasks = findNotClosedTasksByChatId(chatId);

        Map<TaskState, Long> stateCount = activeTasks.stream()
            .collect(Collectors.groupingBy(MonitoringTask::getState, Collectors.counting()));

        Long totalExecutions = allTasks.stream()
            .mapToLong(MonitoringTask::getExecutionCount)
            .sum();

        Long totalErrors = allTasks.stream()
            .mapToLong(MonitoringTask::getErrorCount)
            .sum();

        long closedTasksCount = taskRepository.countClosedTasksByChatId(chatId);

        return Map.of(
            "totalTasks", allTasks.size(),
            "activeTasks", stateCount.getOrDefault(TaskState.ACTIVE, 0L),
            "pausedTasks", stateCount.getOrDefault(TaskState.PAUSED, 0L),
            "errorTasks", stateCount.getOrDefault(TaskState.ERROR, 0L),
            "closedTasks", closedTasksCount,
            "totalExecutions", totalExecutions,
            "totalErrors", totalErrors
        );
    }

    // Получить историю задач пользователя (включая закрытые)
    @Transactional(readOnly = true)
    public List<MonitoringTask> getTaskHistory(Long chatId, int limit) {
        return taskRepository.findByChatId(chatId)
            .stream()
            .sorted(Comparator.comparing(MonitoringTask::getCreatedAt).reversed())
            .limit(limit)
            .toList();
    }

    @Transactional
    public int closeAllTasks() {
        int closedCount = taskRepository.closeAllTasks(ZonedDateTime.now());
        log.info("В таблице закрыто {} задач", closedCount);
        return closedCount;
    }
}
