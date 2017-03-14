package net.anders.autounlock.ML;

import android.util.Log;

import net.anders.autounlock.CoreService;
import net.anders.autounlock.ML.DataProcessing.Features;
import net.anders.autounlock.ML.DataSegmentation.WindowData;

import java.util.ArrayList;

/**
 * Created by Anders on 09-03-2017.
 */

public class LearningProcessor {

    private static final String TAG = "LearningProcessor";

    public static void Learn(ArrayList<ArrayList<WindowData>> trainingSessions)  {

        // For each train in unlocksessions
        for (int i = 0; i < trainingSessions.size(); i++) {

            // If the train session is already clustered, skip
            if (!CoreService.isClustered(i+1)) {

                ArrayList<WindowData> curTrain = trainingSessions.get(i);

                for (int j = 0; j < trainingSessions.size(); j++) {
                    if(j!=i){
                        ArrayList<WindowData> nextTrain = trainingSessions.get(j);

                        boolean cluster = true;

                        // Compares each window in i train with against the next train
                        for (int k = 0; k < curTrain.size(); k++) {

                            WindowData curWindow = curTrain.get(k);
                            WindowData nextWindow = nextTrain.get(k);

                            // Break if the windows are not within the required thresholds,
                            // else continue to investigate if sessions should be clustered
                            if (!Features.processTraining(curWindow, nextWindow)) {
                                cluster = false;
                                Log.i(TAG, "CLUSTER NOT EXISTING: Training session " + String.valueOf(i) + " and " + String.valueOf(i+1));
                                break;
                            }
                        }

                        if (cluster) {
                            // Update the two train sessions to be clustered together
                            Log.i(TAG, "CLUSTER FOUND: Training session " + String.valueOf(i) + " and " + String.valueOf(i+1));
                            CoreService.updateCluster(i+1, j+1); // Current train
                            break;
                        }
                    }
                }
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
