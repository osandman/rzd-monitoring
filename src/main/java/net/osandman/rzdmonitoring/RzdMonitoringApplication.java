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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

@SpringBootApplication
public class RzdMonitoringApplication {
    private static RestResponseParser restResponseParser;
    private final static String END_POINT = "";

    private static final Map<String, String> baseParams = new HashMap<>() {{
        put("dir", "0");
        put("tfl", "3");
        put("checkSeats", "1");
        put("code0", "2030400");
        put("code1", "2000002");
        put("dt0", "25.07.2023");
        put("md", "0");
    }};

    public RzdMonitoringApplication(RestResponseParser restResponseParser) {
        RzdMonitoringApplication.restResponseParser = restResponseParser;
    }

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run(RzdMonitoringApplication.class, args);

        Scanner scanner = new Scanner(System.in);
        String data = "";
        System.out.println("Введите дату отправления");
        while (!Objects.equals(data = scanner.nextLine(), "q")) {
            baseParams.put("dt0", data);
            RootRoute rootRoute = getRootRoute();
            findTickets(rootRoute);
            System.out.println("Введите дату отправления");
        }
        System.out.println("До свидания");
    }

    private static void findTickets(RootRoute rootRoute) {
        baseParams.put("layer_id", "5764");
        for (Tp tp : rootRoute.tp) {
            for (Route route : tp.list) {
                baseParams.put("tnum0", route.number);
                System.out.println("Ищем для поезда " + route.number);
                RootTrain rootTrain = JsonParser.parse(getBodyFromResponse(baseParams), RootTrain.class);
                Printer.printTickets(rootTrain);
            }
        }
    }

    private static RootRoute getRootRoute() {
        baseParams.put("layer_id", "5827");
        RootRoute rootRoute = JsonParser.parse(getBodyFromResponse(baseParams), RootRoute.class);
        Printer.printRoute(rootRoute);
        return rootRoute;
    }

    private static String getBodyFromResponse(Map<String, String> routeMap) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(routeMap);

        sleep(1000);
        FirstResponse firstResponse = restResponseParser.callSecondGetRequest(END_POINT, params, FirstResponse.class);
        System.out.println(firstResponse);

        sleep(1000);
        params.clear();
        params.add("layer_id", routeMap.get("layer_id"));
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
