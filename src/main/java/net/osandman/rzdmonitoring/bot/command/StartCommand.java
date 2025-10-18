package net.osandman.rzdmonitoring.bot.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class StartCommand extends AbstractTelegramCommand {

    @Qualifier("sortedTelegramCommands")
    @Lazy
    private final List<ITelegramCommand> telegramCommands;
    private final static String START_MESSAGE = """
        Привет!
        Добро пожаловать, %s!
        Это бот для получения информации о маршрутах и билетах РЖД.
        Доступны команды:
        %s
        """;

    @Override
    public Command getCommand() {
        return Command.START;
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        Chat chat = update.getMessage().getChat();
        String firstName = hasText(chat.getFirstName()) ? chat.getFirstName() : "";
        String lastName = hasText(chat.getLastName()) ? chat.getLastName() : "";
        String fullName = hasText(firstName + lastName)
            ? String.join(" ", firstName, lastName).trim()
            : chat.getUserName();
        String allCommands = String.join(System.lineSeparator(),
            telegramCommands.stream()
                .filter(ITelegramCommand::canToShow)
                .map(telegramCommand ->
                    telegramCommand.getCommand().getCommandStr() + " - " + telegramCommand.getCommand().getDesc()
                )
                .toList()
        );
        String message = START_MESSAGE.formatted(fullName, allCommands);
        sendMessage(chatId, message, true);
        sendButtons(chatId, "Выберите опцию:",
            telegramCommands.stream()
                .filter(ITelegramCommand::canToShow)
                .map(telegramCommand -> telegramCommand.getCommand().getCommandStr())
                .toList());
    }

    @Override
    public boolean canToShow() {
        return true;
    }
}