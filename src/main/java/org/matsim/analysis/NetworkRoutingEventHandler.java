package org.matsim.analysis;

//import necessary libraries

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;

public class NetworkRoutingEventHandler implements LinkEnterEventHandler,
        LinkLeaveEventHandler,
        PersonArrivalEventHandler {

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        System.out.println("Link enter:" +
                linkEnterEvent.getTime() + ":" +
                linkEnterEvent.getVehicleId() + ":" +
                linkEnterEvent.getLinkId());
    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {
        System.out.println("Link Leave:" +
                linkLeaveEvent.getTime() + ":" +
                linkLeaveEvent.getVehicleId() + ":" +
                linkLeaveEvent.getLinkId());
    }

    @Override
    public void handleEvent(PersonArrivalEvent personArrivalEvent) {
        System.out.println("Route Complete:" +
                personArrivalEvent.getTime() + ":" +
                personArrivalEvent.getPersonId() + ":" +
                personArrivalEvent.getLinkId());
    }
}
