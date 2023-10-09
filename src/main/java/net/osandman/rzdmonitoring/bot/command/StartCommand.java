package net.osandman.rzdmonitoring.bot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class StartCommand extends TelegramCommand {
    private final List<TelegramCommand> commands;
    private final String MESSAGE = """
            Привет!
            Добро пожаловать, %s!
            Это бот для мониторинга билетов РЖД.
            Доступны команды:
            %s
            """;

    public StartCommand(List<TelegramCommand> commands) {
        this.commands = commands;
        command = CommandEnum.START.name;
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();
        String allCommands = String.join(System.lineSeparator(),
                commands.stream().map(TelegramCommand::getCommand).toList());
        String message = MESSAGE.formatted(userName, allCommands);
        sendMessage(chatId, message);
    }
}
