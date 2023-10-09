package net.osandman.rzdmonitoring.bot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class FindTicketsCommand extends TelegramCommand {
    public FindTicketsCommand() {
        command = CommandEnum.TICKETS.name;
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Еще не готовы у пони подковы ...");
    }
}
