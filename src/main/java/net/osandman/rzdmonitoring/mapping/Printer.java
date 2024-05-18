package net.osandman.rzdmonitoring.mapping;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Printer {

    private final Notifier notifier;

    private static final String GREEN = "\033[42m";
    private static final String RESET = "\033[0m";

    public Printer(Notifier notifier) {
        this.notifier = notifier;
    }

    public void ticketsMapping(RootTrain rootTrain) {
        Map<String, Integer> findBottomSeat = new LinkedHashMap<>();
        Map<String, String> trainParams = new LinkedHashMap<>();
        try {
            rootTrain.lst.stream().collect(Collectors.toMap(el -> el.number, el -> el))
                .values().forEach(train -> train.cars.forEach(car -> car.seats
                    .forEach(seat -> {
                        String seatLabel = seat.label;
                        if (seat.label.equalsIgnoreCase("нижнее") && !car.type.contains("Люкс")) {
                            trainParams.put("поезд №", train.number);
                            trainParams.put("от ", train.station0);
                            trainParams.put("до ", train.station1);
                            trainParams.put("дата: ", Objects.requireNonNullElse(train.localDate0, train.date0));
                            trainParams.put("время: ", Objects.requireNonNullElse(train.localTime0, train.time0));
//                                Beeper.Beep();
                            seatLabel = GREEN + seat.label + RESET;
                            findBottomSeat.put(car.cnumber + " (тариф=" + seat.tariff + ")", seat.free);
                        }
                        log.info("Поезд {}, вагон №{}, тип {}, свободных {}={} ({}), тариф={}",
                            train.number, car.cnumber, car.type,
                            seatLabel,
                            seat.free, seat.places, seat.tariff);
                    })));
            if (!findBottomSeat.isEmpty()) {
                sendNotifier(findBottomSeat, trainParams);
            }
        } catch (Exception e) {
            log.error("Ошибка во время разбора маршрута поезда {}", rootTrain, e);
        }
        log.info("---the-end---");
    }

    private void sendNotifier(Map<String, Integer> findBottomSeat, Map<String, String> trainParams) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(", ",
                trainParams.entrySet().stream()
                    .map(entry -> entry.getKey() + entry.getValue()).toList()))
            .append("\n");
        findBottomSeat.forEach((car, count) -> builder
            .append("вагон: ").append(car)
            .append(", нижних мест: ").append(count)
            .append("\n"));
        notifier.sendMessage(builder.toString());
    }
}
