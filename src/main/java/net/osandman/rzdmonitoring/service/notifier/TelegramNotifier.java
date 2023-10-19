package net.osandman.rzdmonitoring.service.notifier;

import  org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramNotifier implements Notifier {

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.chat-id}")
    private String chatId;

    @Override
    public void sendMessage(String message) {
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
            System.out.println("Response Code: " + responseCode);

            // Здесь можно обработать ответ от сервера, если это необходимо

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
