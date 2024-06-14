package net.osandman.rzdmonitoring.repository;

import net.osandman.rzdmonitoring.entity.UserState;

public interface UserStateRepository {

    UserState get(long chatId);

    UserState getOrCreate(long chatId);

    boolean remove(long chatId);

    UserState add(long chatId, UserState userState);
}