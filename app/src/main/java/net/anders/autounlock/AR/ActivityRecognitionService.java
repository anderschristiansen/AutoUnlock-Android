package net.anders.autounlock.AR;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 22-02-2017.
 */

public class ActivityRecognitionService extends Service {
    private volatile boolean running = true;

    private ActivityRecognition activityRecognition;
    private Thread activityCollector;

    private static String TAG = "ActivityRecognitionService";

    @Override
    public void onCreate() {
        activityRecognition = new ActivityRecognition();
        activityCollector = new Thread(activityRecognition);
        activityCollector.start();
    }

    @Override
    public void onDestroy() {
        activityRecognition.terminate();
//        CoreService.recordedBluetooth = new ArrayList<BluetoothData>();
//        CoreService.recordedLocation = new ArrayList<LocationData>();
//        CoreService.recordedWifi = new ArrayList<WifiData>();
    }

//    void sendDecisionIntent(List foundLocks) {
//        Intent startDecision = new Intent("START_DECISION");
//        sendBroadcast(startDecision);
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ActivityRecognition implements Runnable {

        @Override
        public void run() {

            CoreService.windowBufferSize = 200;
            CoreService.windowSize = 20;
            CoreService.windowOverlap = 0.50;

            Intent startRecognition = new Intent("START_RECOGNITION");
            sendBroadcast(startRecognition);

            while (running) {



                // In order to not have empty lists in the RingBuffer, previous data will be used if no new data has been found.

//                if (!CoreService.recordedAccelerometer.isEmpty() || prevRecordedAccelerometer.isEmpty()) {
//                    prevRecordedAccelerometer = CoreService.recordedAccelerometer;
//                    CoreService.recordedAccelerometer = new ArrayList<AccelerometerData>();
//                }


                // Do not start decision making before we have at least one nearby Bluetooth device (the lock),
                // and adapter location. We cannot be sure any Wifi access points are nearby.
//                if (!prevRecordedBluetooth.isEmpty() && !prevRecordedLocation.isEmpty()) {
//                    for (int i = 0; i < CoreService.recordedBluetooth.size(); i++) {
//                        if (CoreService.recordedBluetooth.get(i).getSource().equals(BluetoothService.ANDERS_BEKEY)) {
//                            Log.e("Start Decision", "BeKey found");
//                            foundLocks.add(CoreService.recordedBluetooth.get(i).getSource());
//                            Intent startDecision = new Intent("START_DECISION");
//                            startDecision.putStringArrayListExtra("Locks", foundLocks);
//                            sendBroadcast(startDecision);
//                        }
//                    }
//                    if (!foundLocks.isEmpty()) {
//                        sendDecisionIntent(foundLocks);
//                    }
//                }

//                StringBuilder stringBuilder = new StringBuilder();
//                for (int i = 0; i < prevRecordedAccelerometer.size(); i++) {
//                    stringBuilder.append(prevRecordedAccelerometer.get(i).toString());
//                }
//
//                for (int i = 0; i < prevRecordedBluetooth.size(); i++) {
//                    stringBuilder.append(prevRecordedBluetooth.get(i).toString());
//                }
//
//                for (int i = 0; i < prevRecordedLocation.size(); i++) {
//                    stringBuilder.append(prevRecordedLocation.get(i).toString());
//                }
//
//                for (int i = 0; i < prevRecordedWifi.size(); i++) {
//                    stringBuilder.append(prevRecordedWifi.get(i).toString());
//                }
//
//                Log.v("StringOUT", stringBuilder.toString());
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }

        private void terminate() {
            running = false;
        }
    }
}