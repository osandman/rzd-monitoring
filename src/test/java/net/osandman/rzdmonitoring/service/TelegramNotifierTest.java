package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.service.notifier.Notifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TelegramNotifierTest {

    @Autowired
    Notifier notifier;

    @Test
    void messageIsSend() {
        notifier.sendMessage("test message");
    }
}