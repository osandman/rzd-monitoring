package net.osandman.rzdmonitoring.config;

import net.osandman.rzdmonitoring.bot.RzdMonitoringBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class RzdMonitoringBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(RzdMonitoringBot rzdMonitoringBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(rzdMonitoringBot);
        return api;
    }
}
