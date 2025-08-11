package net.osandman.rzdmonitoring.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.route.Tp;
import net.osandman.rzdmonitoring.dto.Result;
import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.TicketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MultiTaskScheduler implements SchedulingConfigurer {

    private final ScheduleConfig scheduleConfig;
    private final TicketService ticketService;
    private final RouteService routeService;
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
        private State state;
        private long interval;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("Установлены параметры шедулера запуска мониторинга билетов: state='{}', interval='{}' минут",
            scheduleConfig.getState(),
            scheduleConfig.getInterval());
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);  // Задайте размер пула в зависимости от ваших нужд
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
        this.taskRegistrar = taskRegistrar;
    }

    public Result addTask(TicketsTask ticketsTask) {
        // предварительная проверка, что заданный маршрут существует
        RootRoute rootRoute =
            routeService.findRootRoute(ticketsTask.fromCode(), ticketsTask.toCode(), ticketsTask.date());
        Result result = checkRoute(rootRoute, ticketsTask.taskId());
        if (!result.success()) {
            return result;
        }
        removeTask(ticketsTask.chatId(), ticketsTask.taskId());  // Удаляем задачу, если она уже существует
        Runnable task = () -> ticketService.process(ticketsTask);
        TaskScheduler scheduler = taskRegistrar.getScheduler();
        if (scheduler != null) {
            ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(
                task, Duration.ofMinutes(scheduleConfig.getInterval())
            );
            Map<String, TaskInfo> taskMap =
                scheduledTasks.getOrDefault(ticketsTask.chatId(), new ConcurrentHashMap<>());
            TaskInfo taskInfo = new TaskInfo(
                ticketsTask,
                task,
                scheduledTask,
                State.ACTIVE,
                scheduleConfig.getInterval()
            );
            taskMap.put(ticketsTask.taskId(), taskInfo);
            scheduledTasks.putIfAbsent(ticketsTask.chatId(), taskMap);
        }
        return result;
    }

    public void changeInterval(long newInterval) {
        for (Map<String, TaskInfo> taskMap : scheduledTasks.values()) {
            for (TaskInfo taskInfo : taskMap.values()) {
                // Обновляем интервал в задаче
                taskInfo.setInterval(newInterval);
                // Перезапускаем только активные задачи
                if (taskInfo.getState() == State.ACTIVE) {
                    restartTask(taskInfo);
                }
            }
        }
        // Обновляем глобальную конфигурацию
        scheduleConfig.setInterval(newInterval);
        log.info("Обновлен интервал выполнения задач: {} минут", newInterval);
    }

    /**
     * Изменяет состояние задач.
     */
    public void changeState(State newState) {
        for (Map<String, TaskInfo> taskMap : scheduledTasks.values()) {
            for (TaskInfo taskInfo : taskMap.values()) {
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
        }
        // Обновляем глобальную конфигурацию
        scheduleConfig.setState(newState);
        log.info("Обновлено состояние задач: {}", newState);
    }

    /**
     * Приостанавливает выполнение задачи.
     */
    private void pauseTask(TaskInfo taskInfo) {
        if (taskInfo.getScheduledFuture() != null) {
            taskInfo.getScheduledFuture().cancel(true);
            taskInfo.setScheduledFuture(null);
        }
    }

    /**
     * Перезапускает выполнение задачи.
     */
    private void restartTask(TaskInfo taskInfo) {
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

    private static Result checkRoute(RootRoute rootRoute, String taskId) {
        // проверка того, что маршрут существует, только после этого добавлять задание
        if (rootRoute == null || rootRoute.result.toLowerCase().contains("fail")) { // || rootRoute.tp.get(0).list.isEmpty()
            return new Result(false, "Не найден маршрут или ошибка запроса");
        }
        if (!rootRoute.tp.isEmpty()) {
            Tp tp = rootRoute.tp.get(0);
            long countType = tp.list.stream()
                .filter(route -> route.type == 1) // 1 - пригородные, 0 - междугородние
                .count();
            if (countType >= tp.list.size()) {
                return new Result(false, "Найдены только пригородные поезда");
            }
            long countNotTrain = tp.msgList.stream()
                .filter(map -> map.get("message").toLowerCase().contains("в указанную дату поезд не ходит"))
                .count();
            if (countNotTrain >= tp.list.size()) {
                return new Result(false, "В указанную дату поезд не ходит");
            }
        }
        return new Result(true, taskId);
    }

    public Boolean removeTask(long chatId, String taskId) {
        Map<String, TaskInfo> taskMap = scheduledTasks.get(chatId);
        if (taskMap == null) {
            return null;
        }
        TaskInfo taskInfo = taskMap.remove(taskId);
        if (taskInfo != null) {
            if (taskInfo.getScheduledFuture() != null) {
                taskInfo.getScheduledFuture().cancel(true);
            }
            return true;
        }
        return false;
    }

    public Integer removeAllTasks(long chatId) {
        Map<String, TaskInfo> taskMap = scheduledTasks.remove(chatId);
        if (taskMap == null) {
            return null;
        }
        int count = 0;
        for (TaskInfo taskInfo : taskMap.values()) {
            if (taskInfo.getScheduledFuture() != null) {
                taskInfo.getScheduledFuture().cancel(true);
                count++;
            }
        }
        return count;
    }
}
