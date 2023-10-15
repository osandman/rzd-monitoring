package net.osandman.rzdmonitoring.bot.command;

import net.osandman.rzdmonitoring.bot.UserState;
import net.osandman.rzdmonitoring.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TelegramCommand {
    protected TelegramLongPollingBot sender;
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    protected String command;
    public static Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    public final static String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    public String getCommandName() {
        return command;
    }

    public void handleCommand(TelegramLongPollingBot sender, Update update) {
        this.sender = sender;
        handleCommand(update);
    }

    protected abstract void handleCommand(Update update);

    protected void sendMessage(long chatId, String message) {
        int maxLength = 4096;
        List<String> messageBlocks = new ArrayList<>();
        while (message.length() > maxLength) {
            String block = message.substring(0, maxLength);
            messageBlocks.add(block);
            message = message.substring(maxLength);
        }
        if (!message.isEmpty()) {
            messageBlocks.add(message);
        }
        for (String block : messageBlocks) {
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), block);
            executeMessage(sendMessage);
            Utils.sleep(1000);
        }
    }

    protected void executeMessage(SendMessage sendMessage) {
        try {
            sender.execute(sendMessage);
            log.info("Сообщение '{}' отправлено пользователю, chatId={}", sendMessage.getText(), sendMessage.getChatId());
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    protected <T> void sendButtons(long chatId, String message, List<T> toButtons) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();
        List<KeyboardButton> buttons = new ArrayList<>();

        for (T button : toButtons) {
            buttons.add(new KeyboardButton(button.toString()));
            if (buttons.size() >= 4) {
                currentRow.addAll(buttons);
                keyboard.add(currentRow);
                buttons.clear();
                currentRow = new KeyboardRow();
            }
        }
        if (!buttons.isEmpty()) {
            currentRow.addAll(buttons);
            keyboard.add(currentRow);
        }
        keyboardMarkup.setKeyboard(keyboard);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);
        sendMessage.setReplyMarkup(keyboardMarkup);
        executeMessage(sendMessage);
    }

    @Override
    public String toString() {
        return getCommandName();
    }
}
