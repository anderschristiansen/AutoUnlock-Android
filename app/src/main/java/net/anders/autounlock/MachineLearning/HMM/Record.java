package net.anders.autounlock.MachineLearning.HMM;

import net.anders.autounlock.MachineLearning.SessionData;
import net.anders.autounlock.MachineLearning.WindowData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;

/**
 * Created by Anders on 06-03-2017.
 */

public class Record {

    // Lists of lists of values from multiple iterations (observations), used to create HMMs
    List<List<ObservationInteger>> toHmmOri;
    List<List<ObservationInteger>> toHmmVelo;

    // Other variables
    public boolean firstElement = true, answer = true;
    public String fileName, input;
    public FileWriter writerOri, writerVelo, gWriter, iWriter;
    public OpdfIntegerWriter opdfWriterOri, opdfWriterVelo;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    //HMM
    public OpdfIntegerFactory factory;
    public Hmm<ObservationInteger> hmmOri;
    public Hmm<ObservationInteger> hmmVelo;
    public KMeansLearner<ObservationInteger> kml;

    // TODO ABC
    boolean unlockDoor;
    ArrayList<SessionData> clusters;
    public int noOfSessions;

    File outputDirectory = new File("/sdcard/AutoUnlock/HMM/");

    public void record(ArrayList<SessionData> clusters, boolean unlockDoor) {
        this.unlockDoor = unlockDoor;
        this.clusters = clusters;

        try {
            outputDirectory.mkdirs();

            File sdCardFile = new File(outputDirectory, "Overview.txt");
            // Save the cluster name
            gWriter = new FileWriter(sdCardFile, true);

            countSessions();
            if (unlockDoor) { fileName = "unlock-cluster-" + noOfSessions; }
            else { fileName = "lock-cluster-" + noOfSessions; }

            // Writes the gesture name to a reference file
            gWriter.write(fileName + "\r\n");
            gWriter.close();

            learn(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Counts the the number of saved gestures to give a correct length to the array
    public int countSessions() throws IOException {
        File inputDirectory = new File("/sdcard/AutoUnlock/HMM/");
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(inputDirectory, "Overview.txt")));
        lnr.skip(Long.MAX_VALUE);
        noOfSessions = lnr.getLineNumber() + 1;
        lnr.close();

        return noOfSessions;
    }

    // Generate and save HMMs to text files
    public void learn(String fileName) throws IOException, InterruptedException {
        toHmmOri = new LinkedList<List<ObservationInteger>>();
        toHmmVelo = new LinkedList<List<ObservationInteger>>();

        for (SessionData session: clusters) {
            createData(session.getWindows());
        }

        hmmOri = createHmm(toHmmOri);
        writerOri = new FileWriter(new File(outputDirectory, fileName + "_ori.txt"), true);
        opdfWriterOri = new OpdfIntegerWriter();
        HmmWriter.write(writerOri, opdfWriterOri, hmmOri);
        writerOri.close();

        hmmVelo = createHmm(toHmmVelo);
        writerVelo = new FileWriter(new File(outputDirectory, fileName + "_velo.txt"), true);
        opdfWriterVelo = new OpdfIntegerWriter();
        HmmWriter.write(writerVelo, opdfWriterVelo, hmmVelo);
        writerVelo.close();
    }


    public void createData(List<WindowData> windows){
        List<ObservationInteger> ori = new LinkedList<ObservationInteger>();
        List<ObservationInteger> velo = new LinkedList<ObservationInteger>();

        for (WindowData window : windows) {
            ori.add(new ObservationInteger((int)window.getOrientation()));
            velo.add(new ObservationInteger((int)window.getVelocity()));

        }
        toHmmOri.add(ori);
        toHmmVelo.add(velo);
    }


    public Hmm<ObservationInteger> createHmm(List<List<ObservationInteger>> seq){
        // The factory object initialise the observation distributions of each state to a discrete distribution.
        // The argument ('2') of the OpdfIntegerFactory object constructor means that the observations can only have two values ('0' and '1').
        factory = new OpdfIntegerFactory(360);
        kml = new KMeansLearner<ObservationInteger>(8, factory, seq);
        Hmm<ObservationInteger>hmm = kml.iterate();

        // Now we can build a BaumWelchLearner object that can find an HMM fitted to the observation sequences we've just generated
        // The Baum-Welch algorithm only finds the local minimum of its optimum function, so an initial approximation of the result is needed
        // Local maximum likelihood can be derived efficiently using the Baum–Welch algorithm
        BaumWelchLearner bwl = new BaumWelchLearner();
        bwl.setNbIterations(20);
        bwl.learn(hmm, seq);
        return hmm;
    }
}
