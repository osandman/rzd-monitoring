package net.osandman.rzdmonitoring;

import net.osandman.rzdmonitoring.service.ResponseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RzdMonitoringApplication implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RzdMonitoringApplication.class);

    @Autowired
    private ResponseProcess responseProcess;

    public static void main(String[] args) {
        SpringApplication.run(RzdMonitoringApplication.class, args);
        logger.info("Привет! Успешный запуск приложения {}", logger.getName());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        responseProcess.autoLoop("294Ж", "070Ч");
//        responseProcess.autoLoop();
    }
}
