package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.route.Route;
import net.osandman.rzdmonitoring.client.dto.route.Tp;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.mapping.RouteMapper;
import net.osandman.rzdmonitoring.repository.StationEnum;
import net.osandman.rzdmonitoring.mapping.Printer;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static net.osandman.rzdmonitoring.repository.StationEnum.MOSCOW_ALL;
import static net.osandman.rzdmonitoring.repository.StationEnum.PERM_ALL;
import static net.osandman.rzdmonitoring.util.Utils.sleep;

@Service
@Deprecated
public class ResponseProcess {
    private final RequestProcess requestProcess;
    private final RouteMapper routeMapper;
    private final Printer printer;
    private final static String END_POINT = "";
    private final Scanner scanner;
    public final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";
    public final static StationEnum START_STATION = MOSCOW_ALL;
    public final static StationEnum FINISH_STATION = PERM_ALL;

    @Autowired
    public ResponseProcess(RequestProcess requestProcess, RouteMapper routeMapper, Printer printer) {
        this.requestProcess = requestProcess;
        this.routeMapper = routeMapper;
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
            baseParams.put("dt0", date);
            System.out.printf("Ищем билеты для поездов: %s\n",
                    findRoute.length == 0 ? "всех маршрутов" : Arrays.asList(findRoute));
            RootRoute rootRoute = getRootRoute();
            if (findRoute.length == 0) {
                findRoute = rootRoute.getTp()
                        .stream().flatMap(route -> route.list.stream().map(r -> r.number)).toArray(String[]::new);
            }
            findTickets(rootRoute, findRoute);
            int pause = 1000 * 300;
            System.out.println("Пауза ..." + pause / 1000 / 60 + " минут");
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
        baseParams.put("layer_id", LayerId.DETAIL_ID.value);
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
                    printer.ticketsMapping(rootTrain);
                }
            }
        }
    }

    private RootRoute getRootRoute() {
        baseParams.put("layer_id", "5827");
        RootRoute rootRoute = JsonParser.parse(getBodyFromResponse(), RootRoute.class);
        if (rootRoute != null) {
            routeMapper.mapping(rootRoute);
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
