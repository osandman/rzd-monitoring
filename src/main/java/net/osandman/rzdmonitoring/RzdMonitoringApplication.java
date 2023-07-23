package net.osandman.rzdmonitoring;

import net.osandman.rzdmonitoring.dto.FirstResponse;
import net.osandman.rzdmonitoring.dto.route.RootRoute;
import net.osandman.rzdmonitoring.dto.route.Route;
import net.osandman.rzdmonitoring.dto.route.Tp;
import net.osandman.rzdmonitoring.dto.train.RootTrain;
import net.osandman.rzdmonitoring.service.RestResponseParser;
import net.osandman.rzdmonitoring.util.JsonParser;
import net.osandman.rzdmonitoring.util.Printer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

@SpringBootApplication
public class RzdMonitoringApplication {
    private static RestResponseParser restResponseParser;
    private final static String END_POINT = "";
    private final static String PERM_2 = "2030400";
    private final static String MOSCOW_YAR = "2000002";
    private final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    private static final Map<String, String> baseParams = new HashMap<>() {{
        put("dir", "0");
        put("tfl", "3");
        put("checkSeats", "1");
//        put("md", "0");
        put("code0", PERM_2);
        put("code1", MOSCOW_YAR);
    }};

    public RzdMonitoringApplication(RestResponseParser restResponseParser) {
        RzdMonitoringApplication.restResponseParser = restResponseParser;
    }

    public static void main(String[] args) {

        SpringApplication.run(RzdMonitoringApplication.class, args);

        Scanner scanner = new Scanner(System.in);
        String data = "";
        System.out.println("Введите дату отправления в формате " + DATE_FORMAT_PATTERN);
        while (!Objects.equals(data = scanner.nextLine(), "q")) {
            try {
                LocalDate.parse(data, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
                baseParams.put("dt0", data);
                RootRoute rootRoute = getRootRoute();
                findTickets(rootRoute);
                System.out.println("Введите дату отправления");
            } catch (DateTimeParseException e) {
                System.out.println("Введите правильную дату");
            }
        }
        System.out.println("До свидания");
    }

    private static void findTickets(RootRoute rootRoute) {
        baseParams.put("layer_id", "5764");
        for (Tp tp : rootRoute.tp) {
            for (Route route : tp.list) {
                baseParams.put("tnum0", route.number);
                System.out.println("Ищем свободные места для поезда " + route.number);
                RootTrain rootTrain = JsonParser.parse(getBodyFromResponse(), RootTrain.class);
                if (rootTrain == null) {
                    System.out.println("Поезд " + route.number + " не обработан\n");
                    continue;
                }
                Printer.printTickets(rootTrain);
            }
        }
    }

    private static RootRoute getRootRoute() {
        baseParams.put("layer_id", "5827");
        RootRoute rootRoute = JsonParser.parse(getBodyFromResponse(), RootRoute.class);
        if (rootRoute != null) {
            Printer.printRoute(rootRoute);
            return rootRoute;
        }
        return new RootRoute();
    }

    private static String getBodyFromResponse() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(baseParams);

        sleep(100);
        FirstResponse firstResponse = restResponseParser.callSecondGetRequest(END_POINT, params, FirstResponse.class);
        System.out.println(firstResponse);

        sleep(1000);
        params.clear();
        params.add("layer_id", baseParams.get("layer_id"));
        params.add("rid", String.valueOf(firstResponse.RID));
        return restResponseParser.callSecondGetRequest(END_POINT, params);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
