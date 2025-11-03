package net.osandman.rzdmonitoring.service.AI;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Qualifier("defaultChatClient")
    private final ChatClient chatClient;

    private static final String PROMPT = """
        Проанализируй данные о населённом пункте/станции/вокзале "{%s}" и напиши
        ОДИН короткий интересный факт об этом месте (1-2 предложения), факт должен быть достоверным и информативным,
        если не удалось найти данные то ничего не пиши,
        если найдено несколько населенных пунктов с одинаковыми названиями то выбери крупнейший из них
        """;

    public String getStationAnswer(String station) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt().user(PROMPT.formatted(station));
        return requestSpec.call().content();
    }
}
