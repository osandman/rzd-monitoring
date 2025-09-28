package net.osandman.rzdmonitoring.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.osandman.rzdmonitoring.bot.command.Command;
import net.osandman.rzdmonitoring.bot.command.ParamType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public void deleteAllCommands() {
        userStates.clear();
    }

    @Data
    public static class CommandState {
        private int step = 1;
        private Map<ParamType, String> params = new HashMap<>();
        private Map<MultiSelectType, MultiSelect> multiSelectParams = new HashMap<>();

        public MultiSelect createMultiSelectParam(MultiSelectType type, String initialMessage) {
            return multiSelectParams.computeIfAbsent(type, k -> new MultiSelect(initialMessage));
        }

        public MultiSelect getMultiSelectParam(MultiSelectType type) {
            return multiSelectParams.get(type);
        }

        public void deleteMultiSelectParam(MultiSelectType type) {
            multiSelectParams.remove(type);
        }

        public void deleteAllMultiSelectParams() {
            multiSelectParams.clear();
        }

        public void incrementStep() {
            this.step++;
        }

        public void decrementStep() {
            this.step--;
        }

        public void addKey(ParamType key, String value) {
            params.put(key, value);
        }
    }

    @Getter
    @Setter
    public static class MultiSelect {
        private String initialMessage;
        private Set<String> selectedOptions = new HashSet<>();
        private Integer messageId;

        public MultiSelect(String initialMessage) {
            this.initialMessage = initialMessage;
        }

        public String getCurrentText() {
            if (selectedOptions.isEmpty()) {
                return initialMessage;
            }
            return initialMessage + "\n\nВыбрано: " + String.join(", ", selectedOptions);
        }

        public void toggleOption(String option) {
            if (selectedOptions.contains(option)) {
                selectedOptions.remove(option);
            } else {
                selectedOptions.add(option);
            }
        }
    }
}

