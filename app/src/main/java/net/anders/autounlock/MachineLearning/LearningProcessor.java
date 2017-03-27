package net.anders.autounlock.MachineLearning;

import net.anders.autounlock.MachineLearning.HMM.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anders on 09-03-2017.
 */

public class LearningProcessor {

    private static final String TAG = "LearningProcessor";

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

        for (Map.Entry<Integer, ArrayList<SessionData>> entry : map.entrySet()) {
            Record model = new Record();

            ArrayList<SessionData> temp = new ArrayList<>();

            for (SessionData session : entry.getValue()) {
                temp.add(session);
            }
            model.record(temp, unlockDoor);
        }
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
