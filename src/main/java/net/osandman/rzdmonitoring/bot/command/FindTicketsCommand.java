package net.osandman.rzdmonitoring.bot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class FindTicketsCommand extends TelegramCommand {
    public FindTicketsCommand() {
        command = CommandEnum.TICKETS.name;
    }

    @Override
    public void handleCommand(TelegramLongPollingBot sender, Update update) {
        super.handleCommand(sender, update);
        sendMessage(sender, "Еще не готовы у пони подковы ...");
    }
}
