package net.osandman.rzdmonitoring.bot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class TelegramCommand {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    protected String command;
    protected String userName;
    protected long chatId;

    public String getCommand() {
        return command;
    }

    public void handleCommand(TelegramLongPollingBot sender, Update update) {
        chatId = update.getMessage().getChatId();
        userName = update.getMessage().getChat().getUserName();
        log.info("Сообщение '{}' получено от пользователя {}, chatId={}", update.getMessage().getText(), userName, chatId);
    }

    protected void sendMessage(TelegramLongPollingBot sender, String message) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        try {
            sender.execute(sendMessage);
            log.info("Сообщение '{}' отправлено пользователю {}, chatId={}", sendMessage.getText(), userName, chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}
