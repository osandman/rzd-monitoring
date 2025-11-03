package net.osandman.rzdmonitoring.api;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.service.AI.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AIController {

    private final ChatService chatService;

    @GetMapping
    public String getAnswer(@RequestParam String question) {
        return chatService.getStationAnswer(question);
    }

}
