package net.osandman.rzdmonitoring.scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
    private final Map<Long, Map<String, ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>();
    private ScheduledTaskRegistrar taskRegistrar;

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
            ScheduledFuture<?> scheduledTask =
                scheduler.scheduleWithFixedDelay(task, Duration.ofMinutes(scheduleConfig.getInterval()));
            Map<String, ScheduledFuture<?>> taskMap =
                scheduledTasks.getOrDefault(ticketsTask.chatId(), new ConcurrentHashMap<>());
            taskMap.put(ticketsTask.taskId(), scheduledTask);
            scheduledTasks.putIfAbsent(ticketsTask.chatId(), taskMap);
        }
        return result;
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
        Map<String, ScheduledFuture<?>> taskMap = scheduledTasks.get(chatId);
        if (taskMap == null) {
            return null;
        }
        ScheduledFuture<?> scheduledTask = taskMap.remove(taskId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            return true;
        }
        return false;
    }

    public Integer removeTasks(long chatId) {
        Map<String, ScheduledFuture<?>> taskMap = scheduledTasks.remove(chatId);
        if (taskMap == null) {
            return null;
        }
        for (ScheduledFuture<?> scheduledFuture : taskMap.values()) {
            scheduledFuture.cancel(false);
        }
        return taskMap.size();
    }
}
