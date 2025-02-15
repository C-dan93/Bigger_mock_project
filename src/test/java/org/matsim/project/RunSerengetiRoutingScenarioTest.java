package org.matsim.project;

//import for Logging - helps with debugging tests
import org.apache.logging.log4j.LogManager;

//JUnit imports - these are testing framework tools
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

//MATSim imports needed for the test
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.run.RunNetworkRoutingScenario;
import org.matsim.testcases.MatsimTestUtils;

public class RunSerengetiRoutingScenarioTest {
    //Register MATSim's test utilities
    //This helps with test directories and clean up
        @RegisterExtension
        public MatsimTestUtils utils = new MatsimTestUtils();

    //This is the actual test method
        @Test
        public final void test() {
            try {
                String[] args = {
                        "/scenario/routing-scenario-v1.0/input/routing-config-v1.0.xml",
                        "__config:controler.outputDirectory", utils.getOutputDirectory(),
                        "__config:controler.lastIteration", "0"
                };

                //Run the 3 steps as in the main class:
                //1. Preapare the configuration
                Config config = RunNetworkRoutingScenario.prepareConfig(args);
                //2.Create the scenario
                Scenario scenario = RunNetworkRoutingScenario.prepareScenario(config);
                // Set up and run the controller
                Controler controler = RunNetworkRoutingScenario.prepareControler(scenario);
                controler.run();

            } catch (Exception ee) {

                LogManager.getLogger(this.getClass()).fatal("There was an exception: \n" + ee);
                fail();
            }
        }
}
