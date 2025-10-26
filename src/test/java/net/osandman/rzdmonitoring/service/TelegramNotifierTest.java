package net.osandman.rzdmonitoring.service;

import lombok.SneakyThrows;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled
class TelegramNotifierTest {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifierTest.class);

    @Autowired
    Notifier notifier;

    @Value("${bot.token}")
    String botToken;

    @Value("${bot.chat-id}")
    private Long chatId;

    @Test
    void messageIsSend() {
        notifier.sendMessage("test message", chatId);
    }

    @Test
    void send() {
        sendMessage("282026575", "testtest");
    }

    @SneakyThrows
    public void sendMessage(String chatId, String message) {

        String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        String params = "chat_id=" + chatId + "&text=" + message;
        byte[] postData = params.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData);
        }
        int responseCode = connection.getResponseCode();
        log.info("Сообщение отправлено в Telegram чат: '{}'. Код ответа: '{}'", params, responseCode);

        // Если нужно обработать ответ от сервера
        if (responseCode == HttpURLConnection.HTTP_OK) {
            log.info("Сообщение успешно отправлено");
        } else {
            log.error("Ошибка при отправке сообщения. Код ответа: {}", responseCode);
        }
    }
}