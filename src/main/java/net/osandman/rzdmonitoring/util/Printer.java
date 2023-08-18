package net.osandman.rzdmonitoring.util;

import net.osandman.rzdmonitoring.dto.route.RootRoute;
import net.osandman.rzdmonitoring.dto.train.RootTrain;

import java.util.stream.Collectors;

public class Printer {

    private static final String GREEN = "\033[42m";
    private static final String RESET = "\033[0m";

    public static void printRoute(RootRoute rootRoute) {
        rootRoute.tp.stream().collect(Collectors.toMap(tp -> tp.from, tp -> tp))
                .values().forEach(el -> el.list
                        .forEach(route -> System.out.printf("Поезд %s(%s), отправление из %s - %s в %s," +
                                        " прибытие в %s - %s в %s\n",
                                route.number, route.brand,
                                route.station0,
                                route.localDate0 != null ? route.localDate0 : route.date0,
                                route.localTime0 != null ? route.localTime0 : route.time0,
                                route.station1,
                                route.localDate1 != null ? route.localDate1 : route.date1,
                                route.localTime1 != null ? route.localTime1 : route.time1
                        )));
        System.out.println();
    }

    public static void printTickets(RootTrain rootTrain) {
        rootTrain.lst.stream().collect(Collectors.toMap(el -> el.number, el -> el))
                .values().forEach(el -> el.cars.forEach(car -> car.seats
                        .forEach(seat -> System.out.printf("Поезд %s, вагон №%s, тип %s, свободных %s=%s (%s), тариф=%s\n",
                                el.number, car.cnumber, car.type,
                                seat.label.equalsIgnoreCase("нижнее") ? GREEN + seat.label + RESET : seat.label,
                                seat.free, seat.places, seat.tariff))));
        System.out.println();
    }
}
