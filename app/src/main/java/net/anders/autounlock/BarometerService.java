package net.anders.autounlock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Anders on 27-02-2017.
 */

public class BarometerService extends Service implements SensorEventListener {
    static String TAG = "BarometerService";
    //private static final boolean ADAPTIVE_ACCELEROMETER_FILTER = true;

    int startMode;       // indicates how to behave if the service is killed
    IBinder binder;      // interface for clients that bind
    boolean allowRebind; // indicates whether onRebind should be used

    private SensorManager sensorManager;
    private Sensor barometerSensor;

//    private float[] gravity = new float[3];
//    private float[] magneticField = new float[3];
//    private float[] linearAcceleration = new float[4];
//    private float[] previousAcceleration = new float[3];
//    private float[] accelerationFilter = new float[3];
//    private float[] rotationVector = new float[5];
//    private float[] gyroscope = new float[3];

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

//    private static final float NS2S = 1.0f / 1000000000.0f;
//    private float previousTimestamp;
//    private float dT = 0;
//    private float previousVelocity[] = new float[3];
//    private long startTime;


    public void onCreate() {
        // The service is being created
        powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BarometerService");
        wakeLock.acquire();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        //startTime = System.currentTimeMillis();
    }


    public void onSensorChanged(SensorEvent event) {
        long timestamp = event.timestamp;
        float value = event.values[0];

        String valueString = String.valueOf(value);
        //baroText.setText(valueString);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //We do not take accuracy into account
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to adapter call to startService()
        return startMode;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return allowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        Log.v("BarometerService", "Stopping");

        sensorManager.unregisterListener(this, barometerSensor);
        wakeLock.release();
    }
}
