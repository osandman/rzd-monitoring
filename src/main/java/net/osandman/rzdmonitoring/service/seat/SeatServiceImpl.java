package net.osandman.rzdmonitoring.service.seat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.RestTemplateConnector;
import net.osandman.rzdmonitoring.client.dto.v2.train.RootDto;
import net.osandman.rzdmonitoring.dto.SeatDto;
import net.osandman.rzdmonitoring.dto.TicketsResult;
import net.osandman.rzdmonitoring.dto.TrainDto;
import net.osandman.rzdmonitoring.mapping.TrainMapper;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {

    private final TrainMapper trainMapper;
    private final RestTemplateConnector restTemplateConnector;
    private final Notifier notifier;

    public static final String TRAIN_ICON1 = "\uD83D\uDE86"; // 🚆
    public static final String TRAIN_ICON2 = "\uD83D\uDE89"; // 🚉
    public static final String NOT_FOUND_ICON = "\uD83D\uDE1E"; // 😞

    @Override
    public TicketsResult monitoringProcess(TicketsTask ticketsTask, List<SeatFilter> seatFilters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("service_provider", List.of("B2B_RZD"));
            put("isBonusPurchase", List.of("false"));
        }};
        List<TrainDto> trains = new ArrayList<>();
        for (String routNumber : ticketsTask.routeNumbers()) {
            String body = """
                {
                  "OriginCode": "%s",
                  "DestinationCode": "%s",
                  "Provider": "P1",
                  "DepartureDate": "%s",
                  "TrainNumber": "%s",
                  "SpecialPlacesDemand": "StandardPlacesAndForDisabledPersons",
                  "OnlyFpkBranded": false,
                  "HasPlacesForLargeFamily": false,
                  "CarIssuingType": "All"
                }
                """.formatted(ticketsTask.fromCode(), ticketsTask.toCode(), ticketsTask.date(), routNumber);

            RootDto root;
            try {
                root = restTemplateConnector.callPostRequest(
                    "https://ticket.rzd.ru", "apib2b/p/Railway/V1/Search/CarPricing", params, RootDto.class, body
                );
            } catch (Exception e) {
                String errMsg = "Ошибка при получении данных для поезда %s, '%s'".formatted(routNumber, e.getMessage());
                log.error(errMsg);
                notifier.sendMessage(
                    "Ошибка при получении данных для поезда %s. Если ошибка повторится удалите задачу"
                        .formatted(routNumber),
                    ticketsTask.chatId()
                );
                trains.add(TrainDto.builder().error(errMsg).trainNumber(routNumber).build());
                continue;
            }

            TrainDto trainDto = trainMapper.map(root);
            if (trainDto.getError() != null) {
                log.error("Ошибка при преобразовании json: '{}', задача {}, поезд {}",
                    trainDto.getError(), ticketsTask, routNumber);
                notifier.sendMessage(
                    "Ошибка при преобразовании данных для поезда %s. Если ошибка повторится удалите задачу"
                        .formatted(routNumber),
                    ticketsTask.chatId()
                );
                trains.add(trainDto);
                continue;
            }

            List<SeatDto> filteredSeats = trainDto.getSeats().stream()
                .filter(seatDto ->
                    seatFilters.stream().allMatch(seatFilter -> seatFilter.getPredicate().test(seatDto))
                )
                .toList();
            trainDto.setSeats(filteredSeats);
            trains.add(trainDto);

            if (filteredSeats.isEmpty()) {
                log.info("Не найдено свободных мест, задача {}, поезд {}", ticketsTask, routNumber);
                notifier.sendMessage(
                    "%s для поезда %s[%s-%s] на %s не найдены свободные места"
                        .formatted(
                            NOT_FOUND_ICON,
                            trainDto.getTrainNumber(),
                            trainDto.getFromStation(),
                            trainDto.getToStation(),
                            trainDto.getDateTimeFrom().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        ),
                    ticketsTask.chatId()
                );
                continue;
            }
            log.info("Найдено {} свободных мест, задача {}, поезд {}",
                filteredSeats.size(), ticketsTask, trainDto.getTrainNumber());

            for (SeatDto filteredSeat : filteredSeats) {
                String message = "%s №%s[%s-%s] %s".formatted(
                    TRAIN_ICON1,
                    trainDto.getTrainNumber(),
                    trainDto.getFromStation(),
                    trainDto.getToStation(),
                    filteredSeat.toString()
                );
                notifier.sendMessage(message, ticketsTask.chatId());
            }
        }
        return TicketsResult.builder()
            .findRoutes(((int) trains.stream().filter(trainDto -> !hasText(trainDto.getError())).count()))
            .comment("Поиск свободных мест в поездах [%s] завершен, фильтры поиска: %s"
                .formatted(String.join(", ", ticketsTask.routeNumbers()), seatFilters))
            .trains(trains)
            .build();
    }
}
