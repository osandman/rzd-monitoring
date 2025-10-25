package net.osandman.rzdmonitoring.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.osandman.rzdmonitoring.bot.command.Command;
import net.osandman.rzdmonitoring.bot.command.ParamType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Data
public class UserState {
    // String - команда ("/xxx")
    private final Map<Command, CommandState> userStates = new ConcurrentHashMap<>();

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
        private Map<ParamType, String> params = new ConcurrentHashMap<>();
        private Map<ParamType, List<?>> additionalObjects = new ConcurrentHashMap<>();
        private Map<MultiSelectType, MultiSelect> multiSelectParams = new ConcurrentHashMap<>();

        public MultiSelect getOrCreateMultiSelectParam(MultiSelectType type, String initialMessage) {
            return multiSelectParams.computeIfAbsent(type, k -> new MultiSelect(initialMessage));
        }

        public String getParam(ParamType type) {
            return params.get(type);
        }

        public <T> List<T> getAdditionalObject(ParamType type, Class<T> clazz) {
            return Optional.ofNullable(additionalObjects.get(type))
                .map(list -> list.stream()
                    .map(clazz::cast)
                    .toList())
                .orElse(List.of());
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
        private List<String> selectedOptions = new ArrayList<>();
        private Integer messageId;
        private Map<Integer, String> indexToOptionMap = new HashMap<>();

        // Методы для работы с маппингом
        public void setIndexToOptionMapping(Map<Integer, String> mapping) {
            this.indexToOptionMap = mapping;
        }

        public String getOptionByIndex(Integer index) {
            return indexToOptionMap.get(index);
        }

        public MultiSelect(String initialMessage) {
            this.initialMessage = initialMessage;
        }

        public String getSelectedText() {
            if (selectedOptions.isEmpty()) {
                return initialMessage;
            }
            return String.join(", ", selectedOptions);
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

