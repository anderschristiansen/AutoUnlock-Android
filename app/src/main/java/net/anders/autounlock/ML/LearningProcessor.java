package net.anders.autounlock.ML;

import android.util.Log;

import net.anders.autounlock.CoreService;
import net.anders.autounlock.ML.DataProcessing.Features;
import net.anders.autounlock.ML.DataSegmentation.ClusterData;
import net.anders.autounlock.ML.DataSegmentation.ClustersData;
import net.anders.autounlock.ML.DataSegmentation.WindowData;
import net.anders.autounlock.ML.HMM.ModelTraining;

import java.util.ArrayList;

/**
 * Created by Anders on 09-03-2017.
 */

public class LearningProcessor {

    private static final String TAG = "LearningProcessor";

    public static void Start(ArrayList<ArrayList<WindowData>> trainingSessions)  {

        // Method to identify clusters in the training sessions
        ArrayList<ClusterData> clusters = ClusterTrainingSessions(trainingSessions);

        // Make models
         ModelTraining mt = new ModelTraining();
         mt.train(clusters);
    }

    private static ArrayList<ClusterData> ClusterTrainingSessions(ArrayList<ArrayList<WindowData>> trainingSessions) {

        ArrayList<ClusterData> clusters = new ArrayList<>();

        // For each train in unlocksessions
        for (int i = 0; i < trainingSessions.size(); i++) {

            ArrayList<WindowData> curTrain = trainingSessions.get(i);

            // If the train session is already clustered, skip
            if (!CoreService.isClustered(i+1)) {

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
                                Log.i(TAG, "CLUSTER NOT EXISTING: ModelTraining session " + String.valueOf(i) + " and " + String.valueOf(i+1));
                                break;
                            }
                        }
                        if (cluster) {
                            // Update the two train sessions to be clustered together
                            Log.i(TAG, "CLUSTER FOUND: ModelTraining session " + String.valueOf(i) + " and " + String.valueOf(i+1));
                            CoreService.updateCluster(i+1, j+1); // Current train
                            break;
                        }
                    }
                }
            }
            clusters.add(new ClusterData(CoreService.getClusterId(i+1), curTrain));
        }

        return clusters;
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
//                ModelTraining t = new ModelTraining(sequencesOri, sequencesVelo);
//                } catch (InterruptedException e) {
//                e.printStackTrace();
//                } catch (IOException e) {
//                e.printStackTrace();
//                } catch (FileFormatException e) {
//                e.printStackTrace();
//                }
