package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TelegramCommand {
    protected TelegramLongPollingBot sender;
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    protected String command;
    public static Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    public final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    public String getCommand() {
        return command;
    }

    public void handleCommand(TelegramLongPollingBot sender, Update update) {
        this.sender = sender;
        handleCommand(update);
    }

    protected abstract void handleCommand(Update update);

    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        try {
            sender.execute(sendMessage);
            log.info("Сообщение '{}' отправлено пользователю, chatId={}", sendMessage.getText(), chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}
