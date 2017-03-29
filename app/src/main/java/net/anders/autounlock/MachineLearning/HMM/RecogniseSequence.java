package net.anders.autounlock.MachineLearning.HMM;

import android.content.Context;

import net.anders.autounlock.CoreService;
import net.anders.autounlock.MachineLearning.PatternRecognitionService;
import net.anders.autounlock.MachineLearning.WindowData;
import net.anders.autounlock.NotificationUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.ObservationReal;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerReader;

/**
 * Created by Anders on 06-03-2017.
 */

public class RecogniseSequence {

    private static String TAG = "RecogniseSequence";

    //Observation list from one gesture performance to be compared against a saved HMM
    public List<ObservationVector> sequencesInstanceVec = new ArrayList<>();
//    public List<ObservationVector> sequencesInstanceVelo = new ArrayList<>();

    public double probabilityVec = 0;
    //    public double probabilityVelo = 0;
    public double bestMatchProb = 0.00000000000000000000000001;
    public int bestMatchNo = -1;
    public int currentMatchNo = -1;

    public boolean recognise(Context context, WindowData[] unlock) {
        createData(unlock);
        evaluate(sequencesInstanceVec);

        if (bestMatchNo < 0){
            System.out.println("*Unable to recognise gesture!*");
            System.out.println("Try again.");
        }
        else {
            System.out.println("Best match is HMM: " + bestMatchNo +  " with probabilty " + bestMatchProb);

            NotificationUtility notification = new NotificationUtility();
            notification.displayUnlockNotification(context, bestMatchNo+1);

            bestMatchNo = -1;
            bestMatchProb = 0.00000000000000000000000001;
            return true;
        }
        return false;
    }

    public void createData(WindowData[] windows){
        for (WindowData window : windows) {
            sequencesInstanceVec.add(new ObservationVector(new double[]{window.getOrientation(), window.getVelocity()}));
        }
    }

    public void evaluate(List<ObservationVector> vec){

        for (int i = 0; CoreService.hmmVecList.size() > i; i++) {
            probabilityVec = getProbability("HMM: " + i + ": vec", vec, CoreService.hmmVecList.get(i), probabilityVec);
            currentMatchNo = i;

            if (true) {
//            if (probabilityVec !=0 && probabilityVec > bestMatchProb) {
                bestMatchProb = probabilityVec;
                bestMatchNo = currentMatchNo;
            }
        }
    }

    /* Evaluation problem - Forward-Backward Calculator */
    public double getProbability(String label, List<ObservationVector> sequence, Hmm<ObservationVector> hmm, double probability){

//        ForwardBackwardCalculator fbc = new ForwardBackwardCalculator(sequence, hmm);
//        probability = fbc.probability();
//        System.out.println("FBC: - " + label + ": "+ probability);

        ViterbiCalculator vit = new ViterbiCalculator(sequence, hmm);
        probability = vit.lnProbability();
        System.out.println("FBC: - " + label + ": "+ probability);

        return probability;
    }
}

