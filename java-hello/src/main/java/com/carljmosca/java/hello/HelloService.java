/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carljmosca.java.hello;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author moscac
 */
public class HelloService {

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public void run() {
        displayMessage("hello");
        String pauseSecondsValue = System.getenv("PAUSE_SECONDS");
        int pauseSeconds;
        try {
            pauseSeconds = Integer.parseInt(pauseSecondsValue);
            displayMessage("Pausing for " + pauseSeconds + " seconds...");
            Thread.sleep(pauseSeconds * 1000);
        } catch (NumberFormatException e) {
            displayMessage("Did not find valid value for PAUSE_SECONDS env variable.");
        } catch (InterruptedException e) {
            System.out.print("Interruped.");
        }
        displayMessage("goodbye");
    }

    private void displayMessage(String message) {
        System.out.println(SDF.format(new Date()) + ": " + message);
    }

}
