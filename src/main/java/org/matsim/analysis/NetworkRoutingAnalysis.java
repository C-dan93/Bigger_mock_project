package org.matsim.analysis;

import org.matsim.core.events.EventsUtils; //This gives us MATSim events tool

public class NetworkRoutingAnalysis {
    public static void main(String[] args) {
        // Create handler for processing routing events
        var handler = new NetworkRoutingEventHandler();
        // create events manager to cor--ordinate events processing
        var manager = EventsUtils.createEventsManager();
        //connect handler to manager
        manager.addHandler(handler);
        // Read and process events from simulation output
        EventsUtils.readEvents(manager,
                "C:\\Users\\emerald\\IdeaProjects\\matsim-serengeti-park-hodenhagen\\scenarios\\serengeti-park-v1.0\\input\\scenarios\\serengeti-park-v1.0\\output\\output-serengeti-park-v1.0-run1\\serengeti-park-v1.0-run1.output_events.xml.gz"         );
    }
}


