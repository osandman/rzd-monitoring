package net.osandman.rzdmonitoring.bot;

import net.osandman.rzdmonitoring.bot.command.ITelegramCommand;
import net.osandman.rzdmonitoring.entity.UserState;
import net.osandman.rzdmonitoring.repository.UserStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static net.osandman.rzdmonitoring.bot.command.UnknownCommand.UNKNOWN_COMMAND;

@Component
public class RzdMonitoringBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUserName;

    private final List<ITelegramCommand> commands;
    private final UserStateRepository userStateRepository;

    @Autowired
    public RzdMonitoringBot(
        @Value("${bot.token}") String botToken,
        List<ITelegramCommand> commands, UserStateRepository userStateRepository
    ) {
        super(botToken);
        this.commands = commands;
        this.userStateRepository = userStateRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String messageText = update.getMessage().getText();
            for (ITelegramCommand command : commands) {
                if (messageText.equalsIgnoreCase(command.getCommand())) {
                    userStateRepository.remove(chatId);
                    command.handleCommand(update);
                    return;
                }
            }
            commands.stream()
                .filter(command -> command.getCommand().equalsIgnoreCase(UNKNOWN_COMMAND))
                .findAny().ifPresent(command -> command.handleCommand(update));
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            for (ITelegramCommand command : commands) {
                UserState userState = userStateRepository.get(chatId);
                if (userState != null && userState.getUserStates().containsKey(command.getCommand())) {
                    command.handleCommand(update);
                    return;
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }
}
