package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindStationsCommand extends AbstractTelegramCommand {

    @Override
    public Command getCommand() {
        return Command.STATIONS;
    }

    @Override
    public void handleCommand(Update update) {
        CommandContext command = buildCommandContext(update, getCommand());
        switch (command.state().getStep()) {
            case 1 -> { // начало команды
                sendMessage(command.chatId(), "Введите текст для поиска станции", true);
                command.state().incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
            }
            case 3 -> { // выбор из найденных станций
                List<StationDto> stations = command.state().getAdditionalObject(
                    ParamType.STATIONS, StationDto.class
                );
                StationDto stationDto = stations.stream()
                    .filter(dto -> dto.name().equalsIgnoreCase(command.messageText()))
                    .findAny().orElse(null);
                if (stationDto == null) {
                    findAndShowStationsAndIncrementStep(command.messageText(), command.state(), command.chatId());
                    command.state().decrementStep();
                    return;
                }
                sendMessage(command.chatId(), stationDto.printStr());
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}
