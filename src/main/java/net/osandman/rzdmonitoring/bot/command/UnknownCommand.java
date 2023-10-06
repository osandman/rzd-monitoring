package net.osandman.rzdmonitoring.bot.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UnknownCommand extends TelegramCommand {
    @Override
    public void handleCommand(TelegramLongPollingBot sender, Update update) {
        super.handleCommand(sender, update);
        super.sendMessage(sender, "Команда не найдена");
    }
}
