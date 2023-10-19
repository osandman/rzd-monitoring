package net.osandman.rzdmonitoring.mapping;

import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;

public interface Mapper {

    String routesMapping(RootRoute route);

    void ticketsMapping(RootTrain train);
}
