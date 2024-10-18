package net.osandman.rzdmonitoring;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.repository.StationEnum;
import net.osandman.rzdmonitoring.service.TicketService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class RzdMonitoringApplication implements ApplicationRunner {

    public final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";
    private final TicketService ticketService;

    public RzdMonitoringApplication(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public static void main(String[] args) {
        log.info("Привет! Успешный запуск приложения {}", log.getName());
        SpringApplication.run(RzdMonitoringApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
//        scanTickets();
    }

    private void scanTickets() {
        Scanner scanner = new Scanner(System.in);
        String date;
        log.info("Введите дату отправления в формате '{}' или 'q' для выхода", DATE_FORMAT_PATTERN);
        while (!(date = scanner.nextLine()).equalsIgnoreCase("q")) {
            try {
                LocalDate parse = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
                if (!parse.isBefore(LocalDate.now())) {
                    StationEnum from = StationEnum.PERM_ALL;
                    StationEnum to = StationEnum.MOSCOW_ALL;
                    log.info("Маршрут {} - {}", from, to);
//        ticketService.autoLoop(date, "091И", "001Э", "011Е", "077Ы", "009Н");
                    ticketService.autoLoop(date, from.code(), to.code(), "011Е", "077Ы", "009Н");
//        ticketService.autoLoop(date);
                }
            } catch (Exception e) {
                log.error("Некорректный ввод");
            }
            log.info("Введите дату отправления в формате '{}' или 'q' для выхода", DATE_FORMAT_PATTERN);
        }
        log.info("Вы вышли из цикла поиска билетов");
    }
}
