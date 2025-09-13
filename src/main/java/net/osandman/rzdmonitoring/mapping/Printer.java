package net.osandman.rzdmonitoring.mapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.train.Lst;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.dto.SeatDto;
import net.osandman.rzdmonitoring.dto.TrainDto;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.osandman.rzdmonitoring.service.seat.SeatServiceImpl.TRAIN_ICON1;

@Component
@Slf4j
@RequiredArgsConstructor
public class Printer {

    private final Notifier notifier;
    private static final String GREEN = "\033[42m";
    private static final String RESET = "\033[0m";

    public TrainDto ticketsMapping(RootTrain rootTrain, Long chatId) {
        List<SeatDto> findSeats = new LinkedList<>();
        Map<String, String> trainParams = new LinkedHashMap<>();
        TrainDto trainDto = new TrainDto();
        try {
            rootTrain.lst.stream().collect(Collectors.toMap(el -> el.number, el -> el))
                .values().forEach(train -> train.cars.forEach(car -> car.seats
                    .forEach(seat -> {
                        String seatLabel = seat.label;
                        fill(train, trainParams, trainDto);
                        if (!seat.label.toLowerCase().contains("верх") && !car.type.toLowerCase().contains("люкс")) {
//                        if (!car.type.contains("Люкс") && train.number.contains("286")) {
                            findSeats.add(
                                new SeatDto(car.cnumber, car.type, seat.label, seat.places, seat.tariff, seat.free)
                            );
                            seatLabel = GREEN + seat.label + RESET;
                        }
                        log.info("Поезд {}, вагон №{}, тип {}, свободных {}={} ({}), тариф={}",
                            train.number, car.cnumber, car.type,
                            seatLabel, seat.free, seat.places, seat.tariff);
                    })));
            if (!findSeats.isEmpty()) {
                trainDto.setSeats(findSeats);
                sendNotify(findSeats, trainParams, chatId);
            }
        } catch (Exception e) {
            log.error("Ошибка во время разбора маршрута поезда {}, '{}'", rootTrain, e.getMessage());
        }
        log.info("---the-end---");
        return trainDto;
    }

    private static void fill(
        Lst train, Map<String, String> trainParams, TrainDto trainDto
    ) {
        trainDto.setTrainNumber(train.number);
        trainDto.setFromStation(train.station0);
        trainDto.setToStation(train.station1);
        trainDto.setDateTimeFrom(
            LocalDateTime.parse(
                Objects.requireNonNullElse(train.localDate0, train.date0) + " " + Objects.requireNonNullElse(train.localTime0, train.time0),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            ));
        trainDto.setDateTimeTo(
            LocalDateTime.parse(
                Objects.requireNonNullElse(train.localDate1, train.date1) + " " + Objects.requireNonNullElse(train.localTime1, train.time1),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            ));
        trainParams.put(TRAIN_ICON1 + " ", train.number);
        trainParams.put("от ", train.station0);
        trainParams.put("до ", train.station1);
        trainParams.put("дата: ", Objects.requireNonNullElse(train.localDate0, train.date0));
        trainParams.put("время: ", Objects.requireNonNullElse(train.localTime0, train.time0));
    }

    private void sendNotify(List<SeatDto> findSeats, Map<String, String> trainParams, Long chatId) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(" ",
                trainParams.entrySet().stream()
                    .map(entry -> entry.getKey() + entry.getValue()).toList()))
            .append("\n");
        findSeats.forEach(car -> builder.append(car).append("\n"));
        notifier.sendMessage(builder.toString(), chatId);
    }
}
