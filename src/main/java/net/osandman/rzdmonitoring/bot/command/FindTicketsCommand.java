package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.service.RouteService;
import net.osandman.rzdmonitoring.service.StationService;
import net.osandman.rzdmonitoring.service.TicketService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static net.osandman.rzdmonitoring.bot.command.ParamEnum.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION_CODE;

@Component
public class FindTicketsCommand extends TelegramCommand {


    public FindTicketsCommand(TicketService ticketService, RouteService routeService,
                              StationService stationService) {
        super(ticketService, routeService, stationService, CommandEnum.TICKETS.name);
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
//        sendMessage(chatId, "Еще не готовы у пони подковы ...");
        Message message = update.getMessage();
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
                sendMessage(
                    chatId,
                    runScheduler(
                        chatId,
                        commandState
                    )
                );
                userStates.remove(chatId);
            }
        }
    }

    private String runScheduler(Long chatId, UserState.CommandState commandState, String... trainNumbers) {
        String date = commandState.getParams().get(DATE);
        String from = commandState.getParams().get(FROM_STATION);
        String to = commandState.getParams().get(TO_STATION);

        Executors.newSingleThreadExecutor().execute(
            () -> {
                String threadName = "task-" + date + "-from-" + from + "-to-" + to;
                Thread.currentThread().setName(threadName);
                if (threads.get(chatId) == null) {
                    threads.put(chatId, new CopyOnWriteArrayList<>() {{
                        add(threadName);
                    }});
                } else {
                    List<String> threadNames = threads.get(chatId);
                    threadNames.add(threadName);
                }
                ticketService.autoLoop(
                    date,
                    commandState.getParams().get(FROM_STATION_CODE),
                    commandState.getParams().get(TO_STATION_CODE),
                    trainNumbers
                );
            }
        );
        return ("Запущен мониторинг билетов на нижние места на дату %s, "
                + "при нахождении билетов вы получите уведомление в чат").formatted(date);
    }
}
