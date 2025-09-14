package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.TaskResult;
import net.osandman.rzdmonitoring.dto.route.RouteDto;
import net.osandman.rzdmonitoring.dto.route.RoutesResult;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.ScheduleConfig;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.route.RouteService;
import net.osandman.rzdmonitoring.service.seat.SeatFilter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
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
    private final RouteService routeService;


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
            case 6 -> { // ввод даты и выбор маршрута поезда
                LocalDate localDate = handleDate(update, command);
                if (localDate == null) {
                    return;
                }
                String dateToSearch = localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));

                sendMessage(command.chatId(), "Поиск поездов на %s".formatted(dateToSearch));
                RoutesResult routesResult = routeService.findRoutes(
                    command.state().getParams().get(FROM_STATION_CODE),
                    command.state().getParams().get(TO_STATION_CODE),
                    dateToSearch
                );
                if (routesResult.error() != null) {
                    sendMessage(
                        command.chatId(),
                        "⚠ Ошибка: '%s' при поиске маршрутов на дату '%s'".formatted(routesResult.error(), dateToSearch)
                    );
                    sendCalendar(command.chatId(), "Введите дату отправления:", update);
                    return;
                }
                List<String> availableNumbers;
                if (routesResult.routesCount() != 0) {
                    availableNumbers = routesResult.routes().stream()
                        .filter(route -> !route.getIsSuburban())
                        .map(RouteDto::getTrainNumber)
                        .toList();
                } else {
                    sendMessage(
                        command.chatId(),
                        "⚠ не найдены поезда на дату '%s'".formatted(dateToSearch)
                    );
                    sendCalendar(command.chatId(), "Введите дату отправления:", update);
                    return;
                }
                if (availableNumbers.isEmpty()) {
                    sendMessage(
                        command.chatId(),
                        "Нет подходящих поездов на дату '%s'".formatted(dateToSearch)
                    );
                    sendCalendar(command.chatId(), "Введите дату отправления:", update);
                    return;
                }
                sendButtons(
                    command.chatId(),
                    "Найдено %d маршрутов, выберите номер поезда:".formatted(availableNumbers.size()),
                    availableNumbers
                );
                command.state().addKey(DATE, localDate.format(ISO_LOCAL_DATE));
                command.state().incrementStep();
            }
            case 7 -> { // запуск мониторинга билетов
                // TODO дополнить получение фильтра из текстового сообщения телеграма, временно хардкод
                List<SeatFilter> seatFilters = List.of(
                    SeatFilter.COMPARTMENT, SeatFilter.DOWN_SEATS, SeatFilter.NOT_INVALID
                );
                TaskResult taskResult = createTask(command, seatFilters);
//                command.chatId(), command.state(), command.messageText().split(",")
                String messageTask;
                if (taskResult.success()) {
                    messageTask = """
                        ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                        период поиска каждые %d мин. (фильтр по умолчанию: '%s')
                        """.formatted(
                        taskResult.taskId(),
                        scheduleConfig.getInterval(),
                        String.join(",", seatFilters.stream().map(SeatFilter::getButtonText).toList())
                    );
                } else {
                    messageTask = "Произошла ошибка при запуске мониторинга билетов: '%s'".formatted(taskResult.msg());
                    command.state().decrementStep();
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

    private TaskResult createTask(CommandContext commandContext, List<SeatFilter> seatFilters) {
        UserState.CommandState state = commandContext.state();
        String date = state.getParams().get(DATE);
        String from = state.getParams().get(FROM_STATION);
        String to = state.getParams().get(TO_STATION);
        long chatId = commandContext.chatId();
        String[] trainNumbers = commandContext.messageText().split(",");
        String taskId = "task-" + date + "-" + from + "-" + to + "-" +
                        String.join("_", trainNumbers) + "-chatId-" + chatId;
        TicketsTask ticketsTask = TicketsTask.builder()
            .chatId(chatId)
            .taskId(taskId)
            .date(date)
            .fromCode(state.getParams().get(FROM_STATION_CODE))
            .toCode(state.getParams().get(TO_STATION_CODE))
            .routeNumbers(trainNumbers)
            .build();
        try {
            multiTaskScheduler.addTask(ticketsTask, seatFilters);
        } catch (Exception e) {
            return new TaskResult(false, e.getMessage(), taskId);
        }
        return new TaskResult(true, "Задача добавлена", taskId);
    }
}
