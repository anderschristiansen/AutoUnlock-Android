package net.anders.autounlock.ML.HMM;

import net.anders.autounlock.ML.DataSegmentation.ClusterData;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.ObservationReal;
import be.ac.ulg.montefiore.run.jahmm.OpdfGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfGaussianWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;

/**
 * Created by Anders on 06-03-2017.
 */

public class ModelTraining {

    // Lists of lists of values from multiple iterations (observations), used to create HMMs
    List<List<ObservationInteger>> toHmmOri;
    List<List<ObservationInteger>> toHmmVelo;

    // Lists of values from one full iteration/gesture training performance
    List<ObservationInteger> sequencesOri;
    List<ObservationInteger> sequencesVelo;

    // Current received value from sensor to add to list
    public int receivedOri = 0;
    public int receivedVelo = 0;

    // Other variables
    public boolean firstElement = true, answer = true;
    public String gestureName, input;
    public FileWriter writerOri, writerVelo, gWriter, iWriter;
    public OpdfIntegerWriter opdfWriterOri, opdfWriterVelo;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    //HMM
    public OpdfIntegerFactory factory;
    public Hmm<ObservationInteger> hmmOri;
    public Hmm<ObservationInteger> hmmVelo;
    public KMeansLearner<ObservationInteger> kml;

    File outputDirectory = new File("/sdcard/AutoUnlock/HMM/");

    public void train(List<ObservationInteger> seqOri, List<ObservationInteger> seqVelo) {
//    public void train(ArrayList<ClusterData> clusters) {
        try {
            sequencesOri = seqOri;
            sequencesVelo = seqVelo;
            outputDirectory.mkdirs();
            getGesture();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //ModelClassification r = new ModelClassification(); r.Rec();
    }

    public void getGesture() throws IOException, InterruptedException{
        gestureName = "test2";

        File sdCardFile = new File(outputDirectory, "GestureList.txt");
        // Save the gesture name
        gWriter = new FileWriter(sdCardFile, true);
        // Writes the gesture name to a reference file
        gWriter.write(gestureName + "\r\n");
        gWriter.close();

        learnGesture(gestureName);
    }

    //Repeat gesture to generate observation sequences and then generate and save HMMs to text files
    public void learnGesture(String gestureName) throws IOException, InterruptedException {
        toHmmOri = new LinkedList<List<ObservationInteger>>();
        toHmmVelo = new LinkedList<List<ObservationInteger>>();

        for(int i=1;i<10;i++){
//            sequencesX = new LinkedList<ObservationInteger>();
//            runContinuous();
            toHmmOri = createData(toHmmOri, sequencesOri);
            toHmmVelo = createData(toHmmVelo, sequencesVelo);
        }

        hmmOri = createHmm(toHmmOri);
        writerOri = new FileWriter(new File(outputDirectory, gestureName + "Ori.txt"), true);
        opdfWriterOri = new OpdfIntegerWriter();
        HmmWriter.write(writerOri, opdfWriterOri, hmmOri);
        writerOri.close();

        hmmVelo = createHmm(toHmmVelo);
        writerVelo = new FileWriter(new File(outputDirectory, gestureName + "Velo.txt"), true);
        opdfWriterVelo = new OpdfIntegerWriter();
        HmmWriter.write(writerVelo, opdfWriterVelo, hmmVelo);
        writerVelo.close();
    }

    //Tells device to continually send data and retrieves it, generating a sequence
    public void runContinuous() throws InterruptedException {
        System.out.println("Capturing...");

        for (int i=0;i<10;i++){
            if (firstElement == true){
                firstElement = false;
            }
            else {
                ObservationInteger ori = new ObservationInteger(2);
                sequencesOri.add(ori);

                ObservationInteger velo = new ObservationInteger(2);
                sequencesVelo.add(velo);
            }
        }
        firstElement = true;
        System.out.println("Stopped capturing");
    }

    public List<List<ObservationInteger>> createData (List<List<ObservationInteger>> toHmm, List<ObservationInteger> sequences){
        toHmm.add(sequences);
        return toHmm;
    }


    public Hmm<ObservationInteger> createHmm(List<List<ObservationInteger>> seq){
        // The factory object initialise the observation distributions of each state to a discrete distribution.
        // The argument ('2') of the OpdfIntegerFactory object constructor means that the observations can only have two values ('0' and '1').
        factory = new OpdfIntegerFactory(10);
        kml = new KMeansLearner<ObservationInteger>(3, factory, seq);
        Hmm<ObservationInteger>hmm = kml.iterate();

        // Now we can build a BaumWelchLearner object that can find an HMM fitted to the observation sequences we've just generated
        // The Baum-Welch algorithm only finds the local minimum of its optimum function, so an initial approximation of the result is needed
        // Local maximum likelihood can be derived efficiently using the Baum–Welch algorithm
        BaumWelchLearner bwl = new BaumWelchLearner();
        bwl.setNbIterations(10);
        bwl.learn(hmm, seq);
        return hmm;
    }


}
