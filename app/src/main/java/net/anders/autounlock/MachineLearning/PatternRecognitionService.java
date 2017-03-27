package net.anders.autounlock.MachineLearning;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import net.anders.autounlock.MachineLearning.HMM.Recognise;
import net.anders.autounlock.RingBuffer;

/**
 * Created by Anders on 22-02-2017.
 */

public class PatternRecognitionService extends Service {
    private volatile boolean running = true;

    private PatternRecognition patternRecognition;
    private Recognise recognise;

    private static String TAG = "PatternRecognitionService";

    @Override
    public void onCreate() {
        patternRecognition = new PatternRecognition();
        recognise = new Recognise();
    }

    @Override
    public void onDestroy() {
        patternRecognition.terminate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class PatternRecognition implements Runnable {

        @Override
        public void run() {

            Intent startRecognition = new Intent("START_RECOGNITION");
            sendBroadcast(startRecognition);

            while (running) {

                WindowData[] snapshot = RingBuffer.getSnapshot();
                recognise.recognise(snapshot);

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        private void terminate() {
            running = false;
        }
    }
}