package net.anders.autounlock.DataProcessingt;

import java.io.IOException;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;

/**
 * Created by Anders on 15-02-2017.
 */

public class Classification {

    public static void getClassifier() {

        /*
         The letters Opdf stand for observation probability distribution function

         1) There is no such thing as a “State” class, so each state is simply designated by an integer index
         2) A sequence of states is thus implemented as an array of integers

         ----
         2 states: Rask og feber
         Læge kan ikke se de to states = de er hidden
         Patient kan have følgende symtomer = normal, cold, dizzy

         Hidden states: Healthy, Fever
         Observations: Normal, Cold, Dizzy
         Entire system: HMM
         ----

         0 Still
         1 Walk
         2 Run

         3 Lie
         4 Sit
         5 Stand
         */

        Hmm<ObservationInteger> hmm = new Hmm <ObservationInteger>(2, new OpdfIntegerFactory(2));

        hmm.setPi(0, 0.95);
        hmm.setPi(1, 0.05);
        hmm.setOpdf(0, new OpdfInteger(new double[] {0.95 , 0.05}));
        hmm.setOpdf(1, new OpdfInteger(new double[] {0.2 , 0.8}));
        hmm.setAij(0, 1, 0.05);
        hmm.setAij(0, 0, 0.95);
        hmm.setAij(1, 0, 0.1);
        hmm.setAij(1, 1, 0.9);


//        KMeansLearner<ObservationInteger> kml = new KMeansLearner<ObservationInteger>(3, new OpdfIntegerFactory (4), sequences);
//        Hmm<ObservationInteger> initHmm = kml.iterate();

        //KMeansLearner<ObservationReal> kml = new KMeansLearner<ObservationReal>(10, new OpdfGaussianFactory(), ob.observations);

        try {
            //(new GenericHmmDrawerDot()).write(hmm, "C:/@master/autounlock/app/src/main/java/net/anders/autounlock/DataProcessing/hmmtest.dot");
            (new GenericHmmDrawerDot()).write(hmm, "hmmtest.dot");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ForwardBackwardScaledCalculator fbsc = new ForwardBackwardScaledCalculator(test_pair.getValue(),model_pair.getValue().get_hmm());
//        System.out.println(fbsc.lnProbability());


    }

}

