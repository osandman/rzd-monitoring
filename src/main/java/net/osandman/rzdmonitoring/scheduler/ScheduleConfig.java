package net.osandman.rzdmonitoring.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Параметры шедулера.
 */
@Configuration
@Getter
@Setter
public class ScheduleConfig {

    @Value("${scheduler.state}")
    private State state;

    @Value("${scheduler.interval}")
    private long interval;
}
