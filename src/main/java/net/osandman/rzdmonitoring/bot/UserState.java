package net.osandman.rzdmonitoring.bot;

import lombok.Data;
import net.osandman.rzdmonitoring.bot.command.ParamEnum;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserState {
    // String - команда ("/xxx")
    private final Map<String, CommandState> userStates = new HashMap<>();

    public CommandState getCommandState(String command) {
        return userStates.computeIfAbsent(command, k -> new CommandState());
    }

    @Data
    public static class CommandState {
        private int step = 1;
        private Map<ParamEnum, String> params = new HashMap<>();

        public void incrementStep() {
            this.step++;
        }

        public void addKey(ParamEnum key, String value) {
            params.put(key, value);
        }
    }
}

