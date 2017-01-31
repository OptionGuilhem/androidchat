package com.example.guilhem.tuto;


/**
 * Created by Guilhem on 12/11/2015.
 */

public class AppTimer {

    private static int count = 0;
    public static boolean show = false;

    public static void activityStarted() {
        if (count == 0) {
            show = false;
        }
        count++;
    }
    public static void activityStopped() {
        count--;
        if (count == 0) {
            show = true;
        }
    }
}
