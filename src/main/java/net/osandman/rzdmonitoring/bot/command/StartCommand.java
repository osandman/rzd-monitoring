package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartCommand extends AbstractTelegramCommand implements ITelegramCommand {

    private final List<ITelegramCommand> commands;
    private final static String MESSAGE = """
        Привет!
        Добро пожаловать, %s!
        Это бот для получения информации о маршрутах и билетах РЖД.
        Доступны команды:
        %s
        """;

    @Override
    public String getCommand() {
        return CommandEnum.START.getCommand();
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        Chat chat = update.getMessage().getChat();
        String fullName = chat.getFirstName() + " " + chat.getLastName();
        String allCommands = String.join(System.lineSeparator(),
            commands.stream()
                .filter(ITelegramCommand::canToShow)
                .map(ITelegramCommand::getCommand).toList()
        );
        String message = MESSAGE.formatted(fullName, allCommands);
        sendMessage(chatId, message);
        sendButtons(chatId, "Выберите опцию:",
            commands.stream()
                .filter(ITelegramCommand::canToShow)
                .map(ITelegramCommand::getCommand)
                .toList());
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}