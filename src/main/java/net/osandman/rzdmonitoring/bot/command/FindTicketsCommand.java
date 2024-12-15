package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.ScheduleConfig;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static net.osandman.rzdmonitoring.bot.command.ParamEnum.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION_CODE;

@Component
@RequiredArgsConstructor
public class FindTicketsCommand extends AbstractTelegramCommand implements ITelegramCommand {

    private final MultiTaskScheduler multiTaskScheduler;
    private final ScheduleConfig scheduleConfig;

    @Override
    public Command getCommand() {
        return Command.TICKETS;
    }

    @Override
    public void handleCommand(Update update) {
        // TODO рефакторить - использовать общий метод из абстрактного класса
        long chatId = update.getMessage().getChatId();
        Message message = update.getMessage();
        String messageText = message.getText();
        String userName = message.getChat().getFirstName() + " " + message.getChat().getLastName();

        log.info("Сообщение '{}' получено от пользователя {}, chatId={}", messageText, userName, chatId);

        UserState userState = userStateRepository.getOrCreate(chatId);
        // устанавливает команду если ее не было
        UserState.CommandState commandState = userState.getOrCreateCommandState(getCommand());

        switch (commandState.getStep()) {
            case 1 -> { // начало команды
                sendMessage(chatId, "Введите станцию отправления");
                commandState.incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findStationsAndIncrementStep(messageText, commandState, 2, chatId);
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
                findStationsAndIncrementStep(messageText, commandState, 4, chatId);
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
                CheckDateResult checkDateResult = dateValidate(messageText);
                if (!checkDateResult.valid()) {
                    commandState.setStep(6);
                    sendMessage(chatId, checkDateResult.message());
                    return;
                }
                commandState.addKey(DATE, messageText);
                String taskId = createTask(chatId, commandState);
                String messageTask;
                if (taskId == null) {
                    messageTask = "⚠ Не найдены маршруты по данному запросу";
                } else {
                    messageTask = """
                        ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                        период поиска каждые %s минуты
                        """.formatted(taskId, scheduleConfig.getInterval());
                }
                sendMessage(chatId, messageTask);
                userStateRepository.get(chatId).deleteCommand(getCommand());
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }

    private String createTask(Long chatId, UserState.CommandState commandState, String... trainNumbers) {
        String date = commandState.getParams().get(DATE);
        String from = commandState.getParams().get(FROM_STATION);
        String to = commandState.getParams().get(TO_STATION);
        String taskId = "task-" + date + "-from-" + from + "-to-" + to + "-chatId-" + chatId;
        TicketsTask ticketsTask = TicketsTask.builder()
            .chatId(chatId)
            .taskId(taskId)
            .date(date)
            .fromCode(commandState.getParams().get(FROM_STATION_CODE))
            .toCode(commandState.getParams().get(TO_STATION_CODE))
            .routeNumbers(trainNumbers)
            .build();
        if (multiTaskScheduler.addTask(ticketsTask)) {
            return taskId;
        }
        return null;
    }
}
