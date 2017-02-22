package net.anders.autounlock.AR.DataSegmentation;

/**
 * Created by Anders on 21-02-2017.
 */

public class Segment {

    public float getAccTot() {
        return accTot;
    }

    public void setAccTot(float accTot) {
        this.accTot = accTot;
    }

    float accTot;

    public Segment(float accTot) {
        this.accTot = accTot;
    }
}
