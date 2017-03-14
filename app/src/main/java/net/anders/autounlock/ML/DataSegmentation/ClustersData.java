package net.anders.autounlock.ML.DataSegmentation;

import java.util.ArrayList;

/**
 * Created by Anders on 14-03-2017.
 */

public class ClustersData {
    private int id;
    private ArrayList<ClusterData> clusters;

    public ClustersData(int id, ArrayList<ClusterData> clusters) {
        this.id = id;
        this.clusters = clusters;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<ClusterData> getClusters() {
        return clusters;
    }

    public void setCluster(ArrayList<ClusterData> clusters) {
        this.clusters = clusters;
    }

    public void addCluster(int id, ClusterData cluster) {
        this.clusters.add(cluster);
        this.id = id;
    }

}
