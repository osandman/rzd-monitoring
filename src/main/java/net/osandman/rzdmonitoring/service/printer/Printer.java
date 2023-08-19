package net.osandman.rzdmonitoring.service.printer;

import net.osandman.rzdmonitoring.dto.route.RootRoute;
import net.osandman.rzdmonitoring.dto.train.RootTrain;

public interface Printer {

    void printRoute(RootRoute route);

    void printTickets(RootTrain train);

}
