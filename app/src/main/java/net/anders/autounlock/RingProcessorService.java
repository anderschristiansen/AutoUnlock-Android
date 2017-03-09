package net.anders.autounlock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import net.anders.autounlock.AR.DataSegmentation.WindowData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 08-03-2017.
 */

// "Firstly, it's faster than something like a linked list because it's an array, and has a predictable pattern of access.
// This is nice and CPU-cache-friendly - at the hardware level the entries can be pre-loaded,
// so the machine is not constantly going back to main memory to load the next item in the ring."
// http://mechanitis.blogspot.dk/2011/06/dissecting-disruptor-whats-so-special.html

public class RingProcessorService extends Service {

    private volatile boolean running = true;
    private WindowData window;

    private RingProcessor ringProcessor;
    private Thread ringCollector;

    private static String TAG = "RingProcessorService";

    @Override
    public void onCreate() {
        ringProcessor = new RingProcessorService.RingProcessor();
        ringCollector = new Thread(ringProcessor);
        ringCollector.start();
    }

    @Override
    public void onDestroy() {
        ringProcessor.terminate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class RingProcessor implements Runnable {

        @Override
        public void run() {

            while (running) {

                if (CoreService.initiateSnapshot) {
                    WindowData[] snapshot = CoreService.windowBuffer.snapshot();

                    if (snapshot.length != 0) {
                        WindowData window = snapshot[snapshot.length-1];
                        Log.i(TAG, String.valueOf(window.getAccelerationX()) + " " +String.valueOf(snapshot.length));
                    }
                    CoreService.initiateSnapshot = false;
                }


//                AccelerometerData[] snapshot = CoreService.rawBuffer.snapshot();
//                Log.i(TAG, String.valueOf(snapshot.length));
//                while (!CoreService.rawEmpty) {
//                    AccelerometerData acc = CoreService.rawBuffer.get();
//                    int head = CoreService.rawBuffer.getHead();
//                    int length = CoreService.rawBuffer.getLength();
//
//                    if (length != 0) {
//
//                        if (head == prev_head_counter + 1) {
//                            Log.i(TAG, String.valueOf(head));
//                            x += acc.getAccelerationX();
//                            y += acc.getAccelerationY();
//                            raw_counter++;
//
//                            if (raw_counter == CoreService.rawBufferSize) {
//                                window = new WindowData(x, y, System.currentTimeMillis());
//                                CoreService.windowBuffer.add(window);
//                                raw_counter = 0;
//                            }
//
//                        }
//                        prev_head_counter = head;
//                    } else {
//                        x += acc.getAccelerationX();
//                        y += acc.getAccelerationY();
//                        raw_counter++;
//                        prev_head_counter = head;
//                    }
//                }
            }
        }

        private void terminate() {
            running = false;
        }
    }
}