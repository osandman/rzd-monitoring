package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.Result;
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
                sendMessage(command.chatId(), "Введите станцию отправления:");
                command.state().incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
            }
            case 3 -> { // выбор станции отправления из найденных
                if (!checkStationAndSetStates(command, FROM_STATION_CODE, FROM_STATION)) {
                    return;
                }
                sendMessage(command.chatId(), "Введите станцию назначения:");
                command.state().incrementStep();
            }
            case 4 -> { // ввод вручную станции назначения
                findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
            }
            case 5 -> { // выбор станции назначения из найденных
                if (!checkStationAndSetStates(command, TO_STATION_CODE, TO_STATION)) {
                    return;
                }
                sendCalendar(command.chatId(), "Введите дату отправления:", update);
                command.state().incrementStep();
            }
            case 6 -> { // ввод даты
                LocalDate localDate = handleDate(update, command);
                if (localDate == null) {
                    return;
                }
                command.state().addKey(DATE, localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
                Result result = createTask(command.chatId(), command.state());
                if (!result.success()) {
                    sendMessage(
                        command.chatId(),
                        "⚠ %s на '%s', попробуйте выбрать другую дату"
                            .formatted(result.msg(), command.state().getParams().get(DATE))
                    );
                    sendCalendar(command.chatId(), "Введите дату отправления:", update);
                    return;
                }
                String messageTask = """
                    ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                    период поиска каждые %s минуты
                    """.formatted(result.msg(), scheduleConfig.getInterval());
                sendMessage(command.chatId(), messageTask);
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }

    private Result createTask(Long chatId, UserState.CommandState commandState, String... trainNumbers) {
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
        return multiTaskScheduler.addTask(ticketsTask);
    }
}
