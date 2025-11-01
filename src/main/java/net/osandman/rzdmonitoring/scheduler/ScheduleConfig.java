package net.osandman.rzdmonitoring.scheduler;

import lombok.Getter;
import lombok.Setter;
import net.osandman.rzdmonitoring.jpa.entity.TaskState;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметры шедулера.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rzd.scheduler")
public class ScheduleConfig {
    private TaskState taskState;
    private long interval;
}
