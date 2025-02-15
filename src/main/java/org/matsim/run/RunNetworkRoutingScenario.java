package org.matsim.run;

//import tools necessary for Logging-helps track what is happening in your program
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//Core MATSim imports - these provide the main simulation components
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.scenario.ScenarioUtils;

//Utility import for working with arrays
import java.io.IOException;
import java.util.Arrays;

//import static org.matsim.run.RunSerengetiParkScenario.*;

public final class RunNetworkRoutingScenario {
    // Set up logging to help debug and monitor the program
    private static final Logger log = LogManager.getLogger(RunNetworkRoutingScenario.class);

    // Define where to find the configuration file
    private static final String DEFAULT_CONFIG_PATH = "./scenarios/serengeti-park-v1.0/input/serengeti-park-config-v1.0.xml";

    //main method - this where the program starts
    public static void main(String[] args) throws IOException {
        // if no arguments provided, use the default config path
        args = args.length == 0 ? new String[]{DEFAULT_CONFIG_PATH} : args;
        //Log the arguments for debugging purposes
        Arrays.stream(args).forEach(log::info);
        //The three main steps to run a MATSim simulation


        Config config = prepareConfig(args); //1. Set up configuration
        Scenario scenario = prepareScenario(config); // 2. Create scenario
        config.controller().setOutputDirectory("./output/routing-scenario/");
        Controler controler = prepareControler(scenario);
        controler.run();
    }


    // Prepare the configuration for the simulation
    public static Config prepareConfig(String[] args) {
        //set up logging for configuration process
        OutputDirectoryLogging.catchLogEntries();

        //load configuration file
        Config config = ConfigUtils.loadConfig(args[0]);

        //set routing specific settings
        config.routing().setRoutingRandomness(0.); //No randomness in route choice
        config.routing().setAccessEgressType(RoutingConfigGroup.AccessEgressType.accessEgressModeToLink);
        //Apply any command line arguments to the config
        ConfigUtils.applyCommandline(config, Arrays.copyOfRange(args, 1, args.length));
        return config;
    }
    public static Scenario prepareScenario(Config config) {
        return ScenarioUtils.loadScenario(config);
    }
    public static Controler perepareControler(Scenario scenario) {
        return new Controler(scenario);
    }

    public static Controler prepareControler(Scenario scenario) {
        return new Controler(scenario);
    }
}
