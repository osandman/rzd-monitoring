package net.osandman.rzdmonitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RzdRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> void onError(
        RetryContext context, RetryCallback<T, E> callback, Throwable throwable
    ) {
        log.warn("Попытка №{} запроса не удалась, ошибка: '{}', exception: '{}'",
            context.getRetryCount(), throwable.getMessage(), throwable.getClass().getCanonicalName());
    }
}
