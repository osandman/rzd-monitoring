package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.entity.Station;
import net.osandman.rzdmonitoring.service.printer.ConsolePrinter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static net.osandman.rzdmonitoring.entity.Station.MOSCOW_ALL;
import static net.osandman.rzdmonitoring.entity.Station.PERM_ALL;

@Service
public class RzdMonitoringService {

    private final RequestProcess requestProcess;
    private final ConsolePrinter printer;
    public final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";
    public final static Station START_STATION = MOSCOW_ALL;
    public final static Station FINISH_STATION = PERM_ALL;

    private final static Map<String, String> baseParams = new HashMap<>() {{
        put("dir", "0");
        put("tfl", "3");
        put("checkSeats", "1");
//        put("code0", START_STATION.code());
//        put("code1", FINISH_STATION.code());
    }};

    public RzdMonitoringService(RequestProcess requestProcess, ConsolePrinter printer) {
        this.requestProcess = requestProcess;
        this.printer = printer;
    }

    public String getRoutes(String fromStation, String toStation, LocalDate date) {
        baseParams.put("code0", Station.valueOf(fromStation).code());
        baseParams.put("code1", Station.valueOf(toStation).code());
//        requestProcess.
        return "фиг вам";
    }
}
