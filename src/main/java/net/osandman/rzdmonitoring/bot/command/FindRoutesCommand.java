package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import net.osandman.rzdmonitoring.entity.Station;
import net.osandman.rzdmonitoring.service.RzdMonitoringService;
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
    private final RzdMonitoringService monitoringService;

    public FindRoutesCommand(RzdMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
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
                Station from = parseStation(messageText);
                if (from == null) {
                    commandState.setStep(2);
                    sendMessage(chatId, "Станция отправления '%s' не найдена, введите заново".formatted(messageText));
                    return;
                }
                commandState.addKey(FROM_STATION, from.name());
                sendMessage(chatId, "Введите станцию назначения");
                commandState.incrementStep();
            }
            case 3 -> {
                Station to = parseStation(messageText);
                if (to == null) {
                    commandState.setStep(3);
                    sendMessage(chatId, "Станция назначения '%s' не найдена, введите заново".formatted(messageText));
                    return;
                }
                commandState.addKey(TO_STATION, to.name());
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
                String allValues = String.join(", ", commandState.getParams().values());
                sendMessage(chatId, allValues + ", ищем ...");
                sendMessage(chatId, getRoutes(commandState));
                userStates.remove(chatId);
            }
        }
    }

    private String getRoutes(UserState.CommandState commandState) {
        String routesStr = monitoringService.getRoutes(
                Station.valueOf(commandState.getParams().get(FROM_STATION)),
                Station.valueOf(commandState.getParams().get(TO_STATION)),
                commandState.getParams().get(DATE));
        if (!StringUtils.hasLength(routesStr)) {
            routesStr = "маршруты не найдены";
            log.error("Ошибка при получении маршрута");
        }
        return routesStr;
    }

    private Station parseStation(String stationStr) {
        return Arrays.stream(Station.values())
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
