package net.anders.autounlock.AR;

import android.os.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;

/**
 * Created by Anders on 06-03-2017.
 */

public class Record {

    // Lists of lists of values from multiple iterations (observations), used to create HMMs
    List<List<ObservationInteger>> toHmmX;
    List<List<ObservationInteger>> toHmmY;
    List<List<ObservationInteger>> toHmmZ;

    // Lists of values from one full iteration/gesture training performance
    List<ObservationInteger> sequencesX;
    List<ObservationInteger> sequencesY;
    List<ObservationInteger> sequencesZ;

    // Current received value from sensor to add to list
    public int receivedX = 0;
    public int receivedY = 0;
    public int receivedZ = 0;

    // Other variables
    public boolean firstElement = true, answer = true;
    public String gestureName, input, instrumentName;
    public FileWriter writerX, writerY, writerZ, gWriter, iWriter;
    public OpdfIntegerWriter opdfWriterX, opdfWriterY, opdfWriterZ;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    //HMM
    public OpdfIntegerFactory factory;
    public Hmm<ObservationInteger> hmmX, hmmY, hmmZ;
    public KMeansLearner<ObservationInteger> kml;

    File outputDirectory = new File("/sdcard/AutoUnlock/HMM/");

    public Record() throws InterruptedException, IOException, FileFormatException {
        outputDirectory.mkdirs();
        getGesture();
        Recognise r = new Recognise();
        r.Rec();
    }

    public void getGesture() throws IOException, InterruptedException{

        System.out.println("Welcome!");
        System.out.println("You may record up to 5 different gestures.");
        System.out.print("Please state the name of the gesture you wish to create: ");

        gestureName = "test2";
        System.out.println("");

        File sdCardFile = new File(outputDirectory, "GestureList.txt");
        gWriter = new FileWriter(sdCardFile, true);		// Save the gesture name
        gWriter.write(gestureName + "\r\n"); 				// Writes the gesture name to a reference file
        gWriter.close();

//		System.out.println("Choose an instrument from the following");
//		System.out.println("1 - Guitar");
//		System.out.println("2 - Piano");
//		System.out.println("3 - Bass");
//		System.out.println("4 - Synth");
//		System.out.println("5 - Organ");
//		System.out.print("Instrument: ");

        //getInstrument();

        learnGesture(gestureName);
        System.out.println("");
    }

    //Repeat gesture to generate observation sequences and then generate and save HMMs to text files
    public void learnGesture(String gestureName) throws IOException, InterruptedException {

        toHmmX = new LinkedList<List<ObservationInteger>>();
        toHmmY = new LinkedList<List<ObservationInteger>>();
        toHmmZ = new LinkedList<List<ObservationInteger>>();

        for(int i=1;i<11;i++){

            sequencesX = new LinkedList<ObservationInteger>();
            sequencesY = new LinkedList<ObservationInteger>();
            sequencesZ = new LinkedList<ObservationInteger>();

//			System.out.println("");
//			System.out.println("Training iteration " +i);
//			System.out.println("Press Enter, and perform gesture '"+gestureName+"'");
//			System.in.read();

            runContinuous();

            toHmmX = createData(toHmmX, sequencesX);
            toHmmY = createData(toHmmY, sequencesY);
            toHmmZ = createData(toHmmZ, sequencesZ);

//			System.in.read();
        }

        hmmX = createHmm(toHmmX);
        writerX = new FileWriter(new File(outputDirectory, gestureName + "X.txt"), true);
        opdfWriterX = new OpdfIntegerWriter();
        HmmWriter.write(writerX, opdfWriterX, hmmX);
        writerX.close();

        hmmY = createHmm(toHmmY);
        writerY = new FileWriter(new File(outputDirectory, gestureName + "Y.txt"), true);
        opdfWriterY = new OpdfIntegerWriter();
        HmmWriter.write(writerY, opdfWriterY, hmmY);
        writerY.close();

        hmmZ = createHmm(toHmmZ);
        writerZ = new FileWriter(new File(outputDirectory, gestureName + "Z.txt"), true);
        opdfWriterZ = new OpdfIntegerWriter();
        HmmWriter.write(writerZ, opdfWriterZ, hmmZ);
        writerZ.close();
    }

    //Tells device to continually send data and retrieves it, generating a sequence
    public void runContinuous() throws InterruptedException {

        System.out.println("Capturing...");

        for (int i=0;i<10;i++){

            if (firstElement == true){
                firstElement = false;
            }
            else {

                ObservationInteger x = new ObservationInteger(2);
                sequencesX.add(x);

                ObservationInteger y = new ObservationInteger(2);
                sequencesY.add(y);

                ObservationInteger z = new ObservationInteger(3);
                sequencesZ.add(z);

//				Thread.sleep(50);
            }
        }

        firstElement = true;
//		System.out.println("");
        System.out.println("Stopped capturing");
//		System.out.println("");
//		System.out.println("Press Enter to continue");
    }


    public List<List<ObservationInteger>> createData (List<List<ObservationInteger>> toHmm, List<ObservationInteger> sequences){

        toHmm.add(sequences);
        return toHmm;
    }


    public Hmm<ObservationInteger> createHmm(List<List<ObservationInteger>> toHmm){

        factory = new OpdfIntegerFactory(10);
        kml = new KMeansLearner<ObservationInteger>(2,factory,toHmm);
        Hmm<ObservationInteger>hmm = kml.iterate();

        BaumWelchLearner bwl = new BaumWelchLearner();
        bwl.setNbIterations(20);
        bwl.learn(hmm, toHmm);
        return hmm;
    }
}
