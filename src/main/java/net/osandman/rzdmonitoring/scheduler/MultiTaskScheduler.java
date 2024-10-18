package net.osandman.rzdmonitoring.scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Getter
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private ScheduledTaskRegistrar taskRegistrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.taskRegistrar = taskRegistrar;
        log.info("Установлены параметры шедулера запуска мониторинга билетов: state='{}', interval='{}' минут",
            scheduleConfig.getState(),
            scheduleConfig.getInterval());
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);  // Задайте размер пула в зависимости от ваших нужд
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    public void addTask(String taskId, String date, String fromCode, String toCode, String... trainNumbers) {
        removeTask(taskId);  // Удаляем задачу, если она уже существует
        Runnable task = () -> ticketService.process(taskId, date, fromCode, toCode, trainNumbers);
        TaskScheduler scheduler = taskRegistrar.getScheduler();
        if (scheduler != null) {
            ScheduledFuture<?> scheduledTask = scheduler
                .scheduleWithFixedDelay(task, Duration.ofMinutes(scheduleConfig.getInterval()));
            scheduledTasks.put(taskId, scheduledTask);
        }
    }

    public void removeTask(String taskId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(taskId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }

    public void updateTaskInterval(String taskId, String date, String fromCode, String toCode, String... trainNumbers) {
        addTask(taskId, date, fromCode, toCode, trainNumbers);
    }
}
