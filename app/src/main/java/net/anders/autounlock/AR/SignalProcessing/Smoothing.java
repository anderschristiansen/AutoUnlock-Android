package net.anders.autounlock.AR.SignalProcessing;

import net.anders.autounlock.AR.DataSegmentation.Segment;
import net.anders.autounlock.AccelerometerData;

import java.util.List;

/**
 * Created by Anders on 21-02-2017.
 */

public class Smoothing {

    static int counter;
    static List<Segment> smoothSegList;
    static float accTot;

    // 5-point smoothing average finite impulse response (FIR)
    public static void apply5PointSmoothing(AccelerometerData accelerometerData){

        accTot += (accelerometerData.getAccelerationX() + accelerometerData.getAccelerationY() + accelerometerData.getAccelerationZ()) / 3;

        if (counter >= 5) {
            accTot = accTot/5;
            Segment seq = new Segment(accTot);
            smoothSegList.add(seq);

            accTot += (accelerometerData.getAccelerationX() + accelerometerData.getAccelerationY() + accelerometerData.getAccelerationZ()) / 3;
            counter = 0;
            accTot = 0;

    ;
        } else {
            accTot += (accelerometerData.getAccelerationX() + accelerometerData.getAccelerationY() + accelerometerData.getAccelerationZ()) / 3;
            counter++;
        }



    }

}
