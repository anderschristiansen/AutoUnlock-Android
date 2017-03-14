package net.anders.autounlock.ML.DataSegmentation;

/**
 * Created by Anders on 22-02-2017.
 */

public class WindowData {

    double accelerationX;
    double accelerationY;
    double speedX;
    double speedY;
    double orientation;
    double velocity;
    double time;

    public WindowData(double accelerationX, double accelerationY, double speedX, double speedY, double orientation, double velocity, double time) {
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.speedX = speedX;
        this.speedY = speedY;
        this.orientation = orientation;
        this.velocity = velocity;
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

    public double getSpeedX() {
        return speedX;
    }
    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public double getSpeedY() {
        return speedY;
    }
    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public double getOrientation() {
        return orientation;
    }
    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public double getVelocity() {
        return velocity;
    }
    public void setVelocity(double velocity) { this.velocity = velocity;}

    public double getTime() {
        return time;
    }
    public void setTime(double time) {
        this.time = time;
    }
}

