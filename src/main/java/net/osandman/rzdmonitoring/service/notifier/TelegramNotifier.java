package net.osandman.rzdmonitoring.service.notifier;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramNotifier implements Notifier {
    private final String BOT_TOKEN = "6668327388:AAHlwE8ho1gfEhyNg5UhF5rdl_vDKgAHz1w";
    private final String CHAT_ID = "282026575";

    @Override
    public void sendMessage(String message) {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String params = "chat_id=" + CHAT_ID + "&text=" + message;
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
