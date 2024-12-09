package net.osandman.rzdmonitoring.service.notifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class TelegramNotifier implements Notifier {

    @Value("${bot.token}")
    private String botToken;

    @Override
    public void sendMessage(String message, Long chatId) {
        if (chatId == null) {
            log.warn("Сообщение не отправлено в телеграм, т.к. chatId={}", chatId);
            return;
        }
        try {
            URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendMessage");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String params = "chat_id=" + chatId + "&text=" + message;
            byte[] postData = params.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData);
            }

            int responseCode = connection.getResponseCode();
            log.info("В телеграм чат отправлено уведомление: '{}'. Response Code: '{}'", params, responseCode);

        } catch (IOException e) {
            log.error("Error from telegram notify: '{}'", e.getMessage());
        }
    }
}
