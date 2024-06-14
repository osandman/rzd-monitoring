package net.osandman.rzdmonitoring.repository;

import net.osandman.rzdmonitoring.entity.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateRepositoryInMem implements UserStateRepository {

    private final static Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    @Override
    public UserState get(long chatId) {
        return userStates.get(chatId);
    }

    @Override
    public UserState getOrCreate(long chatId) {
        return userStates.computeIfAbsent(chatId, aLong -> new UserState());
    }

    @Override
    public boolean remove(long chatId) {
        return userStates.remove(chatId) != null;
    }

    @Override
    public UserState add(long chatId, UserState userState) {
        return userStates.computeIfPresent(chatId, (id, state) -> userState);
    }
}