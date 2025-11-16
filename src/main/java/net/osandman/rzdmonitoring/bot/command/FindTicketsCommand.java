package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.client.dto.v2.train.CarDto;
import net.osandman.rzdmonitoring.client.dto.v2.train.RootTrainDto;
import net.osandman.rzdmonitoring.dto.TaskResult;
import net.osandman.rzdmonitoring.dto.route.CarriageDto;
import net.osandman.rzdmonitoring.dto.route.RouteDto;
import net.osandman.rzdmonitoring.dto.route.RoutesResult;
import net.osandman.rzdmonitoring.entity.MultiSelectType;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.mapping.RouteMapper;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.route.RouteService;
import net.osandman.rzdmonitoring.service.seat.SeatFilter;
import net.osandman.rzdmonitoring.service.seat.TicketService;
import net.osandman.rzdmonitoring.util.Utils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.dostonhamrakulov.InlineCalendarCommandUtil.isInlineCalendarClicked;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.toMap;
import static net.osandman.rzdmonitoring.bot.command.ParamType.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.ROUTES;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION_CODE;
import static net.osandman.rzdmonitoring.config.Constant.DATE_FORMAT_PATTERN_SHORT;
import static net.osandman.rzdmonitoring.config.Constant.JSON_DATE_FORMAT_PATTERN;
import static net.osandman.rzdmonitoring.util.Utils.dateToString;
import static net.osandman.rzdmonitoring.util.Utils.generateTaskId;

@Component
@RequiredArgsConstructor
public class FindTicketsCommand extends AbstractTelegramCommand {

    private final MultiTaskScheduler multiTaskScheduler;
    private final RouteService routeService;
    private final RouteMapper routeMapper;
    private final TicketService ticketService;

    @Override
    public Command getCommand() {
        return Command.TICKETS;
    }

    @Override
    public void handleCommand(Update update) {
        CommandContext command = buildCommandContext(update, getCommand());
        switch (command.state().getStep()) {
            case 1 -> { // начало команды
                sendMessage(command.chatId(), "Введите станцию отправления:", true);
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
                if (!setupRoutes(update, command)) {
                    return;
                }
                command.state().incrementStep();
            }
            case 7 -> { // выбор поездов и вывод фильтров
                if (isInlineCalendarClicked(update)) {
                    setupRoutes(update, command);
                    return;
                }
                CallbackQuery callbackQuery = checkCallbackQuery(update, command, MultiSelectType.ROUTES);
                if (callbackQuery == null) {
                    return;
                }
                handleComplete(command, MultiSelectType.ROUTES, callbackQuery, false);

                // Отправляем кнопки для множественного выбора фильтров поиска билетов
                sendMessage(command.chatId(), "Определяем доступные типы вагоны для фильтров ...", true);
                Set<String> availableCarTypes = resolveCarTypes(command);
                String addStr = availableCarTypes.size() == 1
                    ? " (присутствуют только '%s' вагоны)".formatted(availableCarTypes.iterator().next())
                    : "";
                sendMultiSelectButtons(
                    command.chatId(),
                    MultiSelectType.SEAT_FILTERS,
                    "Выберите фильтры поиска билетов" + addStr + ":",
                    SeatFilter.getButtonsForAvailableCarTypes(availableCarTypes)
                );
                command.state().incrementStep();
            }
            case 8 -> { // запуск мониторинга билетов
                CallbackQuery callbackQuery = checkCallbackQuery(update, command, MultiSelectType.SEAT_FILTERS);
                if (callbackQuery == null) {
                    return;
                }
                handleComplete(command, MultiSelectType.SEAT_FILTERS, callbackQuery, false);
                TaskResult taskResult = createTask(command);
                String messageTask;
                if (taskResult.success()) {
                    TicketsTask task = taskResult.ticketsTask();
                    messageTask = """
                        ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                        период поиска каждые %d мин. (поезда:%s фильтры поиска:%s). Действия с задачами - команда /tasks
                        """.formatted(
                        task.taskId(),
                        multiTaskScheduler.getScheduledTasks().get(command.chatId()).get(task.taskId()).getInterval(),
                        task.trainDepartureDateMap().keySet(),
                        task.filters()
                    );
                } else {
                    messageTask = "Произошла ошибка при запуске мониторинга билетов: '%s'".formatted(taskResult.msg());
                    command.state().setStep(1);
                }
                sendMessage(command.chatId(), messageTask);
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            }
            default -> {
                sendMessage(command.chatId(), "Шаг команды не найден, обратитесь к разработчику");
            }
        }
    }

    // TODO подумать как можно этот процесс оптимизировать или убрать вообще проверку типов вагонов
    private Set<String> resolveCarTypes(CommandContext command) {
        Set<String> trainNumbers = command.state().getMultiSelectParam(MultiSelectType.ROUTES).getSelectedOptions().stream()
            .map(Utils::getFirstWord)
            .collect(Collectors.toSet());

        Set<String> availableCarTypes = command.state().getAdditionalObject(ROUTES, RouteDto.class).stream()
            .filter(routeDto -> trainNumbers.contains(routeDto.getTrainNumber()))
            .filter(routeDto -> !CollectionUtils.isEmpty(routeDto.getCarriages()))
            .flatMap(routeDto -> routeDto.getCarriages().stream())
            .map(CarriageDto::getTypeName)
            .filter(StringUtils::hasText)
            .collect(Collectors.toCollection(HashSet::new));
        if (SeatFilter.checkAllCarTypesExist(availableCarTypes)) {
            return availableCarTypes;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        for (String trainNumber : trainNumbers) {
            for (int i = 1; i <= 4; i++) {
                try {
                    RootTrainDto rootTrainDto = ticketService.getRootTrainDto(
                        command.state().getParam(FROM_STATION_CODE),
                        command.state().getParam(TO_STATION_CODE),
                        dateToString(localDateTime.plusDays((i + 1) * 5), JSON_DATE_FORMAT_PATTERN),
                        trainNumber
                    );
                    availableCarTypes.addAll(
                        rootTrainDto.getCars().stream().map(CarDto::getCarTypeName).distinct().toList()
                    );
                    if (SeatFilter.checkAllCarTypesExist(availableCarTypes)) {
                        return availableCarTypes;
                    }
                } catch (Exception e) {
                    log.warn("Попытка получить данные маршрута {} для определения типов вагонов не удалась", trainNumber);
                }
            }
        }
        log.info("✅ Определены типы вагонов: {}", availableCarTypes);
        return availableCarTypes;
    }

    private boolean setupRoutes(Update update, CommandContext command) {
        LocalDate localDate = handleDate(update, command);
        if (localDate == null) {
            return false;
        }
        command.state().addKey(DATE, localDate.format(ISO_LOCAL_DATE));
        String dateForShow = localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN_SHORT));

        sendMessage(command.chatId(), "Поиск маршрутов на %s ...".formatted(dateForShow), true);
        List<String> availableRoutes = getAvailableRoutes(command, localDate);
        if (CollectionUtils.isEmpty(availableRoutes)) {
            return false;
        }
        sendMultiSelectButtons(
            command.chatId(),
            MultiSelectType.ROUTES,
            "Найдены маршруты (%d) [%s - %s] на %s, выберите поезд(а) или новую дату:".formatted(
                availableRoutes.size(),
                command.state().getParam(FROM_STATION),
                command.state().getParam(TO_STATION),
                dateForShow
            ),
            availableRoutes
        );
        return true;
    }

    private List<String> getAvailableRoutes(CommandContext command, LocalDate localDate) {
        String dateToSearch = localDate.format(DateTimeFormatter.ofPattern(JSON_DATE_FORMAT_PATTERN));
        RoutesResult routesResult = routeService.findRoutes(
            command.state().getParam(FROM_STATION_CODE),
            command.state().getParam(TO_STATION_CODE),
            dateToSearch
        );
        // проверки что маршруты найдены
        if (routesResult.error() != null) {
            sendMessage(
                command.chatId(),
                "⚠ Ошибка: '%s' при поиске маршрутов на дату %s, попробуйте выбрать другую дату"
                    .formatted(routesResult.error(), dateToSearch)
            );
            return List.of();
        }
        // сохраняем все маршруты чтобы при создании задания
        // использовать данные даты отправления по МСК (departureDateTime)
        command.state().setAdditionalObjects(Map.of(ROUTES, routesResult.routes()));

        List<String> availableRoutes = routeMapper.toFindTicketsList(routesResult.routes());

        if (availableRoutes.isEmpty()) {
            sendMessage(
                command.chatId(),
                "⚠ Не найдены поезда на дату %s, попробуйте выбрать другую дату".formatted(dateToSearch)
            );
            return List.of();
        }
        return availableRoutes;
    }

    private CallbackQuery checkCallbackQuery(Update update, CommandContext command, MultiSelectType multiSelectType) {
        // Проверяем, является ли это callback query для множественного выбора
        if (!update.hasCallbackQuery()) {
            showAlert(null, command.chatId(), "Нужно выбрать значение из списка!");
            return null;
        }
        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (!callbackQuery.getData().startsWith("multiselect:done")) {
            handleMultiSelectCallback(update, multiSelectType);
            return null;
        }
        if (command.state().getMultiSelectParam(multiSelectType).getSelectedOptions().isEmpty()) {
            showAlert(callbackQuery, command.chatId(), "Нужно выбрать хотя бы одно значение!");
            return null;
        }
        return callbackQuery;
    }

    private void handleComplete(
        CommandContext command, MultiSelectType selectType, CallbackQuery callbackQuery, boolean hideButtons
    ) {
        UserState.MultiSelect multiSelect = command.state().getMultiSelectParam(selectType);

        if (hideButtons) {
            // Удаляем inline keyboard
            Integer messageId = callbackQuery.getMessage().getMessageId();
            EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
            editMarkup.setChatId(String.valueOf(command.chatId()));
            editMarkup.setMessageId(messageId);
            editMarkup.setReplyMarkup(null);
            executeMessage(editMarkup);
        }

        // Отправляем сообщение с выбранными опциями
        String message = "Выбраны " + selectType.getDescription() + ":" + System.lineSeparator()
                         + String.join(System.lineSeparator(), "‣" + multiSelect.getSelectedOptions());
        sendMessage(command.chatId(), message);

        // ОБЯЗАТЕЛЬНО отвечаем на callback query
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
        executeMessage(answerCallbackQuery);
    }

    @Override
    public boolean canToShow() {
        return true;
    }

    private TaskResult createTask(CommandContext commandContext) {
        UserState.CommandState state = commandContext.state();
        List<String> trainNumbers = state.getMultiSelectParam(MultiSelectType.ROUTES)
            .getSelectedOptions().stream()
            .map(Utils::getFirstWord)
            .toList();
        Set<SeatFilter> seatFilters = commandContext.state().getMultiSelectParam(MultiSelectType.SEAT_FILTERS)
            .getSelectedOptions().stream()
            .map(SeatFilter::getByButtonText)
            .collect(Collectors.toSet());
        Map<String, LocalDateTime> trainDepartureDateMap = state.getAdditionalObject(ROUTES, RouteDto.class).stream()
            .filter(route -> trainNumbers.contains(route.getTrainNumber()))
            .collect(toMap(RouteDto::getTrainNumber, RouteDto::getDepartureDateTime));

        String taskId = generateTaskId();

        TicketsTask ticketsTask = TicketsTask.builder()
            .chatId(commandContext.chatId())
            .userName(commandContext.userName())
            .taskId(taskId)
            .date(state.getParam(DATE))
            .fromCode(state.getParam(FROM_STATION_CODE))
            .fromStation(state.getParam(FROM_STATION))
            .toCode(state.getParam(TO_STATION_CODE))
            .toStation(state.getParam(TO_STATION))
            .trainDepartureDateMap(trainDepartureDateMap)
            .filters(seatFilters.stream().map(SeatFilter::getButtonText).collect(Collectors.toSet()))
            .build();
        try {
            multiTaskScheduler.addTask(ticketsTask, seatFilters);
        } catch (Exception e) {
            return new TaskResult(false, e.getMessage(), null);
        }
        return new TaskResult(true, "Задача добавлена", ticketsTask);
    }
}
