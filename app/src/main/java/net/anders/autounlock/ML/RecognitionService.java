package net.anders.autounlock.ML;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import net.anders.autounlock.ML.DataSegmentation.WindowData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.RingProcessorService;

/**
 * Created by Anders on 22-02-2017.
 */

public class RecognitionService extends Service {
    private volatile boolean running = true;

    private Recognition recognition;
    private Thread activityCollector;

    private static String TAG = "RecognitionService";

    @Override
    public void onCreate() {
        recognition = new Recognition();
        activityCollector = new Thread(recognition);
        activityCollector.start();
    }

    @Override
    public void onDestroy() {
        recognition.terminate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class Recognition implements Runnable {

        @Override
        public void run() {

            // Numbers of times needed for manual unlocking before enough data is collected
            int manual = 10;
            int windowBufferSize = 1000;
            int windowSize = 30;
            // Last % of current window will be overlapping toused in the next window
            double percentageOverlap = 0;

            CoreService.manualUnLockCalibration = manual;
            CoreService.windowBufferSize = windowBufferSize;
            CoreService.windowSize = windowSize;
            CoreService.windowPercentageOverlap = percentageOverlap;
            CoreService.windowOverlap = windowSize - ((int)(windowSize * percentageOverlap));

            Intent startRecognition = new Intent("START_RECOGNITION");
            sendBroadcast(startRecognition);

            while (running) {


            }
        }
        private void terminate() {
            running = false;
        }
    }
}