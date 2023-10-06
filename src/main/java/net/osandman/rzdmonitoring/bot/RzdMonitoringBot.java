package net.osandman.rzdmonitoring.bot;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.bot.command.TelegramCommand;
import net.osandman.rzdmonitoring.bot.command.UnknownCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
public class RzdMonitoringBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUserName;
    List<TelegramCommand> commands;

    public RzdMonitoringBot(@Value("${bot.token}") String botToken,
                            List<TelegramCommand> commands) {
        super(botToken);
        this.commands = commands;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            for (TelegramCommand command : commands) {
                String messageText = update.getMessage().getText();
                if (command.getCommand().equalsIgnoreCase(messageText)) {
                    command.handleCommand(this, update);
                    return;
                }
            }
            new UnknownCommand().handleCommand(this, update);
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }
}
