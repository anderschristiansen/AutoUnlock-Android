package net.anders.autounlock.ML.HMM;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
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

public class Training {

    // Lists of lists of values from multiple iterations (observations), used to create HMMs
    List<List<ObservationReal>> toHmmOri;
    List<List<ObservationReal>> toHmmVelo;

    // Lists of values from one full iteration/gesture training performance
    List<ObservationReal> sequencesOri;
    List<ObservationReal> sequencesVelo;

    // Current received value from sensor to add to list
    public int receivedOri = 0;
    public int receivedVelo = 0;

    // Other variables
    public boolean firstElement = true, answer = true;
    public String gestureName, input;
    public FileWriter writerOri, writerVelo, gWriter, iWriter;
    public OpdfGaussianWriter opdfWriterOri, opdfWriterVelo;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    //HMM
    public OpdfGaussianFactory factory;
    public Hmm<ObservationReal> hmmOri;
    public Hmm<ObservationReal> hmmVelo;
    public KMeansLearner<ObservationReal> kml;

    File outputDirectory = new File("/sdcard/AutoUnlock/HMM/");

    public Training(List<ObservationReal> seqOri, List<ObservationReal> seqVelo) throws InterruptedException, IOException, FileFormatException {
        sequencesOri = seqOri;
        sequencesVelo = seqVelo;
        outputDirectory.mkdirs();
        getGesture();

        //Classification r = new Classification(); r.Rec();
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
        toHmmOri = new LinkedList<List<ObservationReal>>();
        toHmmVelo = new LinkedList<List<ObservationReal>>();

        for(int i=1;i<10;i++){
//            sequencesX = new LinkedList<ObservationInteger>();
//            runContinuous();
            toHmmOri = createData(toHmmOri, sequencesOri);
            toHmmVelo = createData(toHmmVelo, sequencesVelo);
        }

        hmmOri = createHmm(toHmmOri);
        writerOri = new FileWriter(new File(outputDirectory, gestureName + "Ori.txt"), true);
        opdfWriterOri = new OpdfGaussianWriter();
        HmmWriter.write(writerOri, opdfWriterOri, hmmOri);
        writerOri.close();

        hmmVelo = createHmm(toHmmVelo);
        writerVelo = new FileWriter(new File(outputDirectory, gestureName + "Velo.txt"), true);
        opdfWriterVelo = new OpdfGaussianWriter();
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
                ObservationReal ori = new ObservationReal(2);
                sequencesOri.add(ori);

                ObservationReal velo = new ObservationReal(2);
                sequencesVelo.add(velo);
            }
        }
        firstElement = true;
        System.out.println("Stopped capturing");
    }

    public List<List<ObservationReal>> createData (List<List<ObservationReal>> toHmm, List<ObservationReal> sequences){
        toHmm.add(sequences);
        return toHmm;
    }


    public Hmm<ObservationReal> createHmm(List<List<ObservationReal>> seq){
        // The factory object initialise the observation distributions of each state to a discrete distribution.
        // The argument ('2') of the OpdfIntegerFactory object constructor means that the observations can only have two values ('0' and '1').
        factory = new OpdfGaussianFactory();
        kml = new KMeansLearner<ObservationReal>(3, factory, seq);
        Hmm<ObservationReal>hmm = kml.iterate();

        // Now we can build a BaumWelchLearner object that can find an HMM fitted to the observation sequences we've just generated
        // The Baum-Welch algorithm only finds the local minimum of its optimum function, so an initial approximation of the result is needed
        // Local maximum likelihood can be derived efficiently using the Baum–Welch algorithm
        BaumWelchLearner bwl = new BaumWelchLearner();
        bwl.setNbIterations(10);
        bwl.learn(hmm, seq);
        return hmm;
    }
}