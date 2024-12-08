package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.StationDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class FindStationsCommand extends AbstractTelegramCommand implements ITelegramCommand {

    @Override
    public Command getCommand() {
        return Command.STATIONS;
    }

    @Override
    public void handleCommand(Update update) {
        CommandContext command = buildCommandContext(update, getCommand());
        switch (command.state().getStep()) {
            case 1 -> { // начало команды
                sendMessage(command.chatId(), "Введите текст для поиска станции");
                command.state().incrementStep();
            }
            case 2 -> { // ввод вручную станции отправления
                findStationsAndIncrementStep(
                    command.messageText(), command.state(), 2, command.chatId()
                );
            }
            case 3 -> { // выбор из найденных станций
                StationDto stationDto = getStationDto(
                    command.messageText(), stationService.findStations(command.messageText())
                );
                if (stationDto == null) {
                    command.state().setStep(3);
                    sendMessage(
                        command.chatId(),
                        "Станция '%s' не найдена, выберите из списка".formatted(command.messageText())
                    );
                    return;
                }
                sendMessage(
                    command.chatId(),
                    "код=%s, статус=%d, уровень=%d".formatted(stationDto.code(), stationDto.state(), stationDto.level())
                );
            }
        }
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}
