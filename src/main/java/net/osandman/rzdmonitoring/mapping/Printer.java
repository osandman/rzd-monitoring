package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Printer {

    @Autowired
    Notifier notifier;

    private static final String GREEN = "\033[42m";
    private static final String RESET = "\033[0m";

    public void ticketsMapping(RootTrain rootTrain) {
        Map<String, Integer> findBottomSeat = new HashMap<>();
        Map<String, String> trainParams = new HashMap<>();
        rootTrain.lst.stream().collect(Collectors.toMap(el -> el.number, el -> el))
                .values().forEach(train -> train.cars.forEach(car -> car.seats
                        .forEach(seat -> {
                            String seatLabel = seat.label;
                            if (seat.label.equalsIgnoreCase("нижнее")) {
                                trainParams.put("поезд №", train.number);
                                trainParams.put("дата: ", train.date0);
                                trainParams.put("время: ", train.time0);
//                                Beeper.Beep();
                                seatLabel = GREEN + seat.label + RESET;
                                findBottomSeat.put(car.cnumber, seat.free);
                            }
                            System.out.printf("Поезд %s, вагон №%s, тип %s, свободных %s=%s (%s), тариф=%s\n",
                                    train.number, car.cnumber, car.type,
                                    seatLabel,
                                    seat.free, seat.places, seat.tariff);
                        })));
        if (!findBottomSeat.isEmpty()) {
            sendNotifier(findBottomSeat, trainParams);
        }
        System.out.println();
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
