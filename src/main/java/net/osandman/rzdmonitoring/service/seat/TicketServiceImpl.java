package net.osandman.rzdmonitoring.service.seat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.RestTemplateConnector;
import net.osandman.rzdmonitoring.client.dto.v2.train.RootTrainDto;
import net.osandman.rzdmonitoring.dto.train.SeatDto;
import net.osandman.rzdmonitoring.dto.train.TicketsResult;
import net.osandman.rzdmonitoring.dto.train.TrainDto;
import net.osandman.rzdmonitoring.mapping.TrainMapper;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TrainMapper trainMapper;
    private final RestTemplateConnector restTemplateConnector;
    private final Notifier notifier;
    private final ObjectMapper objectMapper;
    @Lazy
    private final MultiTaskScheduler taskScheduler;

    public static final String TRAIN_ICON1 = "\uD83D\uDE86"; // 🚆
    public static final String TRAIN_ICON2 = "\uD83D\uDE89"; // 🚉
    public static final String NOT_FOUND_ICON = "\uD83D\uDE1E"; // 😞

    @Override
    public TicketsResult monitoringProcess(TicketsTask ticketsTask, Set<SeatFilter> seatFilters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("service_provider", List.of("B2B_RZD"));
            put("isBonusPurchase", List.of("false"));
        }};
        List<TrainDto> trains = new ArrayList<>();
        int errorCountForDate = 0;
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

            RootTrainDto root;
            try {
                root = restTemplateConnector.callPostRequest(
                    "https://ticket.rzd.ru", "apib2b/p/Railway/V1/Search/CarPricing", params, RootTrainDto.class, body
                );
            } catch (Exception e) {
                log.error("Ошибка при получении данных для поезда {}, '{}'", routNumber, e.getMessage());
                String errMsg = extractErrorMessageFromException(e);
                trains.add(TrainDto.builder()
                    .error(errMsg)
                    .trainNumber(routNumber)
                    .build());
                if (errMsg.contains("мест нет")) {
                    continue;
                }
                String userMessage;
                if (errMsg.contains("Некорректное значение") && errMsg.contains("DepartureDate")) {
                    errorCountForDate++;
                    userMessage = "❌ Некорректная дата отправления поезда %s".formatted(routNumber);
                    if (ticketsTask.routeNumbers().length == errorCountForDate) {
                        taskScheduler.removeTask(ticketsTask.chatId(), ticketsTask.taskId());
                        userMessage = userMessage + ". Задача '%s' удалена".formatted(ticketsTask.taskId());
                    }
                } else {
                    userMessage = ("❌ Ошибка при получении данных для поезда %s: '%s'.\n"
                                   + "Если ошибка повторится, то удалите задачу").formatted(routNumber, errMsg);
                }
                notifier.sendMessage(userMessage, ticketsTask.chatId());
                continue;
            }

            TrainDto trainDto = trainMapper.map(root);
            if (trainDto.getError() != null) {
                log.error("Ошибка при преобразовании json: '{}', задача {}, поезд {}",
                    trainDto.getError(), ticketsTask, routNumber);
                notifier.sendMessage(
                    "Ошибка при преобразовании данных для поезда %s. Если ошибка повторится, то удалите задачу"
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
                continue;
            }
            log.info("Найдено {} свободных мест, задача {}, поезд {}",
                filteredSeats.size(), ticketsTask, trainDto.getTrainNumber());

            for (SeatDto filteredSeat : filteredSeats) {
                String message = "%s №%s[%s-%s] %s".formatted(
                    TRAIN_ICON2,
                    trainDto.getTrainNumber(),
                    trainDto.getFromStation(),
                    trainDto.getToStation(),
                    filteredSeat.toString()
                );
                notifier.sendMessage(message, ticketsTask.chatId());
            }
        }
        return TicketsResult.builder()
            .successTrainCount(
                ((int) trains.stream().filter(trainDto -> !hasText(trainDto.getError())).count())
            )
            .comment("Поиск свободных мест в поездах [%s] завершен, фильтры поиска: %s"
                .formatted(String.join(", ", ticketsTask.routeNumbers()), seatFilters))
            .trains(trains)
            .build();
    }

    private String extractErrorMessageFromException(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "";
        // Если это HttpStatusCodeException, попробуем извлечь JSON из тела ответа
        if (e instanceof HttpStatusCodeException httpException) {
            String responseBody = httpException.getResponseBodyAsString();
            try {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                // Пытаемся извлечь сообщение об ошибке из JSON
                if (rootNode.has("Message")) {
                    return rootNode.get("Message").asText();
                } else if (rootNode.has("ProviderError")) {
                    return rootNode.get("ProviderError").asText();
                }
            } catch (IOException ex) {
                // Если не удалось распарсить JSON, вернем оригинальное сообщение
                log.warn("Не удалось распарсить тело ошибки: {}", responseBody);
            }
        }
        return errorMessage;
    }
}
