package net.osandman.rzdmonitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class RzdMonitoringApplication {

    public static void main(String[] args) {
        log.info("Привет! Успешный запуск приложения {}", log.getName());
        SpringApplication.run(RzdMonitoringApplication.class, args);
    }
}
