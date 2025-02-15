package org.matsim.prepare.population;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.config.ConfigUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

//Creates population for the routing analysis
public class CreateRoutingPopulation {
    private static final String outputFile = "scenarios/routing-scenario-v1.0/input/routing-population-v1.0.xml.gz";

    private static final int NUMBER_OF_COMMUTERS = 500;
    private static final int NUMBER_OF_TRAVELERS = 200;

    private final Random random = new Random(1234);

    public static void main (String [] args) {
        CreateRoutingPopulation creator = new CreateRoutingPopulation();
        creator.run();
    }

    public void run() {
        File directory = new File("scenarios/routing-scenario-v1.0/input");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        //Create empty scenario and get population factory
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Population population  = scenario.getPopulation();
        PopulationFactory factory = population.getFactory();

        //create different types of routing agents
        //createCommuters(population, factory, 500); //Regular commuters
        //createRandomTravelers(population, factory, 200); //Random trips

        createCommuters(population, factory);
        createRandomTravelers(population, factory);

        //Write the population file
        new PopulationWriter(population).write(outputFile);
        System.out.println("Created population of " + population.getPersons().size() + "persons");

    }

    private void createCommuters(Population population, PopulationFactory factory) {
        for (int i = 0; i < NUMBER_OF_COMMUTERS; i++) {
            Person person = factory.createPerson(Id.createPersonId("Commuter_"+ i));
            Plan plan = factory.createPlan();
            //Morning activity (home)
            Activity home = factory.createActivityFromCoord("home",
                    generateHomeLocation());
            home.setEndTime(7.5 * 3600 + (random.nextDouble()-0.5) * 3600);
            plan.addActivity(home);

            //Travel to work
            plan.addLeg(factory.createLeg(TransportMode.car));

            //Work Activity
            Activity work = factory.createActivityFromCoord("work",
                    generateWorkLocation());
            work.setEndTime(17.5 * 3600 + (random.nextDouble()- 0.5) * 3600);
            plan.addActivity(work);

            //Travel back home
            plan.addLeg(factory.createLeg(TransportMode.car));

            //Evening  at home
            Activity homeEvening = factory.createActivityFromCoord("home",
                    generateHomeLocation());
            plan.addActivity(homeEvening);

            person.addPlan(plan);
            population.addPerson(person);
        }
    }

    private void createRandomTravelers(Population population, PopulationFactory factory) {
        for (int i = 0; i<NUMBER_OF_TRAVELERS; i++) {
            Person person = factory.createPerson(Id.createPersonId("random_" + i));
            Plan plan = factory.createPlan();

            //Start Location
            Activity start = factory.createActivityFromCoord("origin",
                    generateRandomLocation());
            start.setEndTime(9 * 3600 + random.nextDouble()* 8 * 3600);
            plan.addActivity(start);

            //Travel to destination
            plan.addLeg(factory.createLeg(TransportMode.car));

            // End Location
            Activity end = factory.createActivityFromCoord("destination",
                    generateRandomLocation());
            plan.addActivity(end);
            population.addPerson(person);
        }

    }



    private Coord generateWorkLocation() {
        //Generate coordinates with commercial/industrial areas
        double x = 105000 + random.nextDouble() * 5000;
        double y = 205000 + random.nextDouble() * 5000;
        return new Coord(x,y);

    }


    private Coord generateHomeLocation() {
        // Generate coordinates within residential areas of your network
        double x = 100000 + random.nextDouble() * 10000;
        double y = 200000 + random.nextDouble() * 10000;
        return new Coord(x,y);
    }


    private Coord generateRandomLocation () {
        double x = 100000 + random.nextDouble() * 15000;
        double y = 200000 + random.nextDouble() * 15000;
        return new Coord(x,y);
    }

    //private final Random random = new Random(1234);



}
