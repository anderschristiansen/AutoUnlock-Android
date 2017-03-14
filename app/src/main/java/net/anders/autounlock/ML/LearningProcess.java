package net.anders.autounlock.ML;

import android.util.Log;

import net.anders.autounlock.CoreService;
import net.anders.autounlock.ML.DataProcessing.Features;
import net.anders.autounlock.ML.DataSegmentation.WindowData;

import java.util.ArrayList;

/**
 * Created by Anders on 09-03-2017.
 */

public class LearningProcess {

    private static final String TAG = "LearningProcess";

    public static void Learn(ArrayList<ArrayList<WindowData>> trainingSessions)  {

        CoreService.orientationThreshold = 50;
        CoreService.velocityThreshold = 50;

        // For each train in unlocksessions
        for (int i = 0; i < trainingSessions.size(); i++) {

            if (CoreService.isClustered(i+1)) {
                break;
            }

            ArrayList<WindowData> currentTrain = trainingSessions.get(i);

            if (trainingSessions.get(i + 1) == null) { break;}

            ArrayList<WindowData> nextTrain = trainingSessions.get(i + 1);

            boolean cluster = true;

            // For each window in train
            for (int j = 0; j < currentTrain.size(); j++) {

                WindowData currentWindow = currentTrain.get(j);
                WindowData nextWindow = nextTrain.get(j);

                // If true break, else continue to investigate if sessions are similar
                if (!Features.processTraining(currentWindow, nextWindow)) {
                    cluster = false;
                    Log.i(TAG, "CLUSTER NOT FOUND: Training session " + String.valueOf(i) + " and " + String.valueOf(i + 1));
                    break;
                }
            }

            if (cluster) {
                // do something
                Log.i(TAG, "CLUSTER FOUND: Training session " + String.valueOf(i) + " and " + String.valueOf(i + 1));
                CoreService.updateCluster(i+1, 1);
                CoreService.updateCluster(i+2, 1);
            }
        }
    }
}



//
//    List<ObservationReal> sequencesOri = new ArrayList<>();
//    List<ObservationReal> sequencesVelo = new ArrayList<>();
//
//        sequencesOri.add(new ObservationReal(2.2));
//                sequencesOri.add(new ObservationReal(2.2));
//                sequencesOri.add(new ObservationReal(2.4));
//                sequencesOri.add(new ObservationReal(2.4));
//
//                sequencesVelo.add(new ObservationReal(2.2));
//                sequencesVelo.add(new ObservationReal(2.2));
//                sequencesVelo.add(new ObservationReal(2.4));
//                sequencesVelo.add(new ObservationReal(2.4));
//
//                try {
//                Training t = new Training(sequencesOri, sequencesVelo);
//                } catch (InterruptedException e) {
//                e.printStackTrace();
//                } catch (IOException e) {
//                e.printStackTrace();
//                } catch (FileFormatException e) {
//                e.printStackTrace();
//                }
