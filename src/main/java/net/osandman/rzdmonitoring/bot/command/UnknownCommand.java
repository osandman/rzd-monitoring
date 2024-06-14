package net.osandman.rzdmonitoring.bot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UnknownCommand extends AbstractTelegramCommand implements ITelegramCommand {

    public static final String UNKNOWN_COMMAND = "unknown";

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Команда не найдена");
    }

    @Override
    public String getCommand() {
        return UNKNOWN_COMMAND;
    }

    @Override
    public boolean canToShow() {
        return false;
    }
}
