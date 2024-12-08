package net.osandman.rzdmonitoring.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметры шедулера.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rzd.scheduler")
public class ScheduleConfig {
    private State state;
    private long interval;
}
