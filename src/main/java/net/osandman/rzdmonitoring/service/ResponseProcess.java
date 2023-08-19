package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.dto.FirstResponse;
import net.osandman.rzdmonitoring.dto.route.RootRoute;
import net.osandman.rzdmonitoring.dto.route.Route;
import net.osandman.rzdmonitoring.dto.route.Tp;
import net.osandman.rzdmonitoring.dto.train.RootTrain;
import net.osandman.rzdmonitoring.entity.Station;
import net.osandman.rzdmonitoring.service.printer.ConsolePrinter;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static net.osandman.rzdmonitoring.entity.Station.MOSCOW_ALL;
import static net.osandman.rzdmonitoring.entity.Station.PERM_ALL;
import static net.osandman.rzdmonitoring.util.Utils.sleep;

@Service
public class ResponseProcess {
    private final RequestProcess requestProcess;
    private final ConsolePrinter printer;
    private final static String END_POINT = "";
    private final Scanner scanner;
    public final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";
    public final static Station START_STATION = MOSCOW_ALL;
    public final static Station FINISH_STATION = PERM_ALL;

    @Autowired
    public ResponseProcess(RequestProcess requestProcess, ConsolePrinter printer) {
        this.requestProcess = requestProcess;
        this.printer = printer;
        this.scanner = new Scanner(System.in);
    }

    private final static Map<String, String> baseParams = new HashMap<>() {{
        put("dir", "0");
        put("tfl", "3");
        put("checkSeats", "1");
//        put("md", "0");
        put("code0", START_STATION.code());
        put("code1", FINISH_STATION.code());
    }};

    public void autoLoop(String... findRoute) {
        System.out.printf("Маршрут %s - %s\n", START_STATION, FINISH_STATION);
        System.out.println("Введите дату отправления в формате " + DATE_FORMAT_PATTERN);
        String date = scanner.nextLine();
        while (true) {
            System.out.printf("Ищем билеты для поездов: %s\n",
                    findRoute.length == 0 ? "всех маршрутов" : Arrays.asList(findRoute));
            baseParams.put("dt0", date);
            RootRoute rootRoute = getRootRoute();
            findTickets(rootRoute, findRoute);
            int pause = 1000 * 60;
            System.out.println("Пауза ..." + pause / 1000 + " cекунд");
            sleep(pause);
        }
    }

    public void loopWithInput(Scanner scanner) {
        String date;
        while (!Objects.equals(date = scanner.nextLine(), "q")) {
            try {
                LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
                baseParams.put("dt0", date);
                RootRoute rootRoute = getRootRoute();
                findTickets(rootRoute);
                System.out.println("Введите дату отправления");
            } catch (DateTimeParseException e) {
                System.out.println("Введите правильную дату");
            }
        }
    }

    private void findTickets(RootRoute rootRoute, String... routeNumber) {
        if (rootRoute == null) {
            System.out.println("rootRoute is null");
            return;
        }
        if (rootRoute.tp == null) {
            System.out.println("rootRoute.tp is null");
            System.out.println(rootRoute);
            return;
        }
        baseParams.put("layer_id", "5764");
        for (Tp tp : rootRoute.tp) {
            for (Route route : tp.list) {
                if (routeNumber.length != 0 && Arrays.asList(routeNumber).contains(route.number)) {
                    baseParams.put("tnum0", route.number);
                    System.out.println("Ищем свободные места для поезда " + route.number);
                    RootTrain rootTrain = JsonParser.parse(getBodyFromResponse(), RootTrain.class);
                    if (rootTrain == null) {
                        System.out.println("Поезд " + route.number + " не обработан\n");
                        continue;
                    }
                    printer.printTickets(rootTrain);
                }
            }
        }
    }

    private RootRoute getRootRoute() {
        baseParams.put("layer_id", "5827");
        RootRoute rootRoute = JsonParser.parse(getBodyFromResponse(), RootRoute.class);
        if (rootRoute != null) {
            printer.printRoute(rootRoute);
            return rootRoute;
        }
        return new RootRoute();
    }

    private String getBodyFromResponse() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(baseParams);

        sleep(500);
        FirstResponse firstResponse = requestProcess.callGetRequest(END_POINT, params, FirstResponse.class);
        System.out.println(firstResponse);

        sleep(1000);
        params.clear();
        params.add("layer_id", baseParams.get("layer_id"));
        params.add("rid", String.valueOf(firstResponse.RID));
        return requestProcess.callGetRequest(END_POINT, params);
    }
}
