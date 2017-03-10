package net.anders.autounlock.ML.DataPreprocessing;

import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.ML.DataSegmentation.WindowData;

import java.util.List;

/**
 * Created by Anders on 10-03-2017.
 */

public class WindowConstruction {

    public static WindowData buildWindow(List<AccelerometerData> rawAccelerometerData, WindowData prevWindow) {

        float meanAccX, meanAccY, meanOri;
        float sumAccX = 0, sumAccY = 0, sumOri = 0;

        double speedX, speedY;
        double time_current;
        double speedX_prev = 0, speedY_prev = 0;
        double time_prev;

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
        }
        meanAccX = sumAccX / rawAccelerometerData.size();
        meanAccY = sumAccY / rawAccelerometerData.size();
        meanOri = sumOri / rawAccelerometerData.size();

        time_current = System.currentTimeMillis() * Math.pow(10, -3);

        if (speedX_prev ==  0 && speedY_prev == 0) {
            speedX = speedX_prev + meanAccX;
            speedY = speedY_prev + meanAccY;
        } else {
            speedX = speedX_prev + meanAccX * Math.pow(time_current - time_prev, 2);
            speedY = speedY_prev + meanAccY * Math.pow(time_current - time_prev, 2);
//            speedX = speedX_prev + meanAccX * (time_current - time_prev);
//            speedY = speedY_prev + meanAccY * (time_current - time_prev);
        }

        double velocity = Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
        double degree = (Math.atan(speedX/speedY)*180)/Math.PI;

//        Log.i(TAG, "grader: " + String.valueOf(velocity) + " -- " + String.valueOf(degree));

        return new WindowData(meanAccX, meanAccY, speedX, speedY, meanOri, velocity, time_current);
    }
}
