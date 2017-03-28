package net.anders.autounlock.MachineLearning.HMM;

import net.anders.autounlock.MachineLearning.SessionData;
import net.anders.autounlock.MachineLearning.WindowData;

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
import java.util.ArrayList;
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

public class Recognise {

    private static String TAG = "Recognise";

    //Observation list from one gesture performance to be compared against a saved HMM
    public List<ObservationInteger> sequencesInstanceOri = new ArrayList<>();
    public List<ObservationInteger> sequencesInstanceVelo = new ArrayList<>();

    // Variables
    public boolean firstElement = true;
    public int noOfSessions;
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

    public void recognise(WindowData[] session) {
        try {
            countSessions();

            String[] sessionName = new String[noOfSessions];

            FileInputStream in = new FileInputStream(new File(inputDirectory, "Overview.txt"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            for(int i = 1; i<sessionName.length;i++){
                sessionName[i] = br.readLine();
            }
            in.close();
            br.close();

            System.out.println("");
            System.out.println("Your saved sessions are:");

            for (int i = 1; i<sessionName.length; i++){
                System.out.println("Session #[" + i + "] = " + sessionName[i]);
            }

            for (int i=1; i<sessionName.length;i++){
                readHmm(i);
            }

            createData(session);

            for (int i = 1; i <sessionName.length;i++){
                evaluate(sequencesInstanceOri, sequencesInstanceVelo, i);
            }

            if (bestMatchNo <= 0){
                System.out.println("*Unable to recognise gesture!*");
                System.out.println("Try again.");
            }
            else if (bestMatchNo > 0){
                System.out.println("Best match is: "+ sessionName[bestMatchNo] + " with probabilty " + bestMatchProb);
                bestMatchNo = -1;
                bestMatchProb = 0.00000000000000000000000001;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileFormatException e) {
            e.printStackTrace();
        }
    }

    public void createData(WindowData[] windows){
        for (WindowData window : windows) {
            sequencesInstanceOri.add(new ObservationInteger((int)window.getOrientation()));
            sequencesInstanceVelo.add(new ObservationInteger((int)window.getVelocity()));
        }
    }


    //Counts the the number of saved gestures to give a correct length to the array
    public int countSessions() throws IOException {

        inputDirectory = new File("/sdcard/AutoUnlock/HMM/");
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(inputDirectory, "Overview.txt")));
        lnr.skip(Long.MAX_VALUE);
        noOfSessions = lnr.getLineNumber() + 1;
        lnr.close();

        return noOfSessions;
    }


    //Reads each HMM from the saved text files
    public void readHmm(int hmmNo) throws FileFormatException, FileNotFoundException, IOException{

        String[] fileName = new String[noOfSessions];
        FileInputStream in = new FileInputStream(new File(inputDirectory, "Overview.txt"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        for(int i = 1; i<fileName.length;i++ ){
            fileName[i] = br.readLine();
        }
        in.close();
        br.close();


        // Husk at nævn i rapport at dette var nødvendigt for at få en korrekt syntaks
        changeDecimal(fileName, hmmNo, "_ori.txt");
        changeDecimal(fileName, hmmNo, "_velo.txt");

        fileReaderOri = new FileReader(new File(inputDirectory, fileName[hmmNo] + "_ori.txt"));
        fileReaderVelo = new FileReader(new File(inputDirectory, fileName[hmmNo] + "_velo.txt"));
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

    private void changeDecimal(String[] fileName, int hmmNo, String type) {
        BufferedReader b = null;
        try {
            String fpath = inputDirectory + "/" + fileName[hmmNo] + type;
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

            File file = new File(inputDirectory, fileName[hmmNo] + type);
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

        System.out.println("HMM " +hmmNo + " = ORI: " + probabilityOri +" VELO: " + probabilityVelo);

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

