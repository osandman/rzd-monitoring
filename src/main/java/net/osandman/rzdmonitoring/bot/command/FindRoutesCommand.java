package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.repository.StationEnum;
import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.StationService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

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
        UserState.CommandState commandState = userState.getCommandState(command); // устанавливает команду если ее не было

        switch (commandState.getStep()) {
            case 1 -> { // начало команды
                sendMessage(chatId, "Введите станцию отправления");
                commandState.incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findStations(messageText, commandState, 2, chatId);
            }
            case 3 -> { // выбор станции отправления из найденных
                // TODO нужно чтобы лист станций dto кэшировлся либо сделать его полем класса
                StationDto fromStationDto = getStationDto(messageText, stationService.findStations(messageText));
                if (fromStationDto == null) {
                    commandState.setStep(3);
                    sendMessage(chatId, "Станция отправления '%s' не найдена, выберите из списка".formatted(messageText));
                    return;
                }
                commandState.addKey(FROM_STATION_CODE, fromStationDto.code());
                commandState.addKey(FROM_STATION, fromStationDto.name());
                sendMessage(chatId, "Введите станцию назначения");
                commandState.incrementStep();
            }
            case 4 -> { // ввод вручную станции назначения
                findStations(messageText, commandState, 4, chatId);
            }
            case 5 -> { // выбор станции назначения из найденных
                // TODO нужно чтобы лист станций dto кэшировлся либо сделать его полем класса
                StationDto toStationDto = getStationDto(messageText, stationService.findStations(messageText));
                if (toStationDto == null) {
                    commandState.setStep(5);
                    sendMessage(chatId, "Станция назначения '%s' не найдена, выберите из списка".formatted(messageText));
                    return;
                }
                commandState.addKey(TO_STATION_CODE, toStationDto.code());
                commandState.addKey(TO_STATION, toStationDto.name());
                sendMessage(chatId, "Введите дату отправления, в формате " + DATE_FORMAT_PATTERN);
                commandState.incrementStep();
            }
            case 6 -> { // ввод даты
                if (parseDate(messageText) == null) {
                    commandState.setStep(6);
                    sendMessage(chatId, "Не верный формат даты '%s', введите заново".formatted(messageText));
                    return;
                }
                commandState.addKey(DATE, messageText);
                sendMessage(chatId, "Ищу маршруты от %s до %s, на %s".formatted(
                        commandState.getParams().get(FROM_STATION),
                        commandState.getParams().get(TO_STATION),
                        commandState.getParams().get(DATE)));
                sendMessage(chatId, getRoutes(commandState));
                userStates.remove(chatId);
            }
        }
    }

    private void findStations(String messageText, UserState.CommandState commandState, int step, long chatId) {
        List<StationDto> fromStationDtos = stationService.findStations(messageText);
        if (fromStationDtos.size() == 0) {
            commandState.setStep(step);
            sendMessage(chatId, "Станция '%s' не найдена, введите заново".formatted(messageText));
            return;
        }
        sendButtons(chatId, "Выберите станцию:", fromStationDtos);
        commandState.incrementStep();
    }

    private StationDto getStationDto(String messageText, List<StationDto> toStationDtos) {
        return toStationDtos == null ? null :
                toStationDtos.stream()
                        .filter(stationDto -> stationDto.name().equalsIgnoreCase(messageText))
                        .findAny().orElse(null);
    }

    private String getRoutes(UserState.CommandState commandState) {
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
