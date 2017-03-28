package net.anders.autounlock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.anders.autounlock.MachineLearning.SessionData;
import net.anders.autounlock.MachineLearning.WindowData;

import java.util.ArrayList;

class DataStore {
    private static final String DATABASE_NAME = "datastore.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TIMESTAMP = "TIMESTAMP";

    private static final String LOCK_TABLE = "lock";
    private static final String LOCK_MAC = "MAC";
    private static final String LOCK_PASSPHRASE = "passphrase";
    private static final String LOCK_LATITUDE = "latitude";
    private static final String LOCK_LONGITUDE = "longitude";
    private static final String LOCK_INNER_GEOFENCE = "inner_geofence";
    private static final String LOCK_OUTER_GEOFENCE = "outer_geofence";
    private static final String LOCK_ORIENTATION = "orientation";

    private static final String BLUETOOTH_TABLE = "bluetooth";
    private static final String BLUETOOTH_NAME = "name";
    private static final String BLUETOOTH_SOURCE = "source";
    private static final String BLUETOOTH_RSSI = "RSSI";
    private static final String BLUETOOTH_NEARBY_LOCK = "nearby_lock";

    private static final String WIFI_TABLE = "wifi";
    private static final String WIFI_SSID = "SSID";
    private static final String WIFI_MAC = "MAC";
    private static final String WIFI_RSSI = "RSSI";
    private static final String WIFI_NEARBY_LOCK = "nearby_lock";

    private static final String LOCATION_TABLE = "location";
    private static final String LOCATION_PROVIDER = "provider";
    private static final String LOCATION_LATITUDE = "latitude";
    private static final String LOCATION_LONGITUDE = "longitude";
    private static final String LOCATION_ACCURACY = "accuracy";
    private static final String LOCATION_DATETIME = "datetime";

    private static final String SESSION_TABLE = "session";
    private static final String SESSION_ID = "id";
    private static final String SESSION_DOOR_UNLOCK = "door_unlock";
    private static final String SESSION_CLUSTER = "cluster";

    private static final String WINDOW_TABLE = "window";
    private static final String WINDOW_SESSION_ID = "session_id";
    private static final String WINDOW_ORIENTATION = "orientation";
    private static final String WINDOW_VELOCITY = "velocity";
    private static final String WINDOW_ACCELERATION_X = "acceleration_x";
    private static final String WINDOW_ACCELERATION_Y = "acceleration_y";
    private static final String WINDOW_SPEED_X = "speed_x";
    private static final String WINDOW_SPEED_Y = "speed_y";
    private static final String WINDOW_ACCELERATION_MAG = "acceleration_mag";

    private static final String DECISION_TABLE = "decision";
    private static final String DECISION_DECISION = "decision";
    private static final String DECISION_DOOR_UNLOCK = "door_unlock";

    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    DataStore(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    void insertLockDetails(
            String lockMAC,
            String lockPassphrase,
            double lockLatitude,
            double lockLongitude,
            float lockInnerGeofence,
            float lockOuterGeofence,
            float lockOrientation,
            long timestamp
    ) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCK_MAC, lockMAC);
        contentValues.put(LOCK_PASSPHRASE, lockPassphrase);
        contentValues.put(LOCK_LATITUDE, lockLatitude);
        contentValues.put(LOCK_LONGITUDE, lockLongitude);
        contentValues.put(LOCK_INNER_GEOFENCE, lockInnerGeofence);
        contentValues.put(LOCK_OUTER_GEOFENCE, lockOuterGeofence);
        contentValues.put(LOCK_ORIENTATION, lockOrientation);
        contentValues.put(TIMESTAMP, timestamp);

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.replace(LOCK_TABLE, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    ArrayList<LockData> getKnownLocks() {
        ArrayList<LockData> lockDataArrayList = new ArrayList<>();

        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();

            String lockQuery = "SELECT * FROM " + LOCK_TABLE + ";";
            Cursor lockCursor = database.rawQuery(lockQuery, null);

            lockCursor.moveToFirst();
            if (!lockCursor.isAfterLast()) {
                for (int i = 0; i < lockCursor.getCount(); i++) {
                    String lockMac = lockCursor.getString(lockCursor.getColumnIndex(LOCK_MAC));
                    double lockLatitude = lockCursor.getDouble(lockCursor.getColumnIndex(LOCK_LATITUDE));
                    double lockLongitude = lockCursor.getDouble(lockCursor.getColumnIndex(LOCK_LONGITUDE));
                    float innerGeofence = lockCursor.getInt(lockCursor.getColumnIndex(LOCK_INNER_GEOFENCE));
                    float outerGeofence = lockCursor.getInt(lockCursor.getColumnIndex(LOCK_OUTER_GEOFENCE));
                    float orientation = lockCursor.getFloat(lockCursor.getColumnIndex(LOCK_ORIENTATION));
                    LockData lockData = new LockData(
                            lockMac,
                            new LocationData(lockLatitude, lockLongitude),
                            innerGeofence,
                            outerGeofence,
                            orientation
                    );
                    lockDataArrayList.add(lockData);
                }
            }
            lockCursor.close();
        } finally {
            database.endTransaction();
        }
        return lockDataArrayList;
    }

    LockData getLockDetails(String foundLock) {
        LockData lockData;
        LocationData locationData;
        BluetoothData bluetoothData;
        WifiData wifiData;

        String lockMac;
        String lockPassphrase;
        double lockLatitude;
        double lockLongitude;
        float innerGeofence;
        float outerGeofence;
        float orientation;

        ArrayList<BluetoothData> nearbyBluetoothDevices = new ArrayList<>();
        ArrayList<WifiData> nearbyWifiAccessPoints = new ArrayList<>();

        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();

            String lockQuery = "SELECT * FROM " + LOCK_TABLE + " WHERE " + LOCK_MAC + "='" + foundLock + "';";
            Cursor lockCursor = database.rawQuery(lockQuery, null);

            lockCursor.moveToFirst();
            if (lockCursor.isAfterLast()) {
                // We have not found any locks and return null.
                lockCursor.close();
                return null;
            } else {
                lockMac = lockCursor.getString(lockCursor.getColumnIndex(LOCK_MAC));
                lockPassphrase = lockCursor.getString(lockCursor.getColumnIndex(LOCK_PASSPHRASE));
                lockLatitude = lockCursor.getDouble(lockCursor.getColumnIndex(LOCK_LATITUDE));
                lockLongitude = lockCursor.getDouble(lockCursor.getColumnIndex(LOCK_LONGITUDE));
                innerGeofence = lockCursor.getInt(lockCursor.getColumnIndex(LOCK_INNER_GEOFENCE));
                outerGeofence = lockCursor.getInt(lockCursor.getColumnIndex(LOCK_OUTER_GEOFENCE));
                orientation = lockCursor.getFloat(lockCursor.getColumnIndex(LOCK_ORIENTATION));
                lockCursor.close();
            }

            String bluetoothQuery = "SELECT * FROM " + BLUETOOTH_TABLE + " WHERE "
                    + BLUETOOTH_NEARBY_LOCK + "='" + foundLock + "';";
            Cursor bluetoothCursor = database.rawQuery(bluetoothQuery, null);
            if (bluetoothCursor.getColumnCount() != 0) {
                bluetoothCursor.moveToFirst();
                for (int i = 0; i <= bluetoothCursor.getColumnCount(); i++) {
                    String bluetoothName = bluetoothCursor.getString(bluetoothCursor.getColumnIndex(BLUETOOTH_NAME));
                    String bluetoothSource = bluetoothCursor.getString(bluetoothCursor.getColumnIndex(BLUETOOTH_SOURCE));
                    int bluetoothRSSI = bluetoothCursor.getInt(bluetoothCursor.getColumnIndex(BLUETOOTH_RSSI));
                    long bluetoothTimestamp = bluetoothCursor.getLong(bluetoothCursor.getColumnIndex(TIMESTAMP));

                    bluetoothData = new BluetoothData(bluetoothName, bluetoothSource, bluetoothRSSI, bluetoothTimestamp);
                    nearbyBluetoothDevices.add(bluetoothData);

                    if (!(bluetoothCursor.isLast() || bluetoothCursor.isAfterLast())) {
                        bluetoothCursor.moveToNext();
                    }
                }
            }
            bluetoothCursor.close();

            String wifiQuery = "SELECT * FROM " + WIFI_TABLE + " WHERE "
                    + WIFI_NEARBY_LOCK + "='" + foundLock + "';";
            Cursor wifiCursor = database.rawQuery(wifiQuery, null);
            if (wifiCursor.getColumnCount() != 0) {
                wifiCursor.moveToFirst();
                for (int i = 0; i <= wifiCursor.getColumnCount(); i++) {
                    String wifiSSID = wifiCursor.getString(wifiCursor.getColumnIndex(WIFI_SSID));
                    String wifiMAC = wifiCursor.getString(wifiCursor.getColumnIndex(WIFI_MAC));
                    int wifiRSSI = wifiCursor.getInt(wifiCursor.getColumnIndex(WIFI_RSSI));
                    long wifiTimestamp = wifiCursor.getLong(wifiCursor.getColumnIndex(TIMESTAMP));

                    wifiData = new WifiData(wifiSSID, wifiMAC, wifiRSSI, wifiTimestamp);
                    nearbyWifiAccessPoints.add(wifiData);

                    if (!(wifiCursor.isLast() || wifiCursor.isAfterLast())) {
                        wifiCursor.moveToNext();
                    }
                }
            }
            wifiCursor.close();

            locationData = new LocationData(lockLatitude, lockLongitude);
            lockData = new LockData(lockMac, lockPassphrase, locationData,
                    innerGeofence, outerGeofence, orientation, nearbyBluetoothDevices, nearbyWifiAccessPoints);
            return lockData;
        } finally {
            database.endTransaction();
        }
    }

    void insertBtle(String name, String btleSource, int btleRSSI, String nearbyLock, long timestamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BLUETOOTH_NAME, name);
        contentValues.put(BLUETOOTH_SOURCE, btleSource);
        contentValues.put(BLUETOOTH_RSSI, btleRSSI);
        contentValues.put(BLUETOOTH_NEARBY_LOCK, nearbyLock);
        contentValues.put(TIMESTAMP, timestamp);

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.replace(BLUETOOTH_TABLE, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    void insertWifi(String wifiSSID, String wifiMAC, int wifiRSSI, String nearbyLock, long timestamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WIFI_SSID, wifiSSID);
        contentValues.put(WIFI_MAC, wifiMAC);
        contentValues.put(WIFI_RSSI, wifiRSSI);
        contentValues.put(WIFI_NEARBY_LOCK, nearbyLock);
        contentValues.put(TIMESTAMP, timestamp);

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.replace(WIFI_TABLE, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    void insertLocation(String provider, double latitude, double longitude, float accuracy, String datetime, long timestamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCATION_PROVIDER, provider);
        contentValues.put(LOCATION_LATITUDE, latitude);
        contentValues.put(LOCATION_LONGITUDE, longitude);
        contentValues.put(LOCATION_ACCURACY, accuracy);
        contentValues.put(LOCATION_DATETIME, datetime);
        contentValues.put(TIMESTAMP, timestamp);

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.replace(LOCATION_TABLE, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    void insertDecision(int decision, boolean unlockDoor, long timestamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DECISION_DECISION, decision);
        contentValues.put(DECISION_DOOR_UNLOCK, unlockDoor);
        contentValues.put(TIMESTAMP, timestamp);

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.replace(DECISION_TABLE, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    void updateLockOrientation(String lockMAC, float orientation) {
        String updateQuery = "UPDATE " + LOCK_TABLE
                + " SET " + LOCK_ORIENTATION + " = '" + orientation + "', "
                + TIMESTAMP + " = '" + String.valueOf(System.currentTimeMillis()) + "' "
                + "WHERE " + LOCK_MAC + " = '" + lockMAC + "';";

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.execSQL(updateQuery);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    void updateGeofence(String lockMAC, String geofence, String size) {
        String updateQuery = "UPDATE " + LOCK_TABLE
                + " SET " + geofence + " = '" + size + "', "
                + TIMESTAMP + " = '" + String.valueOf(System.currentTimeMillis()) + "' "
                + "WHERE " + LOCK_MAC + " = '" + lockMAC + "';";

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.execSQL(updateQuery);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    void deleteLockData(String lock) {
        String lockQuery = "DELETE FROM " + LOCK_TABLE + " WHERE " + LOCK_MAC + "='" + lock + "';";
        String bluetoothQuery = "DELETE FROM " + BLUETOOTH_TABLE + " WHERE " + BLUETOOTH_NEARBY_LOCK + "='" + lock + "';";
        String wifiQuery = "DELETE FROM " + WIFI_TABLE + " WHERE " + WIFI_NEARBY_LOCK + "='" + lock + "';";

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.execSQL(lockQuery);
            database.execSQL(bluetoothQuery);
            database.execSQL(wifiQuery);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    // TODO ABC
    void insertSession(WindowData[] snapshot, boolean unlockDoor) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(SESSION_DOOR_UNLOCK, unlockDoor);

        try {
            database = databaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.replace(SESSION_TABLE, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        Cursor c = database.rawQuery("SELECT last_insert_rowid()", null);
        c.moveToFirst();
        int id = c.getInt(0);

        insertWindows(snapshot, id);
    }

    void insertWindows(WindowData[] snapshot, int id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WINDOW_SESSION_ID, id);

        for (WindowData window: snapshot) {
            contentValues.put(WINDOW_ACCELERATION_X, window.getAccelerationX());
            contentValues.put(WINDOW_ACCELERATION_Y, window.getAccelerationY());
            contentValues.put(WINDOW_SPEED_X, window.getSpeedX());
            contentValues.put(WINDOW_SPEED_Y, window.getSpeedY());
            contentValues.put(WINDOW_ORIENTATION, window.getOrientation());
            contentValues.put(WINDOW_VELOCITY, window.getVelocity());
            contentValues.put(WINDOW_ACCELERATION_MAG, window.getAccelerationMag());
            contentValues.put(TIMESTAMP, window.getTime());

            try {
                database = databaseHelper.getWritableDatabase();
                database.beginTransaction();
                database.replace(WINDOW_TABLE, null, contentValues);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    }

    int getSessionCount(boolean unlockDoor) {
        int cnt;
        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();
            String countQuery;
            if (unlockDoor) {
                countQuery = "SELECT * FROM " + SESSION_TABLE + " WHERE " + SESSION_DOOR_UNLOCK + ";";
            } else {
                countQuery = "SELECT * FROM " + SESSION_TABLE + " WHERE NOT(" + SESSION_DOOR_UNLOCK + ");";
            }
            Cursor cursor = database.rawQuery(countQuery, null);
            cnt = cursor.getCount();
            cursor.close();
        } finally {
            database.endTransaction();
        }
        return cnt;
    }

    ArrayList<SessionData> getSessions(boolean unlockDoor) {

        ArrayList<SessionData> clusters = new ArrayList<>();
        int cur_id = 0;
        int prev_id = 0;

        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();

                ArrayList<WindowData> session = new ArrayList<>();

                String sessionQuery;
                if (unlockDoor) {
                    sessionQuery = "SELECT * FROM " + WINDOW_TABLE
                            + " INNER JOIN " + SESSION_TABLE
                            + " ON " + WINDOW_SESSION_ID + "=" + SESSION_ID
                            + " WHERE " + SESSION_DOOR_UNLOCK + ";";
                } else {
                    sessionQuery = "SELECT * FROM " + WINDOW_TABLE
                            + " INNER JOIN " + SESSION_TABLE
                            + " ON " + WINDOW_SESSION_ID + "=" + SESSION_ID
                            + " WHERE NOT(" + SESSION_DOOR_UNLOCK + ");";
                }

                Cursor sessionCursor = database.rawQuery(sessionQuery, null);

                if (sessionCursor.moveToFirst()) {
                    do {
                        cur_id = sessionCursor.getInt(sessionCursor.getColumnIndex(WINDOW_SESSION_ID));
                        double accelerationX = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_ACCELERATION_X));
                        double accelerationY = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_ACCELERATION_Y));
                        double speedX = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_SPEED_X));
                        double speedY = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_SPEED_Y));
                        double orientation = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_ORIENTATION));
                        double velocity = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_VELOCITY));
                        double accelerationMag = sessionCursor.getDouble(sessionCursor.getColumnIndex(WINDOW_ACCELERATION_MAG));
                        double time = sessionCursor.getDouble(sessionCursor.getColumnIndex(TIMESTAMP));

                        if (cur_id != prev_id && prev_id != 0) {
                            SessionData cluster = new SessionData(prev_id, 0, session);
                            clusters.add(cluster);
                            session = new ArrayList<>();
                        }
                        session.add(new WindowData(accelerationX, accelerationY, speedX, speedY, orientation, velocity, accelerationMag, time));
                        prev_id = cur_id;
                    } while (sessionCursor.moveToNext());

                    // Add current session to cluster when queue is at its end
                    SessionData c = new SessionData(cur_id, 0, session);
                    clusters.add(c);
                }
                sessionCursor.close();
//            }
        } finally {
            database.endTransaction();
        }
        return clusters;
    }

    boolean isClustered(int id) {
        boolean clustered = false;
        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();

            String countQuery = "SELECT " + SESSION_CLUSTER + " FROM " + SESSION_TABLE + " WHERE " + SESSION_ID + "=" + id + ";";
            Cursor cursor = database.rawQuery(countQuery, null);

            if(cursor.moveToFirst()){
                if (cursor.getInt(0) != 0) {
                    clustered = true;
                }
            }
            cursor.close();
        } finally {
            database.endTransaction();
        }
        return clustered;
    }

    int getClusterId(int id) {
        int cluster_id = 0;
        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();

            String countQuery = "SELECT " + SESSION_CLUSTER + " FROM " + SESSION_TABLE + " WHERE " + SESSION_ID + "=" + id + ";";
            Cursor cursor = database.rawQuery(countQuery, null);

            if(cursor.moveToFirst()){
                if (cursor.getInt(0) != 0) {
                    cluster_id = cursor.getInt(0);
                }
            }
            cursor.close();
        } finally {
            database.endTransaction();
        }
        return cluster_id;
    }

    int getClusterCount() {
        int cnt;
        try {
            database = databaseHelper.getReadableDatabase();
            database.beginTransaction();

            String countQuery = "SELECT MAX(" + SESSION_CLUSTER + ") FROM " + SESSION_TABLE + ";";
            Cursor cursor = database.rawQuery(countQuery, null);
            cursor.moveToFirst();
            cnt = cursor.getInt(0);
            cursor.close();
        } finally {
            database.endTransaction();
        }
        return cnt;
    }

    void updateCluster(int cur_id, int next_id) {

        int clusterValue;

        if (isClustered(next_id) && getClusterId(next_id) != 0)  {
            clusterValue = getClusterId(next_id);
        }
        else {
            int ctn = getClusterCount() + 1;
            clusterValue = ctn;
        }

        ContentValues args = new ContentValues();
        args.put(SESSION_ID, cur_id);
        args.put(SESSION_CLUSTER, clusterValue);
        database.update(SESSION_TABLE, args, SESSION_ID + "=" + cur_id, null);

        ContentValues args2 = new ContentValues();
        args2.put(SESSION_ID, next_id);
        args2.put(SESSION_CLUSTER, clusterValue);
        database.update(SESSION_TABLE, args2, SESSION_ID + "=" + next_id, null);

    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createDatastore(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            // Production-quality upgrade code should modify the tables when
            // the database version changes instead of dropping the tables and
            // re-creating them.
            if (newVersion != DATABASE_VERSION) {
                Log.w("Datastore", "Database upgrade from old: " + oldVersion + " to: " +
                        newVersion);
                database = databaseHelper.getWritableDatabase();
                dropDatastore();
                createDatastore(database);
                database.close();
            }
        }

        private void createDatastore(SQLiteDatabase database) {
            database.execSQL("PRAGMA foreign_keys = ON;");
            database.execSQL("CREATE TABLE " + LOCK_TABLE + " ("
                    + LOCK_MAC + " TEXT PRIMARY KEY, "
                    + LOCK_PASSPHRASE + " TEXT, "
                    + LOCK_LATITUDE + " DOUBLE, "
                    + LOCK_LONGITUDE + " DOUBLE, "
                    + LOCK_INNER_GEOFENCE + " FLOAT, "
                    + LOCK_OUTER_GEOFENCE + " FLOAT, "
                    + LOCK_ORIENTATION + " FLOAT, "
                    + TIMESTAMP + " LONG)");

            database.execSQL("CREATE TABLE " + BLUETOOTH_TABLE + " ("
                    + BLUETOOTH_NAME + " TEXT, "
                    + BLUETOOTH_SOURCE + " TEXT, "
                    + BLUETOOTH_RSSI + " INTEGER, "
                    + BLUETOOTH_NEARBY_LOCK + " FOREIGNKEY REFERENCES " + LOCK_TABLE + "(" + LOCK_MAC + "), "
                    + TIMESTAMP + " LONG, "
                    + "PRIMARY KEY (" + BLUETOOTH_SOURCE + ", " + BLUETOOTH_NEARBY_LOCK + "))");

            database.execSQL("CREATE TABLE " + WIFI_TABLE + " ("
                    + WIFI_SSID + " TEXT, "
                    + WIFI_MAC + " TEXT, "
                    + WIFI_RSSI + " INTEGER, "
                    + WIFI_NEARBY_LOCK + " FOREIGNKEY REFERENCES " + LOCK_TABLE + "(" + LOCK_MAC + "), "
                    + TIMESTAMP + " LONG, "
                    + "PRIMARY KEY (" + WIFI_MAC + ", " + WIFI_NEARBY_LOCK + "))");

            database.execSQL("CREATE TABLE " + LOCATION_TABLE + " ("
                    + LOCATION_PROVIDER + " TEXT, "
                    + LOCATION_LATITUDE + " TEXT, "
                    + LOCATION_LONGITUDE + " TEXT, "
                    + LOCATION_ACCURACY + " TEXT, "
                    + LOCATION_DATETIME + " TEXT, "
                    + TIMESTAMP + " LONG)");

            database.execSQL("CREATE TABLE " + SESSION_TABLE + " ("
                    + SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SESSION_DOOR_UNLOCK + " BOOLEAN, "
                    + SESSION_CLUSTER + " INTEGER DEFAULT 0)");

            database.execSQL("CREATE TABLE " + WINDOW_TABLE + " ("
                    + WINDOW_SESSION_ID + " INTEGER,"
                    + WINDOW_ACCELERATION_X + " DOUBLE, "
                    + WINDOW_ACCELERATION_Y + " DOUBLE, "
                    + WINDOW_SPEED_X + " DOUBLE, "
                    + WINDOW_SPEED_Y + " DOUBLE, "
                    + WINDOW_ORIENTATION + " DOUBLE, "
                    + WINDOW_VELOCITY + " DOUBLE, "
                    + WINDOW_ACCELERATION_MAG + " DOUBLE, "
                    + TIMESTAMP + " LONG, "
                    + "FOREIGN KEY(" + WINDOW_SESSION_ID + ") REFERENCES " + SESSION_TABLE + "(" + SESSION_ID + "));");

            database.execSQL("CREATE TABLE " + DECISION_TABLE + " ("
                    + DECISION_DECISION + " INTEGER, "
                    + DECISION_DOOR_UNLOCK + " BOOLEAN, "
                    + TIMESTAMP + " LONG)");
        }


        private void dropDatastore() {
            database.execSQL("DROP TABLE IF EXISTS " + LOCK_TABLE);
            database.execSQL("DROP TABLE IF EXISTS " + BLUETOOTH_TABLE);
            database.execSQL("DROP TABLE IF EXISTS " + WIFI_TABLE);
            database.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
            database.execSQL("DROP TABLE IF EXISTS " + DECISION_TABLE);
            database.execSQL("DROP TABLE IF EXISTS " + WINDOW_TABLE);
            database.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE);
        }
    }
}
