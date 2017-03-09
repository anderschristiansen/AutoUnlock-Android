package net.anders.autounlock.AR;

import android.content.Context;

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
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
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

public class Recognise {

    // MotionStar Comms
    private final static byte MSG_RUN_CONTINUOUS = 104;
    private final static int PORT_NUMBER = 5000;
    private InetAddress address;
    private DatagramSocket socket;
    private	final static String IP_ADDRESS = "10.74.192.9";
    //	MotionStar m = new MotionStar();

    //Observation list from one gesture performance to be compared against a saved HMM
    public List<ObservationInteger> sequencesInstanceX, sequencesInstanceY, sequencesInstanceZ;

    // Current received value from sensor to add to list
    public int receivedX = 0;
    public int receivedY = 0;
    public int receivedZ = 0;

    // Variables
    public boolean firstElement = true;
    public int noOfGestures, instrumentNumber;
    public FileReader fileReaderX, fileReaderY, fileReaderZ;
    public FileWriter testWriterX, testWriterY, testWriterZ;
    public OpdfIntegerReader opdfReader;

    //HMM
    public OpdfIntegerFactory factory;
    public Hmm<ObservationInteger> hmmX, hmmY, hmmZ;
    public ForwardBackwardCalculator fbc;
    public Hmm<ObservationInteger> hmm3X, hmm3Y, hmm3Z;
    public Hmm<ObservationInteger> hmm1X, hmm1Y, hmm1Z;
    public Hmm<ObservationInteger> hmm2X, hmm2Y, hmm2Z;
    public Hmm<ObservationInteger> hmm4X, hmm4Y, hmm4Z;
    public Hmm<ObservationInteger> hmm5X, hmm5Y, hmm5Z;


    public double probabilityX = 0;
    public double probabilityY = 0;
    public double probabilityZ = 0;
    public double bestMatchProb = 0.00000000000000000000000001;
    public int bestMatchNo = -1;

    File inputDirectory = new File("/sdcard/AutoUnlock/HMM/");

    public static void start() {

        try {
            Recognise one = new Recognise();
        } catch (FileFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Recognise() throws FileFormatException, IOException, InterruptedException{
        Rec();
    }

    public void Rec() throws IOException, FileFormatException, InterruptedException{

        countGestures();
        String[] gestureName = new String[noOfGestures];
        String[] instrumentName = new String[noOfGestures];

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

//		for(int i = 1; i< instrumentName.length;i++ ){
//			instrumentName[i] = bm.readLine();
//		}

        //im.close();
        //bm.close();

        for (int i=1; i<gestureName.length;i++){
            readHmm(i);
        }

        for (int j = 1; j<11;j++){
            sequencesInstanceX = new LinkedList<ObservationInteger>();
            sequencesInstanceY = new LinkedList<ObservationInteger>();
            sequencesInstanceZ = new LinkedList<ObservationInteger>();

            System.out.println("");
            System.out.println("Recognition iteration " +j);
            System.out.println("Press Enter, and perform a saved gesture");
            System.in.read();

            runContinuous(sequencesInstanceX, sequencesInstanceY, sequencesInstanceZ);

            for (int i = 1; i <gestureName.length;i++){
                evaluate(sequencesInstanceX, sequencesInstanceY, sequencesInstanceZ, i);
            }

            if (bestMatchNo <= 0){
                System.out.println("*Unable to recognise gesture!*");
                System.out.println("Try again.");
            }
            else if (bestMatchNo > 0){
                System.out.println("Best match is: "+ gestureName[bestMatchNo]+ " ("+instrumentName[bestMatchNo]+")");

                if(instrumentName[bestMatchNo].equalsIgnoreCase("guitar")){
                    instrumentNumber = 27;
                    bestMatchNo = -1;
                    bestMatchProb = 0.00000000000000000000000001;
                }
                else if (instrumentName[bestMatchNo].equalsIgnoreCase("piano")){
                    instrumentNumber = 0;
                    bestMatchNo = -1;
                    bestMatchProb = 0.00000000000000000000000001;
                }
                else if (instrumentName[bestMatchNo].equalsIgnoreCase("bass")){
                    instrumentNumber = 33;
                    bestMatchNo = -1;
                    bestMatchProb = 0.00000000000000000000000001;
                }
                else if (instrumentName[bestMatchNo].equalsIgnoreCase("synth")){
                    instrumentNumber = 81;
                    bestMatchNo = -1;
                    bestMatchProb = 0.00000000000000000000000001;
                }
                else if (instrumentName[bestMatchNo].equalsIgnoreCase("organ")){
                    instrumentNumber = 19;
                    bestMatchNo = -1;
                    bestMatchProb = 0.00000000000000000000000001;
                }
                //				midiOutput(instrumentNumber);
            }
        }

        System.out.println("");
//		m.disconnect();
        System.out.println("Restart RECOGNISE program to continue recognising gestures!");
        System.exit(0);
    }


    //Counts the the number of saved gestures to give a correct length to the array
    public int countGestures() throws IOException{

        inputDirectory = new File("/sdcard/AutoUnlock/HMM/");
        //File file = new File("GestureList.txt");
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
        changeDecimal(gestureName, hmmNo, "X");
        changeDecimal(gestureName, hmmNo, "Y");
        changeDecimal(gestureName, hmmNo, "Z");

        fileReaderX = new FileReader(new File(inputDirectory, gestureName[hmmNo] + "X.txt"));
        fileReaderY = new FileReader(new File(inputDirectory, gestureName[hmmNo] + "Y.txt"));
        fileReaderZ = new FileReader(new File(inputDirectory, gestureName[hmmNo] + "Z.txt"));
        opdfReader = new OpdfIntegerReader();


        switch(hmmNo){
            case 1:
                hmm1X = HmmReader.read(fileReaderX, opdfReader);
                hmm1Y = HmmReader.read(fileReaderY, opdfReader);
                hmm1Z = HmmReader.read(fileReaderZ, opdfReader);
                break;

            case 2:
                hmm2X = HmmReader.read(fileReaderX, opdfReader);
                hmm2Y = HmmReader.read(fileReaderY, opdfReader);
                hmm2Z = HmmReader.read(fileReaderZ, opdfReader);
                break;

            case 3:
                hmm3X = HmmReader.read(fileReaderX, opdfReader);
                hmm3Y = HmmReader.read(fileReaderY, opdfReader);
                hmm3Z = HmmReader.read(fileReaderZ, opdfReader);
                break;

            case 4:
                hmm4X = HmmReader.read(fileReaderX, opdfReader);
                hmm4Y = HmmReader.read(fileReaderY, opdfReader);
                hmm4Z = HmmReader.read(fileReaderZ, opdfReader);
                break;

            case 5:
                hmm5X = HmmReader.read(fileReaderX, opdfReader);
                hmm5Y = HmmReader.read(fileReaderY, opdfReader);
                hmm5Z = HmmReader.read(fileReaderZ, opdfReader);
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
            String line = "";
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

    public void runContinuous(List<ObservationInteger>sequencesX, List<ObservationInteger>sequencesY, List<ObservationInteger>sequencesZ) throws InterruptedException{
        System.out.println("Capturing...");
        for (int i=0;i<101;i++){
            if (firstElement == true){
                firstElement = false;
            }
            else {
                ObservationInteger x = new ObservationInteger(1);
                sequencesX.add(x);
                ObservationInteger y = new ObservationInteger(2);
                sequencesY.add(y);
                ObservationInteger z = new ObservationInteger(3);
                sequencesZ.add(z);

                Thread.sleep(50);
            }
        }
        firstElement = true;
        System.out.println("");
        System.out.println("Stopped capturing");
        System.out.println("");
    }


    public void evaluate (List<ObservationInteger> x, List<ObservationInteger> y, List<ObservationInteger> z, int hmmNo){
        switch (hmmNo){
            case 1:
                probabilityX = getProbability(x, hmm1X, probabilityX);
                probabilityY = getProbability(y, hmm1Y, probabilityY);
                probabilityZ = getProbability(z, hmm1Z, probabilityZ);
                break;

            case 2:
                probabilityX = getProbability(x, hmm2X, probabilityX);
                probabilityY = getProbability(y, hmm2Y, probabilityY);
                probabilityZ = getProbability(z, hmm2Z, probabilityZ);
                break;
            case 3:
                probabilityX = getProbability(x, hmm3X, probabilityX);
                probabilityY = getProbability(y, hmm3Y, probabilityY);
                probabilityZ = getProbability(z, hmm3Z, probabilityZ);
                break;

            case 4:
                probabilityX = getProbability(x, hmm4X, probabilityX);
                probabilityY = getProbability(y, hmm4Y, probabilityY);
                probabilityZ = getProbability(z, hmm4Z, probabilityZ);
                break;

            case 5:
                probabilityX = getProbability(x, hmm5X, probabilityX);
                probabilityY = getProbability(y, hmm5Y, probabilityY);
                probabilityZ = getProbability(z, hmm5Z, probabilityZ);
                break;

            default:
                System.out.println("Error in evaluate()");
                break;
        }

        //System.out.println("HMM " +hmmNo + " = X: " + probabilityX +" Y: " + probabilityY  +" Z: "+ probabilityZ);

        if ((probabilityX + probabilityY + probabilityZ) !=0 &&
                (probabilityX + probabilityY + probabilityZ) > bestMatchProb)
        {
            bestMatchProb = probabilityX + probabilityY + probabilityZ;
            bestMatchNo = hmmNo;
        }
    }

    public double getProbability(List<ObservationInteger> sequence, Hmm<ObservationInteger> hmm, double probability){

        fbc = new ForwardBackwardCalculator(sequence, hmm);
        probability = fbc.probability();
        return probability;
    }
}

