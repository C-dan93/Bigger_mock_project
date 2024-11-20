/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Creates population for Serengeti Park simulation
 * @author ikaddoura
 */
public class CreatePopulation {
    private static final Logger log = LogManager.getLogger(CreatePopulation.class);

    // Distribution constants
    private static final double MOTORWAY_DISTRIBUTION = 0.8;
    private static final double NORTH_DISTRIBUTION = 0.1;
    private static final double HODENHAGEN_DISTRIBUTION = 0.1;

    // Time constants
    private static final int HOUR_IN_SECONDS = 3600;
    private static final double MEAN_ARRIVAL_TIME = 11 * HOUR_IN_SECONDS;
    private static final double TIME_VARIANCE = HOUR_IN_SECONDS;

    // Activity and transport mode types
    private static final String ACTIVITY_TYPE = "park";
    private static final String HOME_ACTIVITY = "home";
    private static final String CAR_MODE = "car";

    // Destination types and file paths
    private static final String SERENGETI_PARKPLATZ = "serengetiParkplatz";
    private static final String WASSERLAND_PARKPLATZ = "wasserlandParkplatz";
    private static final String SERENGETI_PARK = "serengetiPark";

    private static final String SERENGETI_PARKPLATZ_SHP = "./original-input-data/shp-files/serengeti-parkplatz/serengeti-parkplatz.shp";
    private static final String WASSERLAND_PARKPLATZ_SHP = "./original-input-data/shp-files/wasserland-parkplatz/wasserland-parkplatz.shp";
    private static final String SERENGETI_PARK_SHP = "./original-input-data/shp-files/serengeti-park/serengeti-park.shp";

    private int personCounter = 0;
    private final Map<String, SimpleFeature> features = new HashMap<>();
    private final Map<Id<Link>, Integer> linkId2numberOfVisitorsSerengetiParkplatz = new HashMap<>();
    private final Map<Id<Link>, Integer> linkId2numberOfVisitorsWasserland = new HashMap<>();
    private final Map<Id<Link>, Integer> linkId2numberOfVisitorsSerengetiPark = new HashMap<>();

    public static void main(String[] args) throws IOException {
        final String networkFile = "./scenarios/serengeti-park-v1.0/input/serengeti-park-network-v1.0.xml.gz";
        final String outputFilePopulation = "./scenarios/serengeti-park-v1.0/input/serengeti-park-population-v1.0.xml.gz";

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(networkFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        CreatePopulation popGenerator = new CreatePopulation(1000, 675, 1569);
        popGenerator.run(scenario);

        new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(outputFilePopulation);
        log.info("Population written to: {}", outputFilePopulation);
    }

    public CreatePopulation(int numberOfSafariVisitors, int safariParkplatzVisitors, int wasserlandParkplatzVisitors) throws IOException {
        initializeVisitorDistribution(numberOfSafariVisitors, safariParkplatzVisitors, wasserlandParkplatzVisitors);
        loadShapeFiles();
    }

    private void initializeVisitorDistribution(int numberOfSafariVisitors, int safariParkplatzVisitors, int wasserlandParkplatzVisitors) {
        distributeVisitors(linkId2numberOfVisitorsSerengetiParkplatz, safariParkplatzVisitors);
        distributeVisitors(linkId2numberOfVisitorsWasserland, wasserlandParkplatzVisitors);
        distributeVisitors(linkId2numberOfVisitorsSerengetiPark, numberOfSafariVisitors);
    }

    private void distributeVisitors(Map<Id<Link>, Integer> distribution, int totalVisitors) {
        distribution.put(Id.createLinkId("2344590910000r"), (int) (totalVisitors * MOTORWAY_DISTRIBUTION));
        distribution.put(Id.createLinkId("44371520007f"), (int) (totalVisitors * NORTH_DISTRIBUTION));
        distribution.put(Id.createLinkId("377320760000r"), (int) (totalVisitors * HODENHAGEN_DISTRIBUTION));
    }

    private void loadShapeFiles() throws IOException {
        log.info("Reading shp files...");
        loadShapeFile(SERENGETI_PARKPLATZ_SHP, SERENGETI_PARKPLATZ);
        loadShapeFile(WASSERLAND_PARKPLATZ_SHP, WASSERLAND_PARKPLATZ);
        loadShapeFile(SERENGETI_PARK_SHP, SERENGETI_PARK);
        log.info("Reading shp files... Done.");
    }

    private void loadShapeFile(String shpFile, String featureKey) throws IOException {
        File file = new File(shpFile);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        try {
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
            try (SimpleFeatureIterator it = source.getFeatures().features()) {
                while (it.hasNext()) {
                    features.put(featureKey, it.next());
                }
            }
        } finally {
            dataStore.dispose();
        }
    }

    public Scenario run(Scenario scenario) {
        Random rnd = MatsimRandom.getRandom();

        createVisitorsForDestination(scenario, rnd, linkId2numberOfVisitorsSerengetiParkplatz, SERENGETI_PARKPLATZ);
        createVisitorsForDestination(scenario, rnd, linkId2numberOfVisitorsWasserland, WASSERLAND_PARKPLATZ);
        createVisitorsForDestination(scenario, rnd, linkId2numberOfVisitorsSerengetiPark, SERENGETI_PARK);

        log.info("Population contains {} agents.", personCounter);
        return scenario;
    }

    private void createVisitorsForDestination(Scenario scenario, Random rnd,
                                              Map<Id<Link>, Integer> visitorDistribution, String type) {
        visitorDistribution.forEach((linkId, count) ->
                createVisitors(scenario, rnd, linkId, count, type));
    }

    private void createVisitors(Scenario scenario, Random rnd, Id<Link> linkId, int visitorCount, String type) {
        Population population = scenario.getPopulation();
        PopulationFactory popFactory = population.getFactory();

        for (int i = 0; i < visitorCount; i++) {
            Person person = createPerson(popFactory, linkId, type);
            Plan plan = createPlan(scenario, popFactory, person, linkId, rnd, type);

            person.addPlan(plan);
            population.addPerson(person);
            person.getAttributes().putAttribute("subpopulation", type);
            personCounter++;
        }
    }

    private Person createPerson(PopulationFactory popFactory, Id<Link> linkId, String type) {
        return popFactory.createPerson(
                Id.create("visitor_" + personCounter + "_" + linkId + "-" + type, Person.class));
    }

    private Plan createPlan(Scenario scenario, PopulationFactory popFactory,
                            Person person, Id<Link> linkId, Random rnd, String type) {
        Plan plan = popFactory.createPlan();

        // Create and add start activity
        Activity startActivity = popFactory.createActivityFromCoord(HOME_ACTIVITY,
                scenario.getNetwork().getLinks().get(linkId).getFromNode().getCoord());
        startActivity.setEndTime(calculateRandomlyDistributedTime());
        plan.addActivity(startActivity);

        // Add leg
        Leg leg = popFactory.createLeg(CAR_MODE);
        plan.addLeg(leg);

        // Create and add end activity
        Point endPoint = getRandomPointInFeature(rnd, features.get(type));
        if (endPoint == null) {
            log.warn("Could not generate end point for person {}", person.getId());
            return plan;
        }

        Activity endActivity = popFactory.createActivityFromCoord(ACTIVITY_TYPE, MGC.point2Coord(endPoint));
        plan.addActivity(endActivity);

        return plan;
    }

    private static Point getRandomPointInFeature(Random rnd, SimpleFeature feature) {
        if (feature == null) return null;

        Point point;
        double x, y;
        Geometry geometry = (Geometry) feature.getDefaultGeometry();

        do {
            x = feature.getBounds().getMinX() +
                    rnd.nextDouble() * (feature.getBounds().getMaxX() - feature.getBounds().getMinX());
            y = feature.getBounds().getMinY() +
                    rnd.nextDouble() * (feature.getBounds().getMaxY() - feature.getBounds().getMinY());
            point = MGC.xy2Point(x, y);
        } while (!geometry.contains(point));

        return point;
    }

    private double calculateRandomlyDistributedTime() {
        Random rnd = MatsimRandom.getRandom();
        double direction = rnd.nextDouble() <= 0.5 ? -1.0 : 1.0;
        return MEAN_ARRIVAL_TIME + (rnd.nextDouble() * TIME_VARIANCE * direction);
    }
}