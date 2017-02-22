package net.anders.autounlock.AR.SignalProcessing;

import net.anders.autounlock.AccelerometerData;

/**
 * Created by Anders on 21-02-2017.
 */

public class Preprocessing {

    public static void Preprocess(AccelerometerData accelerometerData) {

        double x = accelerometerData.getAccelerationX();
        double y = accelerometerData.getAccelerationX();
        double z = accelerometerData.getAccelerationX();
        double time = accelerometerData.getAccelerationX();

        double accTot = Math.sqrt(x*x + y*y + z*z);
    }
}
