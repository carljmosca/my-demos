/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carljmosca.openshift.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    final static Logger LOGGER = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        Application instance = new Application();
        try {
            //instance.run();
            instance.runCronJob();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public Application() {
        
    }

    public void runCronJob() {
        CronJobExample cronJobExample = new CronJobExample();
        cronJobExample.run();
    }
    

    public void run()  {
        JobManager jobManager = new JobManager();
        jobManager.create();
    }
}