package net.anders.autounlock.MachineLearning;

import net.anders.autounlock.MachineLearning.HMM.TrainingModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anders on 09-03-2017.
 */

public class LearningProcess {

    private static final String TAG = "LearningProcess";

    public static void Start(ArrayList<UnlockData> unlocks) {

        // Method to identify clusters in the training unlocks
        ArrayList<UnlockData> clusters = Clustering.AnalyseClusters(unlocks);

        // Create a map to hold sublists of clusters
        Map<Integer, ArrayList<UnlockData>> map = new HashMap<>();

        for (UnlockData unlock : clusters) {
            // Fetch the list for this object's cluster id
            ArrayList<UnlockData> temp = map.get(unlock.getClusterId());

            if (temp == null) {
                // If the list is null we haven't seen an
                // object with this cluster id before, so create
                // a new list and add it to the map
                temp = new ArrayList<UnlockData>();
                map.put(unlock.getClusterId(), temp);
            }
            temp.add(unlock);
        }

        File outputDirectory = new File("/sdcard/AutoUnlock/HMM/");
        DeleteRecursive(outputDirectory);

        for (Map.Entry<Integer, ArrayList<UnlockData>> entry : map.entrySet()) {
            TrainingModel model = new TrainingModel();

            ArrayList<UnlockData> temp = new ArrayList<>();

            for (UnlockData unlock : entry.getValue()) {
                temp.add(unlock);
            }

            // Skip cluster id with 0, as they have not been assignt yet
            if (temp.get(0).cluster_id != 0) {
                model.train(temp);
            }
        }
    }

    private static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
            {
                child.delete();
                DeleteRecursive(child);
            }

        fileOrDirectory.delete();
    }
}