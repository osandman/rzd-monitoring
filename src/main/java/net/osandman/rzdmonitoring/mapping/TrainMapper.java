package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.v2.train.RootTrainDto;
import net.osandman.rzdmonitoring.dto.train.TrainDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public interface TrainMapper {

    /**
     * Преобразует json ответа сервера в объект TrainDto.
     */
    @NonNull
    TrainDto map(RootTrainDto json);
}
