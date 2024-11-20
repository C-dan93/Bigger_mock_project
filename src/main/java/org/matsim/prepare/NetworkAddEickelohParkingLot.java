/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.prepare;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Adds Eickeloh parking lot to the network
 */
public class NetworkAddEickelohParkingLot {
    private static final String INPUT_NETWORK = "./scenarios/serengeti-park-v1.0/input/serengeti-park-network-v1.0.xml.gz";
    private static final String OUTPUT_NETWORK = "./scenarios/serengeti-park-v1.0/input/serengeti-park-network-with-eickeloh-v1.0.xml.gz";

    private static final Set<String> ALLOWED_MODES = Collections.singleton(TransportMode.car);
    private static final double DEFAULT_LENGTH = 1.0;
    private static final double DEFAULT_CAPACITY = 1.0;
    private static final double DEFAULT_LANES = 1.0;
    private static final double DEFAULT_FREESPEED = 1.0;
    private static final long EXISTING_NODE_ID = 99999;
    private static final long NEW_NODE_ID = 1000001;

    public static void main(String[] args) {
        Network network = NetworkUtils.readNetwork(INPUT_NETWORK);
        addParkingLotToNetwork(network);
        NetworkUtils.writeNetwork(network, OUTPUT_NETWORK);
    }

    private static void addParkingLotToNetwork(Network network) {
        NetworkFactory factory = network.getFactory();
        Node existingNode = network.getNodes().get(Id.createNodeId(EXISTING_NODE_ID));
        Node parkingNode = createParkingLotNode(network);

        // Create access links to parking lot
        createParkingLotLinks(network, factory, existingNode, parkingNode);
    }

    private static Node createParkingLotNode(Network network) {
        return NetworkUtils.createAndAddNode(network,
                Id.createNodeId(NEW_NODE_ID),
                new Coord(546546546, 45465546));
    }

    private static void createParkingLotLinks(Network network, NetworkFactory factory,
                                              Node fromNode, Node toNode) {
        // Create main access link
        Link accessLink = factory.createLink(
                Id.createLinkId("zufahrtZumParkplatz"),
                fromNode,
                toNode);

        configureLink(accessLink);
        network.addLink(accessLink);

        // Create alternative access link using NetworkUtils
        NetworkUtils.createAndAddLink(network,
                        Id.createLinkId(1),
                        fromNode,
                        toNode,
                        DEFAULT_LENGTH,
                        DEFAULT_FREESPEED,
                        DEFAULT_CAPACITY,
                        DEFAULT_LANES)
                .setAllowedModes(ALLOWED_MODES);
    }

    private static void configureLink(Link link) {
        link.setLength(DEFAULT_LENGTH);
        link.setCapacity(DEFAULT_CAPACITY);
        link.setAllowedModes(ALLOWED_MODES);
        link.setNumberOfLanes(DEFAULT_LANES);
        link.setFreespeed(DEFAULT_FREESPEED);
    }
}