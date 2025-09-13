package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.dto.TrainDto;
import net.osandman.rzdmonitoring.client.dto.v2.train.RootDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public interface TrainMapper {

    /**
     * Преобразует json ответа сервера в объект TrainDto.
     */
    @NonNull
    TrainDto map(RootDto json);
}
