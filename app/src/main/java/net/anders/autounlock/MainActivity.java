package net.anders.autounlock;

import android.Manifest;
import android.app.ActivityManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.anders.autounlock.Export.Export;

import java.io.IOException;

import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CoreService coreService;
    private boolean bound = false;

    static TextView lockView;

    static Button addLock;
    static Button unlockDoor;
    static Button lockDoor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addLock = (Button) findViewById(R.id.addlock);
        unlockDoor = (Button) findViewById(R.id.unlock);
        lockDoor = (Button) findViewById(R.id.lock);

        DataStore dataStore = new DataStore(this);

        /* TextViews for UI */
        lockView = (TextView) findViewById(R.id.lockView);

        IntentFilter filter = new IntentFilter();
        filter.addAction("BTLE_CONN");

        if (!dataStore.getKnownLocks().isEmpty()) {
            addLock.setVisibility(View.GONE);
            lockView.setText("WAIT FOR LOCK");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isMyServiceRunning(CoreService.class)) {
            bindService(new Intent(this, CoreService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            ComponentName coreService = startService(new Intent(this, CoreService.class));
            bindService(new Intent(this, CoreService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        }

        // Check for location permission on startup if not granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else

            // Check for permission to write to external storage.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//    public void onButtonClickTruePositive(View v) {
//        if (bound) {
//            coreService.newTruePositive();
//            Log.d("Manual Decision", "True Positive");
//        }
//    }
//
//    public void onButtonClickFalsePositive(View v) {
//        if (bound) {
//            coreService.newFalsePositive();
//            Log.d("Manual Decision", "False Positive");
//        }
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("Permission", "Permission grannted, yay");
                    if (bound) {
                        coreService.googleConnect();
                    }
                } else {
                    Toast.makeText(this, "The app needs access to location in order to function.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void onButtonClickExportDatastore(View v) {
        coreService.exportDB();
//        Export.Windows(RingBuffer.getSnapshot());
    }

    // TODO ABC
    public void onButtonClickAddLock(View v) {
        if (bound) {
            coreService.onButtonClickAddLock();
            addLock.setVisibility(View.GONE);
            lockView.setText("SCANNING FOR LOCK");
        }
    }

    public void onButtonClickUnlock(View v) {
        if (bound) {
            coreService.onButtonClickUnlock();
        }
    }

    public void onButtonClickLock(View v) {
        if (bound) {
            coreService.onButtonClickLock();
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CoreService.LocalBinder binder = (CoreService.LocalBinder) service;
            coreService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}
