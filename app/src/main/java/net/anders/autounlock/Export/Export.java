package net.anders.autounlock.Export;

import android.os.Environment;
import android.util.Log;

import net.anders.autounlock.AccelerometerData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by Anders on 15-02-2017.
 */

public class Export {

    static FileWriter writer;

    public static void Database() {
        try {
            File data = Environment.getDataDirectory();

            try {
                String datastorePath = "//data//net.anders.autounlock//databases//datastore.db";
                //String exportPath = String.valueOf(System.currentTimeMillis()) + ".db";
                String exportPath = constructDbName();

                File outputDirectory = new File("/sdcard/AutoUnlock/");
                outputDirectory.mkdirs();

                File datastore = new File(data, datastorePath);
                File export = new File(outputDirectory, exportPath);

                FileChannel source = new FileInputStream(datastore).getChannel();
                FileChannel destination = new FileOutputStream(export).getChannel();

                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();

                Log.v("Export Datastore", "Datastore exported to " + exportPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // do something
        }
    }

    private static String constructDbName() {
        File file=new File("/sdcard/AutoUnlock");
        File[] list = file.listFiles();
        return "AutoUnlock-" + list.length + ".db";
    }

    public static void Csv(List<AccelerometerData> calibrationAccelerometer, String activity) throws IOException {

        File root = Environment.getExternalStorageDirectory();
        File gpxfile = new File(root, activity + ".csv");

        try {
            writer = new FileWriter(gpxfile);
            writeCsvHeader(activity.toString(), "x","y","z");

            for (AccelerometerData acc: calibrationAccelerometer) {
                writeCsvData(activity.toString(), acc.getAccelerationX(), acc.getAccelerationY(), acc.getAccelerationZ());
            }

            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String exportPath = constructCalibrationName(activity);

        File outputDirectory = new File("/sdcard/AutoUnlock/");
        outputDirectory.mkdirs();

        File export = new File(outputDirectory, exportPath);

        FileChannel source = new FileInputStream(gpxfile).getChannel();
        FileChannel destination = new FileOutputStream(export).getChannel();

        destination.transferFrom(source, 0, source.size());
        source.close();
        destination.close();
    }

    private static void writeCsvHeader(String h1, String h2, String h3, String h4) throws IOException {
        String line = String.format("%s;%s;%s;%s\n", h1,h2,h3,h4);
        writer.write(line);
    }

    private static void writeCsvData(String d, float e, float f, float g) throws IOException {
        String line = String.format("%s;%f;%f;%f\n", d, e, f, g);
        writer.write(line);
    }

    private static String constructCalibrationName(String activity) {
        File file=new File("/sdcard/AutoUnlock");
        File[] list = file.listFiles();
        return "AutoUnlock-" + activity + "-" + list.length + ".csv";
    }
}
