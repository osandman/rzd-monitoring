package net.osandman.rzdmonitoring.util;

import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class JsonParserTest {

    @Test
    void parseRoutes() throws IOException {
        RootRoute rootRoute = JsonParser.parse(requireNonNull(JsonParserTest.class.getClassLoader()
                .getResource("json/routes.json")).openStream(),
            RootRoute.class);
        rootRoute.tp.stream().collect(Collectors.toMap(tp -> tp.from, tp -> tp))
            .values().forEach(el -> el.list
                .forEach(route -> System.out.printf("номер поезда %s, маршрут %s\n", route.number,
                    route.route0 + " " + route.route1)));
    }

    @Test
    void parseTrain() throws IOException {
        RootTrain rootTrain = JsonParser.parse(requireNonNull(JsonParserTest.class.getClassLoader()
                .getResource("json/tickets061SH.json")).openStream(),
            RootTrain.class);
        rootTrain.lst.stream().collect(Collectors.toMap(el -> el.number, el -> el))
            .values().forEach(el -> el.cars.forEach(car -> car.seats
                .forEach(seat -> System.out.printf("Вагон №%s, тип %s, свободных %s=%s (%s) \n",
                    car.cnumber, car.type, seat.label, seat.free, seat.places))));
    }
}