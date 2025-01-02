package net.osandman.rzdmonitoring.config;

import net.osandman.rzdmonitoring.bot.RzdMonitoringBot;
import net.osandman.rzdmonitoring.bot.command.ITelegramCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Comparator;
import java.util.List;

@Configuration
public class RzdMonitoringBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(RzdMonitoringBot rzdMonitoringBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(rzdMonitoringBot);
        return api;
    }

    @Bean(name = "sortedTelegramCommands")
    public List<ITelegramCommand> telegramCommands(List<ITelegramCommand> commandList) {
        return commandList.stream()
            .sorted(Comparator.comparingInt(iTelegramCommand -> iTelegramCommand.getCommand().getOrder()))
            .toList();
    }
}
