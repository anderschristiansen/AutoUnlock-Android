package net.anders.autounlock.MachineLearning;

import net.anders.autounlock.AccelerometerData;

import java.util.List;

/**
 * Created by Anders on 10-03-2017.
 */

public class WindowConstruction {

    public static WindowData buildWindow(List<AccelerometerData> rawAccelerometerData, WindowData prevWindow) {

        float meanAccX, meanAccY, meanOri, meanMag;
        float sumAccX = 0, sumAccY = 0, sumOri = 0, sumMag = 0;

        double speedX, speedY, time_current, time_prev;
        double speedX_prev = 0, speedY_prev = 0;

        if (prevWindow != null) {
            speedX_prev = prevWindow.getSpeedX();
            speedY_prev = prevWindow.getSpeedY();
            time_prev = prevWindow.getTime();
        } else {
            time_prev = 0;
        }

        for (AccelerometerData acc : rawAccelerometerData) {
            sumAccX += acc.getAccelerationX();
            sumAccY += acc.getAccelerationY();
            sumOri += acc.getOrientation();
            sumMag += Math.sqrt(acc.getAccelerationX()*acc.getAccelerationX() + acc.getAccelerationY()*acc.getAccelerationY() + acc.getAccelerationZ()*acc.getAccelerationZ());
        }
        meanAccX = sumAccX / rawAccelerometerData.size();
        meanAccY = sumAccY / rawAccelerometerData.size();
        meanOri = sumOri / rawAccelerometerData.size();
        meanMag = sumMag / rawAccelerometerData.size();

        time_current = System.currentTimeMillis() * Math.pow(10, -3);

        if (speedX_prev ==  0 && speedY_prev == 0) {
            speedX = speedX_prev + meanAccX;
            speedY = speedY_prev + meanAccY;
        } else {
            speedX = speedX_prev + meanAccX * Math.pow(time_current - time_prev, 2);
            speedY = speedY_prev + meanAccY * Math.pow(time_current - time_prev, 2);
        }

        double velocity = Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));

        return new WindowData(meanAccX, meanAccY, speedX, speedY, meanOri, velocity, meanMag, time_current);
    }
}
