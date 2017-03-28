package net.anders.autounlock.MachineLearning;

import android.util.Log;

import net.anders.autounlock.CoreService;

import java.util.ArrayList;

/**
 * Created by Anders on 15-03-2017.
 */


public class Clustering {

    private static final String TAG = "Clustering";

    public static ArrayList<UnlockData> AnalyseClusters(ArrayList<UnlockData> unlocks) {

        ArrayList<UnlockData> clusters = new ArrayList<>();

        // For each train in unlockunlocks
        for (int i = 0; i < unlocks.size(); i++) {

            UnlockData currentUnlock = unlocks.get(i);

            // If the unlock is already clustered, skip
            if (!CoreService.isClustered(i+1)) {

                for (int j = 0; j < unlocks.size(); j++) {
                    if(j!=i){
                        UnlockData nextUnlock = unlocks.get(j);

                        boolean cluster = true;

                        // Compares each window in i train with against the next train
                        for (int k = 0; k < currentUnlock.getWindows().size(); k++) {
                            WindowData currentWindow = currentUnlock.getWindows().get(k);
                            WindowData nextWindow = nextUnlock.getWindows().get(k);

                            // Break if the windows are not within the required thresholds,
                            // else continue to investigate if unlocks should be clustered
                            if (!similarity(currentWindow, nextWindow)) {
                                cluster = false;
                                Log.i(TAG, "CLUSTER NOT EXISTING: Record unlock " + String.valueOf(i) + " and " + String.valueOf(i+1));
                                break;
                            }
                        }
                        if (cluster) {
                            // Update the two train unlocks to be clustered together
                            Log.i(TAG, "CLUSTER FOUND: Record unlock " + String.valueOf(i) + " and " + String.valueOf(i+1));
                            CoreService.updateCluster(i+1, j+1); // Current train
                            break;
                        }
                    }
                }
            }
            clusters.add(new UnlockData(currentUnlock.getId(), CoreService.getClusterId(i+1), currentUnlock.getWindows()));
        }
        return clusters;
    }

    public static boolean similarity(WindowData currentWindow, WindowData nextWindow) {

        double c_ori = currentWindow.getOrientation();
        double c_velo = currentWindow.getVelocity();
        double n_ori = nextWindow.getOrientation();
        double n_velo = nextWindow.getVelocity();

        if ((Math.abs(c_ori - n_ori) < CoreService.orientationThreshold) &&
                (Math.abs(c_velo - n_velo) < CoreService.velocityThreshold)) {
            return true;
        }
        return false;
    }
}

