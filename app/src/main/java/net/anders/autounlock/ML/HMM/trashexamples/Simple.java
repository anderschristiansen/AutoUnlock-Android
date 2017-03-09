package net.anders.autounlock.ML.HMM.trashexamples;

import java.io.FileNotFoundException;
import java.io.IOException;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussian;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;

/**
 * Created by Anders on 20-02-2017.
 */

public class Simple {

    public static void test() {

        OpdfMultiGaussianFactory factory = new OpdfMultiGaussianFactory(2);
        //factory.factor();
        Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(2, factory);

        hmm.setPi(0, 0.50);
        hmm.setPi(1, 0.50);

        double[] mean = {4, 4};
        double[][] covariance = {{2, 2}, {1., 1}};
        OpdfMultiGaussian omg = new OpdfMultiGaussian(mean, covariance);

        // same as upper OpdfMultiGaussian, but in one line and with other values
        OpdfMultiGaussian omg2 = new OpdfMultiGaussian(new double[]{6, 6} /*mean*/, new double[][]{{4, 4}, {3., 3}});

        // set Observation Probability distribution function
        hmm.setOpdf(0, omg);
        hmm.setOpdf(1, omg2);

        hmm.setAij(0, 1, 0.2);
        hmm.setAij(0, 0, 0.8);
        hmm.setAij(1, 0, 0.2);
        hmm.setAij(1, 1, 0.8);



        try {
            //(new GenericHmmDrawerDot()).write(hmm, "hmmtest.dot");
            (new GenericHmmDrawerDot()).write(hmm, "learntHmm.dot");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //line = e.toString();
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //line = e.toString();
            e.printStackTrace();
        }
    }
}