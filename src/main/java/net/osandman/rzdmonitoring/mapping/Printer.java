package net.osandman.rzdmonitoring.mapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.dto.SeatDto;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class Printer {

    private final Notifier notifier;
    private static final String GREEN = "\033[42m";
    private static final String RESET = "\033[0m";

    public void ticketsMapping(RootTrain rootTrain) {
        List<SeatDto> findSeats = new LinkedList<>();
        Map<String, String> trainParams = new LinkedHashMap<>();
        try {
            rootTrain.lst.stream().collect(Collectors.toMap(el -> el.number, el -> el))
                .values().forEach(train -> train.cars.forEach(car -> car.seats
                    .forEach(seat -> {
                        String seatLabel = seat.label;
                        if (!seat.label.toLowerCase().contains("верх") && !car.type.toLowerCase().contains("люкс")) {
//                        if (!car.type.contains("Люкс") && train.number.contains("286")) {
                            trainParams.put("поезд №", train.number);
                            trainParams.put("от ", train.station0);
                            trainParams.put("до ", train.station1);
                            trainParams.put("дата: ", Objects.requireNonNullElse(train.localDate0, train.date0));
                            trainParams.put("время: ", Objects.requireNonNullElse(train.localTime0, train.time0));
//                                Beeper.Beep();
                            seatLabel = GREEN + seat.label + RESET;
                            findSeats.add(
                                new SeatDto(car.cnumber, car.type, seat.label, seat.places, seat.tariff, seat.free)
                            );
                        }
                        log.info("Поезд {}, вагон №{}, тип {}, свободных {}={} ({}), тариф={}",
                            train.number, car.cnumber, car.type,
                            seatLabel, seat.free, seat.places, seat.tariff);
                    })));
            if (!findSeats.isEmpty()) {
                sendNotify(findSeats, trainParams);
            }
        } catch (Exception e) {
            log.error("Ошибка во время разбора маршрута поезда {}", rootTrain, e);
        }
        log.info("---the-end---");
    }

    private void sendNotify(List<SeatDto> findSeats, Map<String, String> trainParams) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(", ",
                trainParams.entrySet().stream()
                    .map(entry -> entry.getKey() + entry.getValue()).toList()))
            .append("\n");
        findSeats.forEach(car -> builder.append(car).append("\n"));
        notifier.sendMessage(builder.toString());
    }
}
