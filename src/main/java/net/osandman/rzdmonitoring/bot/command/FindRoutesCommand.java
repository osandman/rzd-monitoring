package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.StationService;
import net.osandman.rzdmonitoring.service.TicketService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static net.osandman.rzdmonitoring.bot.command.ParamEnum.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION_CODE;

@Component
public class FindRoutesCommand extends TelegramCommand {

    protected FindRoutesCommand(TicketService ticketService, RouteService routeService,
                                StationService stationService) {
        super(ticketService, routeService, stationService, CommandEnum.ROUTES.name);
    }

    @Override
    public void handleCommand(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        String messageText = message.getText();
        String userName = message.getChat().getFirstName() + " " + message.getChat().getLastName();

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
}
