package net.osandman.rzdmonitoring.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.jpa.entity.MonitoringTask;
import net.osandman.rzdmonitoring.jpa.entity.TaskState;
import net.osandman.rzdmonitoring.jpa.service.MonitoringTaskService;
import net.osandman.rzdmonitoring.service.seat.SeatFilter;
import net.osandman.rzdmonitoring.service.seat.TicketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MultiTaskScheduler implements SchedulingConfigurer {

    private final ScheduleConfig scheduleConfig;
    private final TicketService ticketService;
    private final MonitoringTaskService taskService;

    @Getter
    private final Map<Long, Map<String, TaskInfo>> scheduledTasks = new ConcurrentHashMap<>();
    private ScheduledTaskRegistrar taskRegistrar;

    /**
     * Информация о задаче.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class TaskInfo {
        private TicketsTask ticketsTask;
        private Runnable runnable;
        private ScheduledFuture<?> scheduledFuture;
        private TaskState state;
        private long interval;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("Установлены параметры шедулера запуска мониторинга билетов: state='{}', interval='{}' минут",
            scheduleConfig.getTaskState(), scheduleConfig.getInterval());

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
        this.taskRegistrar = taskRegistrar;

        // Восстанавливаем активные НЕ закрытые задачи из БД
        restoreTasksFromDatabase();
    }

    // Восстановление только активных НЕ закрытых задач
    private void restoreTasksFromDatabase() {
        List<MonitoringTask> activeTasks = taskService.findActiveTasks();
        if (activeTasks.isEmpty()) {
            log.info("Нет активных задач для восстановления");
            return;
        }

        log.info("Восстанавливаем {} активных задач из БД", activeTasks.size());
        for (MonitoringTask dbTask : activeTasks) {
            try {
                TicketsTask ticketsTask = dbTask.toTicketsTask();
                Set<SeatFilter> seatFilters = convertToSeatFilters(dbTask.getSeatFilters());

                Runnable task = createTaskRunnable(ticketsTask, seatFilters);
                TaskScheduler scheduler = taskRegistrar.getScheduler();

                if (scheduler != null) {
                    ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(
                        task, Duration.ofMinutes(dbTask.getIntervalMinutes())
                    );

                    Map<String, TaskInfo> taskMap = scheduledTasks.getOrDefault(ticketsTask.chatId(), new ConcurrentHashMap<>());
                    TaskInfo taskInfo = new TaskInfo(
                        ticketsTask, task, scheduledTask, TaskState.ACTIVE, dbTask.getIntervalMinutes()
                    );
                    taskMap.put(ticketsTask.taskId(), taskInfo);
                    scheduledTasks.putIfAbsent(ticketsTask.chatId(), taskMap);
                }

                log.info("Восстановлена задача: {}", dbTask.getTaskId());
            } catch (Exception e) {
                log.error("Ошибка восстановления задачи {}: {}", dbTask.getTaskId(), e.getMessage());
                taskService.updateTaskState(dbTask.getTaskId(), TaskState.ERROR);
            }
        }
    }

    public void addTask(TicketsTask ticketsTask, Set<SeatFilter> seatFilters) {
        // Сначала сохраняем в БД
        Set<String> filterNames = seatFilters.stream()
            .map(SeatFilter::getButtonText)
            .collect(Collectors.toSet());

        MonitoringTask dbTask = taskService.saveTask(ticketsTask, filterNames, scheduleConfig.getInterval());

        // Затем добавляем в планировщик
        Runnable task = createTaskRunnable(ticketsTask, seatFilters);
        TaskScheduler scheduler = taskRegistrar.getScheduler();

        if (scheduler != null) {
            ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(
                task, Duration.ofMinutes(scheduleConfig.getInterval())
            );

            Map<String, TaskInfo> taskMap = scheduledTasks.getOrDefault(ticketsTask.chatId(), new ConcurrentHashMap<>());
            TaskInfo taskInfo = new TaskInfo(
                ticketsTask, task, scheduledTask, TaskState.ACTIVE, scheduleConfig.getInterval()
            );
            taskMap.put(ticketsTask.taskId(), taskInfo);
            scheduledTasks.putIfAbsent(ticketsTask.chatId(), taskMap);

            log.info("Задача добавлена в планировщик: {}", ticketsTask.taskId());
        }
    }

    /**
     * Закрытие задачи.
     */
    public Boolean removeTask(long chatId, String taskId) {
        // Убираем из планировщика
        boolean removedFromScheduler = removeTaskFromScheduler(chatId, taskId);
        // Закрываем в БД
        boolean closedInDb = taskService.closeTask(taskId);
        log.info("Задача '{}' удалена: планировщик={}, БД={}", taskId, removedFromScheduler, closedInDb);
        return closedInDb;
    }

    // Закрытие всех задач пользователя
    public Integer removeAllTasksByChatId(long chatId) {
        // Убираем из планировщика
        Map<String, TaskInfo> taskMap = scheduledTasks.remove(chatId);
        int schedulerCount = 0;
        if (taskMap != null) {
            for (TaskInfo taskInfo : taskMap.values()) {
                if (taskInfo.getScheduledFuture() != null) {
                    taskInfo.getScheduledFuture().cancel(true);
                    schedulerCount++;
                }
            }
        }
        // Закрываем в БД
        int dbCount = taskService.closeAllTasksForChatId(chatId);
        log.info("Закрыты задачи для chatId={}: планировщик={}, БД={}", chatId, schedulerCount, dbCount);
        return dbCount;
    }

    public Integer removeAllTasks() {
        int count = 0;
        for (Long chatId : scheduledTasks.keySet()) {
            Map<String, TaskInfo> taskMap = scheduledTasks.remove(chatId);
            if (taskMap == null) {
                continue;
            }
            for (TaskInfo taskInfo : taskMap.values()) {
                if (taskInfo.getScheduledFuture() != null) {
                    taskInfo.getScheduledFuture().cancel(true);
                    count++;
                }
            }
        }
        log.info("Задачи удалены из шедулера, count={}", count);
        // Закрываем в БД
        int closedCount = taskService.closeAllTasks();
        log.info("Удалены все незакрытые задачи из БД, closedCount={}", closedCount);
        return closedCount;
    }

    // Вспомогательный метод - удаление только из планировщика
    private boolean removeTaskFromScheduler(long chatId, String taskId) {
        Map<String, TaskInfo> taskMap = scheduledTasks.get(chatId);
        if (taskMap != null) {
            TaskInfo taskInfo = taskMap.remove(taskId);
            if (taskInfo != null) {
                if (taskInfo.getScheduledFuture() != null) {
                    taskInfo.getScheduledFuture().cancel(true);
                }
                if (scheduledTasks.get(chatId).isEmpty()) {
                    scheduledTasks.remove(chatId);
                }
                return true;
            }
        }
        return false;
    }

    private Runnable createTaskRunnable(TicketsTask ticketsTask, Set<SeatFilter> seatFilters) {
        return () -> {
            try {
                taskService.updateTaskExecution(ticketsTask.taskId());
                ticketService.monitoringProcess(ticketsTask, seatFilters);
            } catch (Exception e) {
                log.error("Ошибка выполнения задачи {}: {}", ticketsTask.taskId(), e.getMessage(), e);
                taskService.incrementTaskErrorCount(ticketsTask.taskId(), e.getMessage());
                handleTaskError(ticketsTask.taskId(), e);
            }
        };
    }

    private void handleTaskError(String taskId, Exception e) {
        // Получаем задачу из БД
        taskService.findActiveTaskById(taskId).ifPresent(dbTask -> {
            // Если много ошибок подряд - приостанавливаем задачу
            if (dbTask.getErrorCount() >= 5) {
                log.warn("Задача {} приостановлена из-за множественных ошибок", taskId);
                pauseTask(taskId);
                taskService.updateTaskState(taskId, TaskState.ERROR);
            }
        });
    }

    public void pauseTask(String taskId) {
        // Обновляем состояние в БД
        taskService.updateTaskState(taskId, TaskState.PAUSED);

        // Приостанавливаем в планировщике
        scheduledTasks.values().stream()
            .flatMap(map -> map.values().stream())
            .filter(taskInfo -> taskInfo.getTicketsTask().taskId().equals(taskId))
            .findFirst()
            .ifPresent(taskInfo -> {
                if (taskInfo.getScheduledFuture() != null) {
                    taskInfo.getScheduledFuture().cancel(true);
                    taskInfo.setScheduledFuture(null);
                    taskInfo.setState(TaskState.PAUSED);
                }
            });
    }

    /**
     * Приостанавливает выполнение задачи.
     */
    private void pauseTask(TaskInfo taskInfo) {
        // Обновляем состояние в БД
        taskService.updateTaskState(taskInfo.ticketsTask.taskId(), TaskState.PAUSED);
        if (taskInfo.getScheduledFuture() != null) {
            taskInfo.getScheduledFuture().cancel(true);
            taskInfo.setScheduledFuture(null);
        }
    }

    private Set<SeatFilter> convertToSeatFilters(Set<String> filterNames) {
        return filterNames.stream()
            .map(name -> {
                try {
                    return SeatFilter.getByButtonText(name);
                } catch (IllegalArgumentException e) {
                    log.warn("Неизвестный фильтр: {}", name);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public long changeInterval(long newInterval) {
        for (Map<String, TaskInfo> taskMap : scheduledTasks.values()) {
            for (TaskInfo taskInfo : taskMap.values()) {
                // Обновляем интервал в задаче
                taskInfo.setInterval(newInterval);
                // Перезапускаем только активные задачи
                if (taskInfo.getState() == TaskState.ACTIVE) {
                    restartTask(taskInfo);
                }
            }
        }
        taskService.updateAllActiveTasksInterval(newInterval);
        return scheduledTasks.values().iterator().next().values().iterator().next().getInterval();
    }

    /**
     * Перезапускает выполнение задачи.
     */
    private void restartTask(TaskInfo taskInfo) {
        // Обновляем состояние в БД
        taskService.updateTaskState(taskInfo.ticketsTask.taskId(), TaskState.ACTIVE);

        // Если задача уже имеет future (например, при изменении интервала) - отменяем
        if (taskInfo.getScheduledFuture() != null) {
            taskInfo.getScheduledFuture().cancel(true);
        }
        TaskScheduler scheduler = taskRegistrar.getScheduler();
        if (scheduler != null) {
            ScheduledFuture<?> newFuture = scheduler.scheduleWithFixedDelay(
                taskInfo.getRunnable(),
                Duration.ofMinutes(taskInfo.getInterval())
            );
            taskInfo.setScheduledFuture(newFuture);
        }
    }

    /**
     * Изменяет состояние задач.
     */
    public void changeAllStates(long chatId, TaskState newState) {
        Map<String, TaskInfo> taskInfoMap = scheduledTasks.get(chatId);
        for (TaskInfo taskInfo : taskInfoMap.values()) {
            // Для задач, которые не меняют состояние - пропускаем
            if (taskInfo.getState() == newState) {
                continue;
            }
            switch (newState) {
                case PAUSED ->
                    // Приостанавливаем задачу
                    pauseTask(taskInfo);
                case ACTIVE ->
                    // Перезапускаем задачу
                    restartTask(taskInfo);
            }
            // Обновляем состояние в задаче
            taskInfo.setState(newState);
        }
        log.info("Обновлено состояние задач: {}", newState);
    }
}
