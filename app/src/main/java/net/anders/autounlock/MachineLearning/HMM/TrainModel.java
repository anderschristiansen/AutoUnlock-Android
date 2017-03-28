package net.anders.autounlock.MachineLearning.HMM;

import android.util.Log;

import net.anders.autounlock.CoreService;
import net.anders.autounlock.MachineLearning.UnlockData;
import net.anders.autounlock.MachineLearning.WindowData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.ObservationReal;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.OpdfGaussianMixture;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchScaledLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;

/**
 * Created by Anders on 06-03-2017.
 */

public class TrainModel {

    private static final String TAG = "TrainModel";

    // Lists of lists of values from multiple iterations (observations), used to create HMMs
//    List<List<ObservationVector>> toHmmOri;
//    List<List<ObservationVector>> toHmmVelo;
    List<List<ObservationVector>> toHmmVec;

    //HMM
//    public Hmm<ObservationVector> hmmOri;
//    public Hmm<ObservationVector> hmmVelo;
    public Hmm<ObservationVector> hmmVec;

    public void train(ArrayList<UnlockData> cluster) {

        try {
            learn(cluster);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Generate and save HMMs to text files
    public void learn(ArrayList<UnlockData> cluster) throws IOException, InterruptedException {

//        toHmmOri = new LinkedList<>();
//        toHmmVelo = new LinkedList<>();
        toHmmVec = new LinkedList<>();

        for (UnlockData unlock : cluster) {
            createData(unlock.getWindows());
        }

//        hmmOri = createHmm(toHmmOri);
//        hmmVelo = createHmm(toHmmVelo);
        hmmVec = createHmm(toHmmVec);

        CoreService.hmmVecList.add(hmmVec);
//        CoreService.hmmVeloList.add(hmmVelo);
    }

    double roundUp(double num) {
        return (Math.ceil(num / 5d) * 5);
    }

    public void createData(List<WindowData> windows){
//        List<ObservationVector> ori = new LinkedList<>();
//        List<ObservationVector> velo = new LinkedList<>();
        List<ObservationVector> seq = new LinkedList<>();

        for (WindowData window : windows) {
//            Log.i(TAG, "ORI: " + (int)window.getOrientation());
//            Log.i(TAG, "VELO: " + window.getVelocity());

            double newOri = window.getOrientation();
            double newVelo = window.getVelocity();

//            newOri = roundUp(newOri);
//            Log.i(TAG, "ORI: " + newOri);

            seq.add(new ObservationVector(new double[]{newOri, newVelo}));
        }
        toHmmVec.add(seq);
//        toHmmVelo.add(velo);
    }

    /* Training problem - Baum Welch */
    public Hmm<ObservationVector> createHmm(List<List<ObservationVector>> seq){
        // The factory object initialise the observation distributions of each state to a discrete distribution.
        // The argument ('2') of the OpdfIntegerFactory object constructor means that the observations can only have two values ('0' and '1').
//        factory = new OpdfIntegerFactory(360);

        KMeansLearner<ObservationVector> kml = new KMeansLearner<ObservationVector>(5, new OpdfMultiGaussianFactory(2), seq);
        Hmm hmm = kml.iterate();

//        kml = new KMeansLearner(8, new OpdfGaussianFactory(), seq);
//        Hmm<ObservationInteger>hmm = kml.iterate();

        // Now we can build a BaumWelchLearner object that can find an HMM fitted to the observation sequences we've just generated
        // The Baum-Welch algorithm only finds the local minimum of its optimum function, so an initial approximation of the result is needed
        // Local maximum likelihood can be derived efficiently using the Baum–Welch algorithm
        BaumWelchLearner bwl = new BaumWelchLearner();
        bwl.setNbIterations(10);
        hmm = bwl.learn(hmm, seq);
        return hmm;
    }
}
