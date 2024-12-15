package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.service.RouteService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static net.osandman.rzdmonitoring.bot.command.ParamEnum.DATE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.FROM_STATION_CODE;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION;
import static net.osandman.rzdmonitoring.bot.command.ParamEnum.TO_STATION_CODE;

@Component
@RequiredArgsConstructor
public class FindRoutesCommand extends AbstractTelegramCommand implements ITelegramCommand {

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
                sendMessage(command.chatId(), "Введите станцию отправления");
                command.state().incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findStationsAndIncrementStep(
                    command.messageText(), command.state(), 2, command.chatId()
                );
            }
            case 3 -> { // выбор станции отправления из найденных
                // TODO нужно чтобы лист станций dto кэшировлся либо сделать его полем класса
                StationDto fromStationDto = getStationDto(
                    command.messageText(), stationService.findStations(command.messageText())
                );
                if (fromStationDto == null) {
                    command.state().setStep(3);
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
                findStationsAndIncrementStep(
                    command.messageText(), command.state(), 4, command.chatId()
                );
            }
            case 5 -> { // выбор станции назначения из найденных
                // TODO нужно чтобы лист станций dto кэшировлся либо сделать его полем класса
                StationDto toStationDto = getStationDto(
                    command.messageText(), stationService.findStations(command.messageText())
                );
                if (toStationDto == null) {
                    command.state().setStep(5);
                    sendMessage(
                        command.chatId(),
                        "Станция назначения '%s' не найдена, выберите из списка".formatted(command.messageText())
                    );
                    return;
                }
                command.state().addKey(TO_STATION_CODE, toStationDto.code());
                command.state().addKey(TO_STATION, toStationDto.name());
                sendMessage(command.chatId(), "Введите дату отправления, в формате " + DATE_FORMAT_PATTERN);
                command.state().incrementStep();
            }
            case 6 -> { // ввод даты
                String dateStr = command.messageText();
                CheckDateResult checkDateResult = dateValidate(dateStr);
                if (!checkDateResult.valid()) {
                    command.state().setStep(6);
                    sendMessage(command.chatId(), checkDateResult.message());
                    return;
                }
                command.state().addKey(DATE, dateStr);
                sendMessage(command.chatId(), "Ищу маршруты от %s до %s, на %s".formatted(
                    command.state().getParams().get(FROM_STATION),
                    command.state().getParams().get(TO_STATION),
                    command.state().getParams().get(DATE)));
                sendMessage(command.chatId(), getRoutes(command.state()));
                userStateRepository.get(command.chatId()).deleteCommand(getCommand());
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }

    private String getRoutes(UserState.CommandState commandState) {
        return routeService.getPrettyStringRoutes(
            commandState.getParams().get(FROM_STATION_CODE),
            commandState.getParams().get(TO_STATION_CODE),
            commandState.getParams().get(DATE));
    }
}
