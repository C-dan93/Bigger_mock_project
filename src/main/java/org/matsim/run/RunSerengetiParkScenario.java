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

package org.matsim.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.lanes.Lane;
import org.matsim.lanes.LanesFactory;
import org.matsim.lanes.LanesToLinkAssignment;
import org.matsim.prepare.population.CreatePopulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class for running the Serengeti Park scenario simulation
 * @author ikaddoura
 */
public final class RunSerengetiParkScenario {
    private static final Logger log = LogManager.getLogger(RunSerengetiParkScenario.class);

    // Demand constants
    @SuppressWarnings("unused")
    private static final double CAR_OCCUPANCY_RATE = 4.0;
    @SuppressWarnings("unused")
    private static final double CONVERSION_FACTOR = 0.9;
    private static final int SERENGETI_PARK_VEHICLES = 1000;
    private static final int SERENGETI_CARPARK_VEHICLES = 675;
    private static final int WASSERLAND_CARPARK_VEHICLES = 675;

    // Supply constants
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_VEHICLE = 45;
    private static final int CAPACITY_PER_CHECKIN_BOOTH = SECONDS_PER_HOUR / SECONDS_PER_VEHICLE;
    private static final int NORTH_CHECKIN_BOOTHS = 4;
    private static final int SOUTH_CHECKIN_BOOTHS = 3;

    // Lane constants
    private static final double LANE_CAPACITY = 720.0;
    private static final double MAIN_LANE_LENGTH = 165.67285516126265;
    private static final String DEFAULT_CONFIG_PATH = "./scenarios/serengeti-park-v1.0/input/serengeti-park-config-v1.0.xml";

    private RunSerengetiParkScenario() {
        // Private constructor to prevent instantiation
    }

    public static void main(String[] args) throws IOException {
        args = args.length == 0 ? new String[]{DEFAULT_CONFIG_PATH} : args;
        Arrays.stream(args).forEach(log::info);

        Config config = prepareConfig(args);
        Scenario scenario = prepareScenario(config);
        prepareControler(scenario).run();
    }

    public static Controler prepareControler(Scenario scenario) {
        Gbl.assertNotNull(scenario);
        return new Controler(scenario);
    }

    public static Scenario prepareScenario(Config config) throws IOException {
        Gbl.assertNotNull(config);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        configureRestrictedLinks(scenario);
        configureCheckInLinks(scenario);
        configureLanes(scenario);
        createPopulation(scenario);

        return scenario;
    }

    private static void configureRestrictedLinks(Scenario scenario) {
        Set<Id<Link>> forCarsRestrictedLinks = new HashSet<>(Arrays.asList(
                Id.createLinkId("3622817410000f"), Id.createLinkId("3622817410000r"),
                Id.createLinkId("3622817520000f"), Id.createLinkId("3622817520000r"),
                Id.createLinkId("7232641180000f")
        ));

        for (Link link : scenario.getNetwork().getLinks().values()) {
            if (forCarsRestrictedLinks.contains(link.getId())) {
                restrictLink(link);
            }
        }
    }

    private static void restrictLink(Link link) {
        link.setFreespeed(0.001);
        link.setCapacity(0.);
    }

    private static void configureCheckInLinks(Scenario scenario) {
        Set<Id<Link>> kassenLinks = new HashSet<>(Arrays.asList(
                Id.createLinkId("3624560720003f"),
                Id.createLinkId("3624560680002f"),
                Id.createLinkId("3624560690002f"),
                Id.createLinkId("3624560660002f"),
                Id.createLinkId("5297562640002f"),
                Id.createLinkId("2184588460002f"),
                Id.createLinkId("2184588440002f")
        ));

        for (Link link : scenario.getNetwork().getLinks().values()) {
            if (kassenLinks.contains(link.getId())) {
                restrictLink(link);
            }

            configureSpecificCheckInLinks(link);
        }
    }

    private static void configureSpecificCheckInLinks(Link link) {
        switch (link.getId().toString()) {
            case "3624560720003f":
                configureNorthCheckIn(link);
                break;
            case "5297562640002f":
                configureSouthCheckIn(link);
                break;
        }
    }

    private static void configureNorthCheckIn(Link link) {
        link.setCapacity(CAPACITY_PER_CHECKIN_BOOTH * NORTH_CHECKIN_BOOTHS);
        link.setFreespeed(2.7777);
        link.setLength(30. * (NORTH_CHECKIN_BOOTHS - 1));
        link.setNumberOfLanes(NORTH_CHECKIN_BOOTHS);
    }

    private static void configureSouthCheckIn(Link link) {
        link.setCapacity(CAPACITY_PER_CHECKIN_BOOTH * SOUTH_CHECKIN_BOOTHS);
        link.setFreespeed(2.7777);
        link.setLength(40. * (SOUTH_CHECKIN_BOOTHS - 1));
        link.setNumberOfLanes(SOUTH_CHECKIN_BOOTHS);
    }

    private static void configureLanes(Scenario scenario) {
        Id<Link> linkIdBeforeIntersection = Id.createLinkId("1325764790002f");
        Id<Link> nextLinkIdLeftTurn = Id.createLinkId("3624560720000f");
        Id<Link> nextLinkIdStraight = Id.createLinkId("1325764790003f");
        Id<Lane> leftTurnLaneId = Id.create("1325764790002f_left", Lane.class);
        Id<Lane> straightLaneId = Id.create("1325764790002f_straight", Lane.class);

        LanesFactory factory = scenario.getLanes().getFactory();
        createLaneAssignment(scenario, factory, linkIdBeforeIntersection, nextLinkIdLeftTurn,
                nextLinkIdStraight, leftTurnLaneId, straightLaneId);
    }

    private static void createLaneAssignment(Scenario scenario, LanesFactory factory,
                                             Id<Link> linkId, Id<Link> leftTurnLink, Id<Link> straightLink,
                                             Id<Lane> leftTurnLaneId, Id<Lane> straightLaneId) {
        LanesToLinkAssignment laneLinkAssignment = factory.createLanesToLinkAssignment(linkId);

        // Create input lane
        Lane laneIn = createInputLane(factory, leftTurnLaneId, straightLaneId);
        laneLinkAssignment.addLane(laneIn);

        // Create left turn lane
        Lane leftTurnLane = createLeftTurnLane(factory, leftTurnLaneId, leftTurnLink);
        laneLinkAssignment.addLane(leftTurnLane);

        // Create straight lane
        Lane straightLane = createStraightLane(factory, straightLaneId, straightLink);
        laneLinkAssignment.addLane(straightLane);

        scenario.getLanes().addLanesToLinkAssignment(laneLinkAssignment);
    }

    private static Lane createInputLane(LanesFactory factory, Id<Lane> leftTurnLaneId, Id<Lane> straightLaneId) {
        Lane lane = factory.createLane(Id.create("1325764790002f_in", Lane.class));
        lane.addToLaneId(leftTurnLaneId);
        lane.addToLaneId(straightLaneId);
        lane.setStartsAtMeterFromLinkEnd(MAIN_LANE_LENGTH);
        lane.setCapacityVehiclesPerHour(LANE_CAPACITY * 4);
        lane.setNumberOfRepresentedLanes(4.0);
        return lane;
    }

    private static Lane createLeftTurnLane(LanesFactory factory, Id<Lane> laneId, Id<Link> nextLink) {
        Lane lane = factory.createLane(laneId);
        lane.addToLinkId(nextLink);
        lane.setStartsAtMeterFromLinkEnd(MAIN_LANE_LENGTH - 1);
        lane.setCapacityVehiclesPerHour(LANE_CAPACITY);
        return lane;
    }

    private static Lane createStraightLane(LanesFactory factory, Id<Lane> laneId, Id<Link> nextLink) {
        Lane lane = factory.createLane(laneId);
        lane.addToLinkId(nextLink);
        lane.setStartsAtMeterFromLinkEnd(MAIN_LANE_LENGTH - 1);
        lane.setCapacityVehiclesPerHour(LANE_CAPACITY * 3.0);
        lane.setNumberOfRepresentedLanes(3.0);
        return lane;
    }

    private static void createPopulation(Scenario scenario) throws IOException {
        CreatePopulation createPopulation = new CreatePopulation(
                SERENGETI_PARK_VEHICLES,
                SERENGETI_CARPARK_VEHICLES,
                WASSERLAND_CARPARK_VEHICLES);
        createPopulation.run(scenario);
    }

    public static Config prepareConfig(String[] args, ConfigGroup... customModules) {
        OutputDirectoryLogging.catchLogEntries();
        String[] typedArgs = Arrays.copyOfRange(args, 1, args.length);
        Config config = ConfigUtils.loadConfig(args[0], customModules);

        config.routing().setRoutingRandomness(0.);
        config.routing().setAccessEgressType(RoutingConfigGroup.AccessEgressType.accessEgressModeToLink);
        config.qsim().setUsingTravelTimeCheckInTeleportation(true);

        ConfigUtils.applyCommandline(config, typedArgs);
        return config;
    }
}