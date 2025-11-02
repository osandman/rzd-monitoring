package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.TaskResult;
import net.osandman.rzdmonitoring.dto.route.CarriageDto;
import net.osandman.rzdmonitoring.dto.route.RouteDto;
import net.osandman.rzdmonitoring.dto.route.RoutesResult;
import net.osandman.rzdmonitoring.entity.MultiSelectType;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.mapping.RouteMapper;
import net.osandman.rzdmonitoring.scheduler.MultiTaskScheduler;
import net.osandman.rzdmonitoring.scheduler.ScheduleConfig;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.route.RouteService;
import net.osandman.rzdmonitoring.service.seat.SeatFilter;
import net.osandman.rzdmonitoring.util.Utils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import static net.osandman.rzdmonitoring.util.Utils.generateTaskId;

@Component
@RequiredArgsConstructor
public class FindTicketsCommand extends AbstractTelegramCommand {

    private final MultiTaskScheduler multiTaskScheduler;
    private final ScheduleConfig scheduleConfig;
    private final RouteService routeService;
    private final RouteMapper routeMapper;

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
                List<String> availableCarTypes = resolveCarTypes(command);
                String addStr = availableCarTypes.size() == 1
                    ? " (присутствуют только '%s' вагоны)".formatted(availableCarTypes.get(0))
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

    private List<String> resolveCarTypes(CommandContext command) {
        List<String> trainNumbers = command.state().getMultiSelectParam(MultiSelectType.ROUTES).getSelectedOptions().stream()
            .map(Utils::getFirstWord)
            .toList();
        List<RouteDto> selectedRoutes = command.state().getAdditionalObject(ROUTES, RouteDto.class).stream()
            .filter(routeDto -> trainNumbers.contains(routeDto.getTrainNumber()))
            .toList();

        List<String> availableCarTypes = Collections.emptyList();

        boolean allTrainsHaveCarriages = true;
        for (RouteDto route : selectedRoutes) {
            List<CarriageDto> carriages = route.getCarriages();
            if (carriages == null || carriages.isEmpty()) {
                allTrainsHaveCarriages = false;
                break;
            }
        }

        if (allTrainsHaveCarriages) {
            // Собираем типы вагонов только если у всех поездов есть данные
            availableCarTypes = selectedRoutes.stream()
                .flatMap(routeDto -> routeDto.getCarriages().stream())
                .map(CarriageDto::getTypeName)
                .filter(Objects::nonNull)
                .filter(type -> !type.trim().isEmpty())
                .distinct()
                .toList();
            log.info("✅ Найдены типы вагонов: {}", availableCarTypes);
        } else {
            log.info("❌ Типы вагонов неизвестны - будут показаны все фильтры");
        }
        return availableCarTypes;
    }

    private boolean setupRoutes(Update update, CommandContext command) {
        LocalDate localDate = handleDate(update, command);
        if (localDate == null) {
            return false;
        }
        command.state().addKey(DATE, localDate.format(ISO_LOCAL_DATE));
        String dateToSearch = localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN_SHORT));

        sendMessage(command.chatId(), "Поиск маршрутов на %s...".formatted(dateToSearch), true);
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
            return false;
        }
        // сохраняем все маршруты чтобы при создании задания
        // использовать данные даты отправления по МСК (departureDateTime)
        command.state().setAdditionalObjects(Map.of(ROUTES, routesResult.routes()));

        List<String> availableRoutes = routeMapper.toFindTicketsList(routesResult.routes()).stream()
            .filter(s -> !s.contains("пригород"))
            .collect(Collectors.toList());

        if (availableRoutes.isEmpty()) {
            sendMessage(
                command.chatId(),
                "⚠ Не найдены поезда на дату %s, попробуйте выбрать другую дату"
                    .formatted(dateToSearch)
            );
            return false;
        }

        sendMultiSelectButtons(
            command.chatId(),
            MultiSelectType.ROUTES,
            "Найдены маршруты (%d) [%s - %s] на %s, выберите поезд(а) или новую дату:".formatted(
                availableRoutes.size(),
                command.state().getParam(FROM_STATION),
                command.state().getParam(TO_STATION),
                dateToSearch
            ),
            availableRoutes
        );
        return true;
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
