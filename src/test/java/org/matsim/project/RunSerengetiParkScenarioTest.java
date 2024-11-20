/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.project;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.run.RunSerengetiParkScenario;
import org.matsim.testcases.MatsimTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author ikaddoura
 *
 */
public class RunSerengetiParkScenarioTest {

    @RegisterExtension
    public MatsimTestUtils utils = new MatsimTestUtils() ;

    @Test
    public final void test() {

        try {
            String [] args = {"./scenarios/serengeti-park-v1.0/input/serengeti-park-config-v1.0.xml",
                    "--config:controler.outputDirectory", utils.getOutputDirectory(),
                    "--config:controler.lastIteration", "0"
            } ;

            Config config = RunSerengetiParkScenario.prepareConfig( args ) ;
            Scenario scenario = RunSerengetiParkScenario.prepareScenario( config ) ;
            Controler controler = RunSerengetiParkScenario.prepareControler( scenario ) ;
            controler.run();

        } catch ( Exception ee ) {
            LogManager.getLogger(this.getClass()).fatal("there was an exception: \n" + ee ) ;

            // if one catches an exception, then one needs to explicitly fail the test:
            fail();
        }


    }
}
