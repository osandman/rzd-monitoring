package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.TaskResult;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION_CODE;

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
                LocalDate localDate = handleDate(update, command);
                if (localDate == null) {
                    return;
                }
                String dateToSearch = localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));

                sendMessage(command.chatId(), "Поиск поездов на %s".formatted(dateToSearch), true);
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
                    return;
                }
                List<String> availableRoutes = routeMapper.toFindTicketsList(routesResult.routes());
                if (availableRoutes.isEmpty()) {
                    sendMessage(
                        command.chatId(),
                        "⚠ Не найдены поезда на дату %s, попробуйте выбрать другую дату"
                            .formatted(dateToSearch)
                    );
                    return;
                }
                command.state().addKey(DATE, localDate.format(ISO_LOCAL_DATE));

                sendMultiSelectButtons(
                    command.chatId(),
                    MultiSelectType.ROUTES,
                    "Найдены маршруты (%d) [%s - %s] на %s, выберите поезд:".formatted(
                        availableRoutes.size(),
                        command.state().getParam(FROM_STATION),
                        command.state().getParam(TO_STATION),
                        dateToSearch
                    ),
                    availableRoutes
                );

                command.state().incrementStep();
            }
            case 7 -> { // выбор поездов и вывод фильтров
                CallbackQuery callbackQuery = checkCallbackQuery(update, command, MultiSelectType.ROUTES);
                if (callbackQuery == null) {
                    return;
                }
                handleComplete(command, MultiSelectType.ROUTES, callbackQuery, false);

                // Отправляем кнопки для множественного выбора фильтров поиска билетов
                sendMultiSelectButtons(
                    command.chatId(),
                    MultiSelectType.SEAT_FILTERS,
                    "Выберите фильтры поиска билетов:",
                    SeatFilter.getButtons()
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
                    messageTask = """
                        ✅ Запущен мониторинг билетов taskId=%s, при нахождении билетов вы получите уведомление в чат,
                        период поиска каждые %d мин. (фильтры поиска: '%s')
                        """.formatted(
                        taskResult.taskId(),
                        scheduleConfig.getInterval(),
                        command.state().getMultiSelectParam(MultiSelectType.SEAT_FILTERS).getSelectedText()
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
        CommandContext command, MultiSelectType seatFilter, CallbackQuery callbackQuery, boolean hideButtons
    ) {
        UserState.MultiSelect multiSelect = command.state().getMultiSelectParam(seatFilter);

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
        sendMessage(command.chatId(), "Выбраны: " + multiSelect.getSelectedText());

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
        String millis = String.valueOf(System.currentTimeMillis());
        String taskId = millis.substring(5, millis.length() - 1);
        TicketsTask ticketsTask = TicketsTask.builder()
            .chatId(commandContext.chatId())
            .taskId(taskId)
            .date(state.getParam(DATE))
            .fromCode(state.getParam(FROM_STATION_CODE))
            .fromStation(state.getParam(FROM_STATION))
            .toCode(state.getParam(TO_STATION_CODE))
            .toStation(state.getParam(TO_STATION))
            .routeNumbers(trainNumbers)
            .filters(seatFilters.stream().map(SeatFilter::getButtonText).collect(Collectors.toSet()))
            .build();
        try {
            multiTaskScheduler.addTask(ticketsTask, seatFilters);
        } catch (Exception e) {
            return new TaskResult(false, e.getMessage(), taskId);
        }
        return new TaskResult(true, "Задача добавлена", taskId);
    }
}
