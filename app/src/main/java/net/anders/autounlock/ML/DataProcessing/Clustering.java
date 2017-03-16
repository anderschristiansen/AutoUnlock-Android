package net.anders.autounlock.ML.DataProcessing;

import android.util.Log;

import net.anders.autounlock.CoreService;
import net.anders.autounlock.ML.DataSegmentation.SessionData;
import net.anders.autounlock.ML.DataSegmentation.WindowData;

import java.util.ArrayList;

/**
 * Created by Anders on 15-03-2017.
 */


public class Clustering {

    private static final String TAG = "Clustering";

    public static ArrayList<SessionData> AnalyseClusters(ArrayList<SessionData> sessions, boolean unlockDoor) {

        ArrayList<SessionData> clusters = new ArrayList<>();

        // For each train in unlocksessions
        for (int i = 0; i < sessions.size(); i++) {

            SessionData currentSession = sessions.get(i);

            // If the train session is already clustered, skip
            if (!CoreService.isClustered(i+1)) {

                for (int j = 0; j < sessions.size(); j++) {
                    if(j!=i){
                        SessionData nextSession = sessions.get(j);

                        boolean cluster = true;

                        // Compares each window in i train with against the next train
                        for (int k = 0; k < currentSession.getWindows().size(); k++) {
                            WindowData currentWindow = currentSession.getWindows().get(k);
                            WindowData nextWindow = nextSession.getWindows().get(k);

                            // Break if the windows are not within the required thresholds,
                            // else continue to investigate if sessions should be clustered
                            if (!Features.processTraining(currentWindow, nextWindow)) {
                                cluster = false;
                                Log.i(TAG, "CLUSTER NOT EXISTING: TrainHMM session " + String.valueOf(i) + " and " + String.valueOf(i+1));
                                break;
                            }
                        }
                        if (cluster) {
                            // Update the two train sessions to be clustered together
                            Log.i(TAG, "CLUSTER FOUND: TrainHMM session " + String.valueOf(i) + " and " + String.valueOf(i+1));
                            CoreService.updateCluster(i+1, j+1); // Current train
                            break;
                        }
                    }
                }
            }
            clusters.add(new SessionData(currentSession.getId(), CoreService.getClusterId(i+1), currentSession.getWindows()));
        }
        return clusters;
    }
}

