package net.anders.autounlock.AR.DataSegmentation;

/**
 * Created by Anders on 02-03-2017.
 */

public class CoordinateData {
    float x;
    float y;
    float ori;

    public CoordinateData(float x, float y, float ori) {
        this.x = x;
        this.y = y;
        this.ori = ori;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) { this.x = x; }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getOri() {
        return ori;
    }
    public void setOri(float ori) {
        this.ori = ori;
    }


    @Override
    public String toString() {
        return "CoordinateData{" +
                "x=" + x +
                ", y=" + y +
                ", ori=" + ori +
                '}';
    }
}
