package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.ScheduleConfig;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        CommandContext command = buildCommandContext(update, getCommand());
        switch (command.state().getStep()) {
            case 1 -> { // начало команды
                sendMessage(command.chatId(), "Введите станцию отправления");
                command.state().incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
            }
            case 3 -> { // выбор станции отправления из найденных
                // TODO нужно чтобы лист станций dto кэшировлся либо сделать его полем класса
                StationDto fromStationDto = getStationDto(
                    command.messageText(), stationService.findStations(command.messageText())
                );
                if (fromStationDto == null) {
                    sendMessage(
                        command.chatId(),
                        "Станция отправления '%s' не найдена, выберите из списка".formatted(command.messageText())
                    );
                    return;
                }
                command.state().addKey(FROM_STATION_CODE, fromStationDto.code());
                command.state().addKey(FROM_STATION, fromStationDto.name());
                sendMessage(command.chatId(), "Введите станцию назначения");
                command.state().incrementStep();
            }
            case 4 -> { // ввод вручную станции назначения
                findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
            }
            case 5 -> { // выбор станции назначения из найденных
                // TODO нужно чтобы лист станций dto кэшировлся либо сделать его полем класса
                StationDto toStationDto = getStationDto(
                    command.messageText(), stationService.findStations(command.messageText())
                );
                if (toStationDto == null) {
                    sendMessage(
                        command.chatId(),
                        "Станция назначения '%s' не найдена, выберите из списка".formatted(command.messageText())
                    );
                    return;
                }
                command.state().addKey(TO_STATION_CODE, toStationDto.code());
                command.state().addKey(TO_STATION, toStationDto.name());
                sendCalendar(command.chatId(), "Введите дату отправления", update);
                command.state().incrementStep();
            }
            case 6 -> { // ввод даты
                LocalDate localDate = handleDate(update, command);
                if (localDate == null) {
                    return;
                }
                command.state().addKey(DATE, localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
                String taskId = createTask(command.chatId(), command.state());
                String messageTask;
                if (taskId == null) {
                    messageTask = "⚠ Не найдены маршруты по данному запросу";
                } else {
                    messageTask = """
                        ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                        период поиска каждые %s минуты
                        """.formatted(taskId, scheduleConfig.getInterval());
                }
                sendMessage(command.chatId(), messageTask);
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
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
