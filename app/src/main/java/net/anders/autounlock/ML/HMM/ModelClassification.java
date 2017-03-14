package net.anders.autounlock.ML.HMM;

import android.util.Log;

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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerReader;

/**
 * Created by Anders on 06-03-2017.
 */

public class ModelClassification {

    private static String TAG = "ModelClassification";

    private final static byte MSG_RUN_CONTINUOUS = 104;
    private final static int PORT_NUMBER = 5000;
    private InetAddress address;
    private DatagramSocket socket;
    private	final static String IP_ADDRESS = "10.74.192.9";

    //Observation list from one gesture performance to be compared against a saved HMM
    public List<ObservationInteger> sequencesInstanceOri;
    public List<ObservationInteger> sequencesInstanceVelo;

    // Current received value from sensor to add to list
    public int receivedOri = 0;
    public int receivedVelo = 0;

    // Variables
    public boolean firstElement = true;
    public int noOfGestures;
    public FileReader fileReaderOri, fileReaderVelo;
    public FileWriter testWriterOri, testWriterVelo;
    public OpdfIntegerReader opdfReader;

    //HMM
    public OpdfIntegerFactory factory;
    public Hmm<ObservationInteger> hmmOri, hmmVelo;
    public ForwardBackwardCalculator fbc;
    public Hmm<ObservationInteger> hmm1Ori, hmm1Velo;
    public Hmm<ObservationInteger> hmm2Ori, hmm2Velo;
    public Hmm<ObservationInteger> hmm3Ori, hmm3Velo;
    public Hmm<ObservationInteger> hmm4Ori, hmm4Velo;
    public Hmm<ObservationInteger> hmm5Ori, hmm5Velo;


    public double probabilityOri = 0;
    public double probabilityVelo = 0;
    public double bestMatchProb = 0.00000000000000000000000001;
    public int bestMatchNo = -1;

    File inputDirectory = new File("/sdcard/AutoUnlock/HMM/");

    public ModelClassification(List<ObservationInteger> seqOri, List<ObservationInteger> seqVelo) throws FileFormatException, IOException, InterruptedException{
        sequencesInstanceOri = seqOri;
        sequencesInstanceVelo = seqVelo;
        Recognise();
    }

    public void Recognise() throws IOException, FileFormatException, InterruptedException{

        countGestures();
        String[] gestureName = new String[noOfGestures];

        FileInputStream in = new FileInputStream(new File(inputDirectory, "GestureList.txt"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        for(int i = 1; i<gestureName.length;i++){
            gestureName[i] = br.readLine();
        }
        in.close();
        br.close();

        System.out.println("");
        System.out.println("Your saved gestures are:");

        for (int i = 1; i<gestureName.length; i++){
            System.out.println("Gesture #[" + i + "] = " + gestureName[i]);
        }

        for (int i=1; i<gestureName.length;i++){
            readHmm(i);
        }

//        for (int j = 1; j<10;j++){
            //sequencesInstanceX = new LinkedList<ObservationInteger>();
//            runContinuous(sequencesInstanceX);

            for (int i = 1; i <gestureName.length;i++){
                evaluate(sequencesInstanceOri, sequencesInstanceVelo, i);
            }

            if (bestMatchNo <= 0){
                System.out.println("*Unable to recognise gesture!*");
                System.out.println("Try again.");
            }
            else if (bestMatchNo > 0){
                System.out.println("Best match is: "+ gestureName[bestMatchNo]);
                    bestMatchNo = -1;
                    bestMatchProb = 0.00000000000000000000000001;
            }
//        }

//        System.out.println("");
//        System.out.println("Restart RECOGNISE program to continue recognising gestures!");
//        System.exit(0);
    }


    //Counts the the number of saved gestures to give a correct length to the array
    public int countGestures() throws IOException{

        inputDirectory = new File("/sdcard/AutoUnlock/HMM/");
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(inputDirectory, "GestureList.txt")));
        lnr.skip(Long.MAX_VALUE);
        noOfGestures = lnr.getLineNumber() + 1;
        lnr.close();

        return noOfGestures;
    }


    //Reads each HMM from the saved text files
    public void readHmm(int hmmNo)throws FileFormatException, FileNotFoundException, IOException{

        String[] gestureName = new String[noOfGestures];
        FileInputStream in = new FileInputStream(new File(inputDirectory, "GestureList.txt"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        for(int i = 1; i<gestureName.length;i++ ){
            gestureName[i] = br.readLine();
        }
        in.close();
        br.close();


        // Husk at nævn i rapport at dette var nødvendigt for at få en korrekt syntaks
        changeDecimal(gestureName, hmmNo, "Ori");
        changeDecimal(gestureName, hmmNo, "Velo");

        fileReaderOri = new FileReader(new File(inputDirectory, gestureName[hmmNo] + "Ori.txt"));
        fileReaderVelo = new FileReader(new File(inputDirectory, gestureName[hmmNo] + "Velo.txt"));
        opdfReader = new OpdfIntegerReader();

        switch(hmmNo){
            case 1:
                hmm1Ori = HmmReader.read(fileReaderOri, opdfReader);
                hmm1Velo = HmmReader.read(fileReaderVelo, opdfReader);
                break;

            case 2:
                hmm2Ori = HmmReader.read(fileReaderOri, opdfReader);
                hmm2Velo = HmmReader.read(fileReaderVelo, opdfReader);
                break;

            case 3:
                hmm3Ori = HmmReader.read(fileReaderOri, opdfReader);
                hmm3Velo = HmmReader.read(fileReaderVelo, opdfReader);
                break;

            case 4:
                hmm4Ori = HmmReader.read(fileReaderOri, opdfReader);
                hmm4Velo = HmmReader.read(fileReaderVelo, opdfReader);
                break;

            case 5:
                hmm5Ori = HmmReader.read(fileReaderOri, opdfReader);
                hmm5Velo = HmmReader.read(fileReaderVelo, opdfReader);
                break;

            default:
                System.out.println("Error in readhHmm()");
                break;
        }
    }

    private void changeDecimal(String[] gestureName, int hmmNo, String type) {
        BufferedReader b = null;
        try {
            String fpath = inputDirectory + "/" + gestureName[hmmNo] + type + ".txt";
            try {
                b = new BufferedReader(new FileReader(fpath));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            String text = "";
            String line;
            while ((line = b.readLine()) != null) {
                text += line.replace(",", ".") + " ";
            }

            File file = new File(inputDirectory, gestureName[hmmNo] + type + ".txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(text.getBytes());
            } finally {
                stream.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void runContinuous(List<ObservationInteger>sequencesOri, List<ObservationInteger>sequencesVelo) throws InterruptedException{
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


    public void evaluate (List<ObservationInteger> ori, List<ObservationInteger> velo, int hmmNo){
        switch (hmmNo){
            case 1:
                probabilityOri = getProbability(ori, hmm1Ori, probabilityOri);
                probabilityVelo = getProbability(velo, hmm1Velo, probabilityVelo);
                break;

            case 2:
                probabilityOri = getProbability(ori, hmm2Ori, probabilityOri);
                probabilityVelo = getProbability(velo, hmm2Velo, probabilityVelo);
                break;
            case 3:
                probabilityOri = getProbability(ori, hmm3Ori, probabilityOri);
                probabilityVelo = getProbability(velo, hmm3Velo, probabilityVelo);
                break;

            case 4:
                probabilityOri = getProbability(ori, hmm4Ori, probabilityOri);
                probabilityVelo = getProbability(velo, hmm4Velo, probabilityVelo);
                break;

            case 5:
                probabilityOri = getProbability(ori, hmm5Ori, probabilityOri);
                probabilityVelo = getProbability(velo, hmm5Velo, probabilityVelo);
                break;

            default:
                System.out.println("Error in evaluate()");
                break;
        }

        //System.out.println("HMM " +hmmNo + " = X: " + probabilityX +" Y: " + probabilityY  +" Z: "+ probabilityZ);

        if ((probabilityOri + probabilityVelo) !=0 &&
                (probabilityOri + probabilityVelo) > bestMatchProb)
        {
            bestMatchProb = probabilityOri + probabilityVelo;
            bestMatchNo = hmmNo;
        }
    }

    public double getProbability(List<ObservationInteger> sequence, Hmm<ObservationInteger> hmm, double probability){

        fbc = new ForwardBackwardCalculator(sequence, hmm);
        probability = fbc.probability();
        return probability;
    }
}

