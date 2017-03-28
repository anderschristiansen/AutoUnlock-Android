package net.anders.autounlock.MachineLearning;

import net.anders.autounlock.MachineLearning.HMM.Record;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anders on 09-03-2017.
 */

public class LearningProcess {

    private static final String TAG = "LearningProcess";

    public static void Start(ArrayList<SessionData> sessions, boolean unlockDoor) {

        // Method to identify clusters in the training sessions
        ArrayList<SessionData> clusters = Clustering.AnalyseClusters(sessions, unlockDoor);

        // Create a map to hold sublists of clusters
        Map<Integer, ArrayList<SessionData>> map = new HashMap<>();

        for (SessionData session : clusters) {
            // Fetch the list for this object's cluster id
            ArrayList<SessionData> temp = map.get(session.getClusterId());

            if (temp == null) {
                // If the list is null we haven't seen an
                // object with this cluster id before, so create
                // a new list and add it to the map
                temp = new ArrayList<SessionData>();
                map.put(session.getClusterId(), temp);
            }
            temp.add(session);
        }

        File outputDirectory = new File("/sdcard/AutoUnlock/HMM/");
        DeleteRecursive(outputDirectory);

        for (Map.Entry<Integer, ArrayList<SessionData>> entry : map.entrySet()) {
            Record model = new Record();

            ArrayList<SessionData> temp = new ArrayList<>();

            for (SessionData session : entry.getValue()) {
                temp.add(session);
            }

            // Skip cluster id with 0, as they have not been assignt yet
            if (temp.get(0).cluster_id != 0) {
                model.record(temp, unlockDoor);
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
//        Recognise reg = new Recognise();
//
//        SessionData c = clusters.get(4);
//        reg.recognise(c);
//
//        SessionData c2 = clusters.get(5);
//        reg.recognise(c2);
//
//        SessionData c3 = clusters.get(1);
//        reg.recognise(c3);

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
//                Record t = new Record(sequencesOri, sequencesVelo);
//                } catch (InterruptedException e) {
//                e.printStackTrace();
//                } catch (IOException e) {
//                e.printStackTrace();
//                } catch (FileFormatException e) {
//                e.printStackTrace();
//                }
