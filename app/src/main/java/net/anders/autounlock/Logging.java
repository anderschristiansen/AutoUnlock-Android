package net.anders.autounlock;

import android.util.Log;

/**
 * Created by Anders on 06-01-2017.
 */

public class Logging {

    private static final String TAG = "Log";

    public static void TrainLog (String extraMac, int extraRSSI) {
        if (!true) {
            Log.v("IntentBroadcast", "MAC: " + extraMac);
            Log.v("IntentBroadcast", "RSSI: " + extraRSSI);
        }
    }

    public static void BluetoothDeviceInfo (String name, String address, int rssi, long timestamp) {
        if (!true) {
            Log.v("Bluetooth", name
                    + " " + address
                    + " " + rssi
                    + " " + timestamp);
        }
    }

    public static void WifiInfo (String res) {
        if (!true) {
            Log.v("Wifi", res);
        }
    }

    public static void GeofenceEntered(String res) {
        if (true) {
            Log.i(TAG, res + " geofence entered");
            MainActivity.geofenceStatusView.setText(res + " geofence entered");
        }
    }

    public static void GeofenceExited(String res) {
        if (true) {
            Log.i(TAG, res + " geofence exited");
            MainActivity.geofenceStatusView.setText(res + " geofence exited");
        }
    }

    public static void LockScore() {
    }

    public static void Location(double latitude, double longitude) {
            MainActivity.locationView.setText(latitude + ", " + longitude);
    }

    public static void LockScanning(boolean status) {
            if (status) {
                MainActivity.lockScanningView.setText(Boolean.toString(status));
            } else {
                MainActivity.lockScanningView.setText(Boolean.toString(status));
            }
    }

}
