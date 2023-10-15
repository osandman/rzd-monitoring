package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.entity.StationEnum;
import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.StationService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import static net.osandman.rzdmonitoring.bot.command.ParamEnum.*;

@Component
public class FindRoutesCommand extends TelegramCommand {
    private final RouteService routeService;
    private final StationService stationService;

    public FindRoutesCommand(RouteService routeService, StationService stationService) {
        this.routeService = routeService;
        this.stationService = stationService;
        command = CommandEnum.ROUTES.name;
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        String userName = update.getMessage().getChat().getUserName();

        log.info("Сообщение '{}' получено от пользователя {}, chatId={}", messageText, userName, chatId);

        UserState userState = userStates.computeIfAbsent(chatId, k -> new UserState());
        UserState.CommandState commandState = userState.getCommandState(command);
        switch (commandState.getStep()) {
            case 1 -> {
                sendMessage(chatId, "Введите станцию отправления");
                commandState.incrementStep();
            }
            case 2 -> {
                StationDto fromStationDto = stationService.findStation(messageText.toUpperCase());
                if (fromStationDto.name() == null) {
                    commandState.setStep(2);
                    sendMessage(chatId, "Станция отправления '%s' не найдена, введите заново".formatted(messageText));
                    return;
                }
                commandState.addKey(FROM_STATION_CODE, fromStationDto.code());
                commandState.addKey(FROM_STATION, fromStationDto.name());
                sendMessage(chatId, "Введите станцию назначения");
                commandState.incrementStep();
            }
            case 3 -> {
                StationDto toStationDto = stationService.findStation(messageText.toUpperCase());
                if (toStationDto.name() == null) {
                    commandState.setStep(3);
                    sendMessage(chatId, "Станция назначения '%s' не найдена, введите заново".formatted(messageText));
                    return;
                }
                commandState.addKey(TO_STATION_CODE, toStationDto.code());
                commandState.addKey(TO_STATION, toStationDto.name());
                sendMessage(chatId, "Введите дату отправления, в формате " + DATE_FORMAT_PATTERN);
                commandState.incrementStep();
            }
            case 4 -> {
                if (parseDate(messageText) == null) {
                    commandState.setStep(4);
                    sendMessage(chatId, "Не верный формат даты '%s', введите заново".formatted(messageText));
                    return;
                }
                commandState.addKey(DATE, messageText);
                sendMessage(chatId, "Ищу маршруты от %s до %s, на %s".formatted(
                        commandState.getParams().get(FROM_STATION),
                        commandState.getParams().get(TO_STATION),
                        commandState.getParams().get(DATE)));
                sendMessage(chatId, getRoutesNew(commandState));
                userStates.remove(chatId);
            }
        }
    }

    private String getRoutes(UserState.CommandState commandState) {
        String routesStr = routeService.getRoutes(
                StationEnum.valueOf(commandState.getParams().get(FROM_STATION_CODE)),
                StationEnum.valueOf(commandState.getParams().get(TO_STATION_CODE)),
                commandState.getParams().get(DATE));
        if (!StringUtils.hasLength(routesStr)) {
            routesStr = "маршруты не найдены";
            log.error("Ошибка при получении маршрута");
        }
        return routesStr;
    }

    private String getRoutesNew(UserState.CommandState commandState) {
        String routesStr = routeService.getRoutes(
                commandState.getParams().get(FROM_STATION_CODE),
                commandState.getParams().get(TO_STATION_CODE),
                commandState.getParams().get(DATE));
        if (!StringUtils.hasLength(routesStr)) {
            routesStr = "Маршруты не найдены";
            log.error("Ошибка при получении маршрута");
        }
        return routesStr;
    }

    private StationEnum parseStation(String stationStr) {
        return Arrays.stream(StationEnum.values())
                .filter(st -> st.name().toLowerCase().contains(stationStr.toLowerCase()))
                .findAny()
                .orElse(null);
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr,
                    DateTimeFormatter.ofPattern(TelegramCommand.DATE_FORMAT_PATTERN));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
