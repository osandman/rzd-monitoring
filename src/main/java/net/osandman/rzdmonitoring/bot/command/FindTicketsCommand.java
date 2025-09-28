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
import java.util.Arrays;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.SEAT_FILTERS;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TRAIN_NUMBERS;

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
            case 7 -> { // вывод фильтров
                command.state().addKey(TRAIN_NUMBERS, command.messageText());
                sendMessage(command.chatId(), "Выбран поезд(а) №%s".formatted(command.messageText()));

                FilterData filterData = createFilterData();
                // TODO сделать метод который позволяет множественный выбор фильтров и поездов с кнопок
                sendButtons(
                    command.chatId(),
                    "Выберите фильтры поиска билетов:",
                    List.of(
                        String.join(",", toStringButtons(filterData.seatFilters1())),
                        String.join(",", toStringButtons(filterData.seatFilters2())),
                        String.join(",", toStringButtons(filterData.seatFilters3())),
                        String.join(",", toStringButtons(filterData.seatFilters4())),
                        String.join(",", toStringButtons(filterData.seatFilters5()))
                    ),
                    2
                );
//                sendMultiSelectButtons(
//                    command.chatId(),
//                    MultiSelectType.SEAT_FILTER,
//                    "Выберите фильтры поиска билетов:",
//                    Set.of(SeatFilter.values())
//                );
                command.state().incrementStep();
            }
            case 8 -> { // запуск мониторинга билетов
                command.state().addKey(SEAT_FILTERS, command.messageText());
                TaskResult taskResult = createTask(command);
                String messageTask;
                if (taskResult.success()) {
                    messageTask = """
                        ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                        период поиска каждые %d мин. (фильтры поиска: '%s')
                        """.formatted(
                        taskResult.taskId(),
                        scheduleConfig.getInterval(),
                        command.state().getParams().get(SEAT_FILTERS)
                    );
                } else {
                    messageTask = "Произошла ошибка при запуске мониторинга билетов: '%s'".formatted(taskResult.msg());
                    command.state().setStep(1);
                }
                sendMessage(command.chatId(), messageTask);
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            }
        }
    }

    private static FilterData createFilterData() {
        List<SeatFilter> seatFilters1 = List.of(
            SeatFilter.COMPARTMENT, SeatFilter.DOWN_SEATS, SeatFilter.NOT_INVALID
        );
        List<SeatFilter> seatFilters2 = List.of(
            SeatFilter.DOWN_SEATS, SeatFilter.NOT_INVALID
        );
        List<SeatFilter> seatFilters3 = List.of(
            SeatFilter.COMPARTMENT
        );
        List<SeatFilter> seatFilters4 = List.of(
            SeatFilter.PLATZKART
        );
        List<SeatFilter> seatFilters5 = List.of(
            SeatFilter.ALL_SEATS
        );
        return new FilterData(seatFilters1, seatFilters2, seatFilters3, seatFilters4, seatFilters5);
    }

    private record FilterData(
        List<SeatFilter> seatFilters1,
        List<SeatFilter> seatFilters2,
        List<SeatFilter> seatFilters3,
        List<SeatFilter> seatFilters4,
        List<SeatFilter> seatFilters5
    ) {
    }

    @Override
    public boolean canToShow() {
        return true;
    }

    List<String> toStringButtons(List<SeatFilter> seatFilters) {
        return seatFilters.stream()
            .map(SeatFilter::getButtonText)
            .toList();
    }

    private TaskResult createTask(CommandContext commandContext) {
        UserState.CommandState state = commandContext.state();
        String date = state.getParams().get(DATE);
        String from = state.getParams().get(FROM_STATION);
        String to = state.getParams().get(TO_STATION);
        String[] trainNumbers = state.getParams().get(TRAIN_NUMBERS).split(",");
        List<SeatFilter> seatFilters = Arrays.stream(state.getParams().get(SEAT_FILTERS).split(","))
            .map(SeatFilter::getByButtonText)
            .toList();
        long chatId = commandContext.chatId();
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
