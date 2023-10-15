package net.osandman.rzdmonitoring.bot;

import net.osandman.rzdmonitoring.bot.command.TelegramCommand;
import net.osandman.rzdmonitoring.bot.command.UnknownCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class RzdMonitoringBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUserName;

    @Autowired
    List<TelegramCommand> commands;

    public RzdMonitoringBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String messageText = update.getMessage().getText();
            for (TelegramCommand command : commands) {
                if (messageText.equalsIgnoreCase(command.getCommandName())) {
                    TelegramCommand.userStates.remove(chatId);
                    command.handleCommand(this, update);
                    return;
                }
            }
            new UnknownCommand(this).handleCommand(this, update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            for (TelegramCommand command : commands) {
                if (!TelegramCommand.userStates.isEmpty() &&
                        TelegramCommand.userStates.get(chatId).getUserStates().containsKey(command.getCommandName())) {
                    command.handleCommand(this, update);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }
}
