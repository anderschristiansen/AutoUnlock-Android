package net.anders.autounlock.AR.DataSegmentation;

import net.anders.autounlock.CoreService;

/**
 * Created by Anders on 22-02-2017.
 */

public class WindowData {

    double accelerationX;
    double accelerationY;
    long time;

    public WindowData(double accelerationX, double accelerationY, long time) {
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.time = time;
    }

    public double getAccelerationX() {
        return accelerationX;
    }
    public void setAccelerationX(int accelerationX) {
        this.accelerationX = accelerationX;
    }

    public double getAccelerationY() {
        return accelerationY;
    }
    public void setAccelerationY(double accelerationY) {
        this.accelerationY = accelerationY;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
}

