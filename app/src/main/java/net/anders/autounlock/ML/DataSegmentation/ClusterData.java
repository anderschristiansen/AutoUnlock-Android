package net.anders.autounlock.ML.DataSegmentation;

import java.util.ArrayList;

/**
 * Created by Anders on 14-03-2017.
 */

public class ClusterData {
    int id;
    ArrayList<WindowData> trainingSession;

    public ClusterData(int id, ArrayList<WindowData> trainingSession) {
        this.id = id;
        this.trainingSession = trainingSession;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<WindowData> getTrainingSession() { return trainingSession; }
    public void setTrainingSession(ArrayList<WindowData> trainingSession) { this.trainingSession = trainingSession; }
}

