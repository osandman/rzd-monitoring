package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.service.route.RouteService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static net.osandman.rzdmonitoring.bot.command.ParamType.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamType.TO_STATION_CODE;
import static net.osandman.rzdmonitoring.config.Constant.DATE_FORMAT_PATTERN_SHORT;

@Component
@RequiredArgsConstructor
public class FindRoutesCommand extends AbstractTelegramCommand {

    private final RouteService routeService;

    @Override
    public Command getCommand() {
        return Command.ROUTES;
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
            case 6 -> { // ввод даты
                LocalDate localDate = handleDate(update, command);
                if (localDate == null) {
                    return;
                }
                command.state().addKey(DATE, localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN_SHORT)));
                sendMessage(command.chatId(), "Ищу маршруты %s - %s на %s".formatted(
                    command.state().getParam(FROM_STATION),
                    command.state().getParam(TO_STATION),
                    command.state().getParam(DATE)));
                UserState.CommandState commandState = command.state();
                String answer = routeService.getRoutesAsString(
                    commandState.getParam(FROM_STATION_CODE),
                    commandState.getParam(TO_STATION_CODE),
                    commandState.getParam(DATE)
                );
                if (answer.toLowerCase().contains("не найдены")) {
                    sendMessage(
                        command.chatId(),
                        "%s на %s, попробуйте выбрать другую дату"
                            .formatted(answer, command.state().getParam(DATE))
                    );
                    return;
                }
                sendMessage(command.chatId(), answer, true);
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}
