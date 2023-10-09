package net.osandman.rzdmonitoring.bot.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UnknownCommand extends TelegramCommand {
    public UnknownCommand(TelegramLongPollingBot sender) {
        this.sender = sender;
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Команда не найдена");
    }
}
