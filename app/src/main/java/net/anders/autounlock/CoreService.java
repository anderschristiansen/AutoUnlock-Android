package net.anders.autounlock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import net.anders.autounlock.Export.Export;
import net.anders.autounlock.MachineLearning.PatternRecognitionService;
import net.anders.autounlock.MachineLearning.UnlockData;
import net.anders.autounlock.MachineLearning.WindowProcessor;
import net.anders.autounlock.MachineLearning.LearningProcess;
import net.anders.autounlock.MachineLearning.WindowData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationReal;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

public class CoreService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private static final String TAG = "CoreService";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private Intent accelerometerIntent;
    private Intent bluetoothIntent;
    private Intent wifiIntent;
    private Intent locationIntent;
    private Intent scannerIntent;
    private Intent patternRecognitionIntent;

    private GoogleApiClient mGoogleApiClient;
    private net.anders.autounlock.Geofence geofence;

    static List<BluetoothData> recordedBluetooth = new ArrayList<BluetoothData>();
    static List<WifiData> recordedWifi = new ArrayList<WifiData>();
    static List<LocationData> recordedLocation = new ArrayList<LocationData>();
    static volatile ArrayList<String> activeInnerGeofences = new ArrayList<>();
    static ArrayList<String> activeOuterGeofences = new ArrayList<>();

    static float currentOrientation = -1f;

    static boolean isLocationDataCollectionStarted = false;
    static boolean isDetailedDataCollectionStarted = false;
    static volatile boolean isScanningForLocks = false;

    static DataStore dataStore;

    public static RingBuffer<WindowData> windowBuffer;
    public static int windowBufferSize;
    public static int windowSize;
    public static double windowPercentageOverlap;
    public static int windowOverlap;
    public static int reqUnlockTraining;
    public static int orientationThreshold;
    public static int velocityThreshold;
    public static double activityThreshold;
    public static boolean isPatternRecognitionRunning = false;
    public static boolean isTraining = false;
    public static boolean isMoving = false;

    public static List<Hmm<ObservationVector>> hmmVecList = new ArrayList<>();

    // Binder given to clients
    private final IBinder localBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {
        CoreService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CoreService.this;
        }
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download adapter file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create adapter
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        // Running the service in the foreground by creating adapter notification
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.bekey_logo)
                .setContentTitle("AutoUnlock")
                .setContentText("Service running in the background")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);

        dataStore = new DataStore(this);
        geofence = new Geofence();

        accelerometerIntent = new Intent(this, AccelerometerService.class);
        locationIntent = new Intent(this, LocationService.class);
        wifiIntent = new Intent(this, WifiService.class);
        bluetoothIntent = new Intent(this, BluetoothService.class);
        scannerIntent = new Intent(this, ScannerService.class);
        patternRecognitionIntent = new Intent(this, PatternRecognitionService.class);

        buildGoogleApiClient();

        IntentFilter geofencesFilter = new IntentFilter();
        geofencesFilter.addAction("GEOFENCES_ENTERED");
        geofencesFilter.addAction("GEOFENCES_EXITED");
        registerReceiver(geofencesReceiver, geofencesFilter);

        IntentFilter startPatternRecognitionFilter = new IntentFilter();
        startPatternRecognitionFilter.addAction("START_PATTERNRECOGNITION");
        startPatternRecognitionFilter.addAction("STOP_PATTERNRECOGNITION");
        startPatternRecognitionFilter.addAction("INCORRECT_UNLOCK");
        registerReceiver(startPatternRecognitionReceiver, startPatternRecognitionFilter);


        /*  MACHINE LEARNING STATS */
        CoreService.windowBufferSize = 50;
        CoreService.windowSize = 20;
        CoreService.windowPercentageOverlap = .2; // Last % of current window will be overlapping toused in the next window
        CoreService.windowOverlap =  CoreService.windowSize - ((int)(CoreService.windowSize *  CoreService.windowPercentageOverlap));
        CoreService.reqUnlockTraining = 5;
        CoreService.orientationThreshold = 50;
        CoreService.velocityThreshold = 50;
        CoreService.activityThreshold = 0;

        Log.v("CoreService", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send adapter message to start adapter job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        googleConnect();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        googleDisconnect();
        Log.v("CoreService", "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // localBinder is used for bound services
        return localBinder;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    void googleConnect() {
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    void googleDisconnect() {
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "Connected ");
        addGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    private BroadcastReceiver geofencesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!dataStore.getUnlocks().isEmpty()) {
                MainActivity.lockView.setText("Updating intelligence \n please be patient");
                trainHMM();
            }

            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            List<String> triggeringGeofencesList = extras.getStringArrayList("Geofences");

            Log.i(TAG, "onReceive: " + extras.getStringArrayList("Geofences"));

            if ("GEOFENCES_ENTERED".equals(action)) {
                for (String geofence : triggeringGeofencesList) {
                    if (geofence.contains("inner")) {
                        Log.i(TAG, "Entered inner geofence");
                        activeInnerGeofences.add(geofence.substring(5));
                        if (!isDetailedDataCollectionStarted) {
                            Log.i(TAG, "onReceive: starting detailed data collection");
                            isDetailedDataCollectionStarted = true;
                            isScanningForLocks = true;
                            startAccelerometerService();
                            startBluetoothService();
                            startWifiService();
                            scanForLocks();

                            MainActivity.lockDoor.setVisibility(View.VISIBLE);
                            MainActivity.unlockDoor.setVisibility(View.VISIBLE);
                            MainActivity.lockView.setVisibility(View.GONE);
                        }
                    } else if (geofence.contains("outer")) {
                        Log.i(TAG, "Entered outer geofence");
                        activeOuterGeofences.add(geofence.substring(5));
                        startRingBuffer();
                        if (!isLocationDataCollectionStarted) {
                            isLocationDataCollectionStarted = true;
                            startLocationService();
                        }
                    }
                }
            } else if ("GEOFENCES_EXITED".equals(action)) {
                for (String geofence : triggeringGeofencesList) {
                    if (geofence.contains("inner")) {
                        Log.i(TAG, "Exited inner geofence");
                        if (isDetailedDataCollectionStarted && activeInnerGeofences.isEmpty()) {
                            isDetailedDataCollectionStarted = false;
                            isScanningForLocks = false;
                            stopAccelerometerService();
                            stopBluetoothService();
                            stopWifiService();
                            stopPatternRecognitionService();

                            MainActivity.lockDoor.setVisibility(View.GONE);
                            MainActivity.unlockDoor.setVisibility(View.GONE);
                            MainActivity.lockView.setVisibility(View.VISIBLE);
                        }
                    } else if (geofence.contains("outer")) {
                        Log.i(TAG, "Entered outer geofence");

                        if (isLocationDataCollectionStarted && activeOuterGeofences.isEmpty()) {
                            isLocationDataCollectionStarted = false;
                            stopLocationService();
                        }
                    }
                }
            }
        }
    };

    private BroadcastReceiver startPatternRecognitionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            Log.e(TAG, "StartPatternRecognition");

            if ("START_PATTERNRECOGNITION".equals(action)) {
                startPatternRecognitionService();
            } else if ("STOP_PATTERNRECOGNITION".equals(action)) {
                isTraining = true;
                stopAccelerometerService();
                stopBluetoothService();
                stopWifiService();
                stopLocationService();
                stopPatternRecognitionService();
                isScanningForLocks = false;
                isDetailedDataCollectionStarted = false;
                isLocationDataCollectionStarted = false;
            } else if ("INCORRECT_UNLOCK".equals(action)) {
                dataStore.deleteCluster(extras.getInt("Cluster"));

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                isTraining = false;
                startAccelerometerService();
                startBluetoothService();
                startWifiService();
                startLocationService();
                startPatternRecognitionService();
                isScanningForLocks = true;
                isDetailedDataCollectionStarted = true;
                isLocationDataCollectionStarted = true;
            }
        }
    };


    void startAccelerometerService() {
        Log.v(TAG, "Starting AccelerometerService");
        Thread accelerometerServiceThread = new Thread() {
            public void run() {
                startService(accelerometerIntent);
            }
        };
        accelerometerServiceThread.start();
    }

    void stopAccelerometerService() {
        Log.d("CoreService", "Trying to stop accelerometerService");
        stopService(accelerometerIntent);
    }

    void startLocationService() {
        Log.v(TAG, "Starting LocationService");
        Thread locationServiceThread = new Thread() {
            public void run() {
                startService(locationIntent);
            }
        };
        locationServiceThread.start();
    }

    void stopLocationService() {
        stopService(locationIntent);
    }

    void startWifiService() {
        Log.v(TAG, "Starting WifiService");
        Thread wifiServiceThread = new Thread() {
            public void run() {
                startService(wifiIntent);
            }
        };
        wifiServiceThread.start();
    }

    void stopWifiService() {
        stopService(wifiIntent);
    }

    void startBluetoothService() {
        Log.v(TAG, "Starting BluetoothService");
        Thread bluetoothServiceThread = new Thread() {
            public void run() {
                startService(bluetoothIntent);
            }
        };
        bluetoothServiceThread.start();
    }

    void stopBluetoothService() {
        stopService(bluetoothIntent);
    }

    void startRingBuffer() {
        Log.d(TAG, "Starting data processing");
        windowBuffer = new RingBuffer(WindowData.class, windowBufferSize);
    }

    void addGeofences() {
        ArrayList<LockData> lockDataArrayList = dataStore.getKnownLocks();
        if (!lockDataArrayList.isEmpty()) {
            for (int i = 0; i < lockDataArrayList.size(); i++) {
                geofence.addGeofence(lockDataArrayList.get(i));
            }
            registerGeofences();
        }
    }

    void registerGeofences() {
        geofence.registerGeofences(this, mGoogleApiClient);
    }

    void unregisterGeofences() {
        geofence.unregisterGeofences(this, mGoogleApiClient);
    }

    static void newTruePositive() { long time = System.currentTimeMillis(); dataStore.insertDecision(0, time); }
    static void newFalseNegative() { long time = System.currentTimeMillis(); dataStore.insertDecision(1, time); }
    static void newFalsePositive() { long time = System.currentTimeMillis(); dataStore.insertDecision(2, time); }
    static void newTrueNegative() { long time = System.currentTimeMillis(); dataStore.insertDecision(3, time); }

    void saveLock(final String lockMAC) {
        new Thread(new Runnable() {
            public void run() {
                boolean success = true;
                String passphrase = "";

                startBluetoothService();
                startWifiService();
                startLocationService();

                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                stopBluetoothService();
                stopWifiService();
                stopLocationService();

                if (success && recordedLocation.size() != 0) {
                    LocationData currentLocation = recordedLocation.get(recordedLocation.size() - 1);

                    LockData lockData = new LockData(
                            lockMAC,
                            passphrase,
                            currentLocation,
                            30,
                            100,
                            -1f,
                            recordedBluetooth,
                            recordedWifi
                    );
                    Log.d(TAG, lockData.toString());
                    newLock(lockData);
                } else {
                    Log.e(TAG, "No location found, cannot add lock");
                }
            }
        }).start();
    }

    private boolean newLock(LockData lockData) {
        Log.d(TAG, "Inserting lock into db");
        dataStore.insertLockDetails(
                lockData.getMAC(),
                lockData.getPassphrase(),
                lockData.getLocation().getLatitude(),
                lockData.getLocation().getLongitude(),
                lockData.getInnerGeofence(),
                lockData.getOuterGeofence(),
                lockData.getOrientation(),
                System.currentTimeMillis()
        );

        for (int i = 0; i < lockData.getNearbyBluetoothDevices().size(); i++) {
            dataStore.insertBtle(
                    lockData.getNearbyBluetoothDevices().get(i).getName(),
                    lockData.getNearbyBluetoothDevices().get(i).getSource(),
                    lockData.getNearbyBluetoothDevices().get(i).getRssi(),
                    lockData.getMAC(),
                    lockData.getNearbyBluetoothDevices().get(i).getTime()
            );
        }

        for (int i = 0; i < lockData.getNearbyWifiAccessPoints().size(); i++) {
            dataStore.insertWifi(
                    lockData.getNearbyWifiAccessPoints().get(i).getSsid(),
                    lockData.getNearbyWifiAccessPoints().get(i).getMac(),
                    lockData.getNearbyWifiAccessPoints().get(i).getRssi(),
                    lockData.getMAC(),
                    lockData.getNearbyWifiAccessPoints().get(i).getTime()
            );
        }

        unregisterGeofences();
        addGeofences();
        registerGeofences();
        return true;
    }

    private void scanForLocks() {
        Log.e(TAG, "scanForLocks: " + activeInnerGeofences);
        Thread scannerServiceThread = new Thread() {
            public void run() {
                startService(scannerIntent);
            }
        };
        scannerServiceThread.start();
    }

    void onButtonClickAddLock() {
        saveLock(BluetoothService.ANDERS_BEKEY);
        unlockNow();
    }

    void onButtonClickUnlock() {
        if (dataStore.getKnownLocks().isEmpty()) {
            saveLock(BluetoothService.ANDERS_BEKEY);
        } else {
            if (isPatternRecognitionRunning) {
                stopPatternRecognitionService();
                isTraining = true;
            }
            handleUnlock();
        }
    }

    void onButtonClickLock() {
        if (dataStore.getKnownLocks().isEmpty()) {
            saveLock(BluetoothService.ANDERS_BEKEY);
        }
        lockNow();
    }

    void lockNow(){
        Toast.makeText(getApplicationContext(), "BeKey locked", Toast.LENGTH_SHORT).show();
    }

    void unlockNow() {
        Toast.makeText(getApplicationContext(), "BeKey unlocked", Toast.LENGTH_SHORT).show();
    }

    void handleUnlock() {
        WindowData[] snapshot = RingBuffer.getSnapshot();
        dataStore.insertUnlock(snapshot);

        Log.i(TAG, "Snapshot length: " + String.valueOf(snapshot.length));

        int cntUnlock = dataStore.getUnlockCount();

        if (cntUnlock >= reqUnlockTraining) {

            Log.v(TAG, "START TRAINING");
            hmmVecList = new ArrayList<>();

            // False negative condition as the door did not catch the unlock
            if (cntUnlock != reqUnlockTraining) {
                Log.v(TAG, "Inserting FN for unlocking");
                newFalseNegative();
            }

            // Start learning procedure
            trainHMM();

            Log.v(TAG, "TRAINING FINISHED");
            isTraining = false;
        }
    }

    public void trainHMM(){
        if (!dataStore.getUnlocks().isEmpty()) {
            LearningProcess.Start(dataStore.getUnlocks());
        }
    }

    public static boolean isClustered(int id) {
        return dataStore.isClustered(id);
    }

    public static void updateCluster(int cur_id, int next_id) {
        dataStore.updateCluster(cur_id, next_id);
    }

    public static int getClusterId(int id) {
        return dataStore.getClusterId(id);
    }

    public static void accelerometerEvent(AccelerometerData anAccelerometerEvent) {
        WindowProcessor.insertAccelerometerEventIntoWindow(anAccelerometerEvent);
    }

    private void startPatternRecognitionService() {
        isPatternRecognitionRunning = true;
        Log.v(TAG, "Starting PatternRecognitionService");
        Thread patternRecognitionServiceThread = new Thread() {
            public void run() {
                startService(patternRecognitionIntent);
            }
        };
        patternRecognitionServiceThread.start();
    }

    private void stopPatternRecognitionService() {
        isPatternRecognitionRunning = false;
        WindowProcessor.prevWindow = null;
        Log.d("CoreService", "Trying to stop PatternRecognitionService");
        stopService(patternRecognitionIntent);
    }

    void exportDB() {
        Export.Database();
        Toast.makeText(getApplicationContext(), "Database exported", Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<UnlockData> getUnlocks() {
        return dataStore.getUnlocks();
    }


    static boolean enviromentalScore(String foundLock) {
        return Enviroment.makeDecision(foundLock);

    }
}
