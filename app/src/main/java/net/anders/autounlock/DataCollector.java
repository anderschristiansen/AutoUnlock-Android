package net.anders.autounlock;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

/**
 * Created by Anders on 13-02-2017.
 */

public class DataCollector {

    public static void insertAccelerometer (float x, float y, float z, long time) {
        //CoreService.dataStore.insertAccelerometer(x, y, z, time);



        Hmm <ObservationInteger> hmm = new Hmm <ObservationInteger>(2, new OpdfIntegerFactory(2));

    }

}
