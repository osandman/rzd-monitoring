package net.osandman.rzdmonitoring.entity;

import lombok.Data;
import lombok.Getter;
import net.osandman.rzdmonitoring.bot.command.Command;
import net.osandman.rzdmonitoring.bot.command.ParamEnum;

import java.util.HashMap;
import java.util.Map;

@Getter
@Data
public class UserState {
    // String - команда ("/xxx")
    private final Map<Command, CommandState> userStates = new HashMap<>();

    public CommandState getOrCreateCommandState(Command command) {
        return userStates.computeIfAbsent(command, k -> new CommandState());
    }

    public void deleteCommand(Command command) {
        userStates.remove(command);
    }

    public void deleteAll() {
        userStates.clear();
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

