package net.anders.autounlock.MachineLearning;

import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.RingBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 21-02-2017.
 */

public class WindowProcess {

    private static final String TAG = "WindowProcess";

    private List<AccelerometerData> currentAccelerometerList = new ArrayList<>();
    private List<AccelerometerData> nextAccelerometerList = new ArrayList<>();
    public static WindowData prevWindow;

    public WindowProcess(AccelerometerData anAccelerometerEvent) {
        insertAccelerometerEventIntoWindow(anAccelerometerEvent);
    }

    public void insertAccelerometerEventIntoWindow(AccelerometerData anAccelerometerEvent) {

        currentAccelerometerList.add(anAccelerometerEvent);

        // Numbers of overlapping values in integers
        int overlap = CoreService.windowOverlap;

        // Adds accelerometerdata if it is needed for the next sliding window
        if (overlap < currentAccelerometerList.size()) {
            nextAccelerometerList.add(anAccelerometerEvent);
        }

        // Convert current accelerometerdata segments into a window
        if (currentAccelerometerList.size() == CoreService.windowSize) {
            processWindow(currentAccelerometerList);
            currentAccelerometerList.addAll(nextAccelerometerList);
            nextAccelerometerList.clear();
        }
    }

    private void processWindow(List<AccelerometerData> rawAccelerometerData) {

        WindowData window = buildWindow(rawAccelerometerData, prevWindow);

        if (window.getAccelerationMag() > CoreService.activityThreshold) {
            // Put new window into the circular buffer
            RingBuffer.addWindow(window);

            if (CoreService.trainingComplete) {
                CoreService.isMoving = true;
            }
        }
        CoreService.isMoving = false;
        prevWindow = window;
        currentAccelerometerList.clear();
    }

    public WindowData buildWindow(List<AccelerometerData> rawAccelerometerData, WindowData prevWindow) {

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
