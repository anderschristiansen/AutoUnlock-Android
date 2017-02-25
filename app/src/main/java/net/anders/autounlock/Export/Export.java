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
            writeCsvHeader(activity.toString(), "x","y","z", "time");

            for (AccelerometerData acc: calibrationAccelerometer) {
                writeCsvData(activity.toString(), acc.getAccelerationX(), acc.getAccelerationY(), acc.getAccelerationZ(), acc.getTime());
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

    private static void writeCsvHeader(String h1, String h2, String h3, String h4, String h5) throws IOException {
        String line = String.format("%s;%s;%s;%s;%s\n", h1,h2,h3,h4,h5);
        writer.write(line);
    }

    private static void writeCsvData(String d, float e, float f, float g, float h) throws IOException {
        String line = String.format("%s;%f;%f;%f;%f\n", d, e, f, g, h);
        writer.write(line);
    }

    private static String constructCalibrationName(String activity) {
        File file=new File("/sdcard/AutoUnlock");
        File[] list = file.listFiles();
        return "AutoUnlock-" + activity + "-" + list.length + ".csv";
    }



    public static void CsvMean(List<Float> mean) throws IOException {

        String activity = "mean";
        String type = "float";

        File root = Environment.getExternalStorageDirectory();
        File gpxfile = new File(root, activity + ".csv");

        try {
            writer = new FileWriter(gpxfile);
            writeFeatureCsvHeader("type", "avg");

            for (Float f: mean) {
                writeMeanCsvData(type.toString(), f);
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

    private static void writeMeanCsvData(String d, float e) throws IOException {
        String line = String.format("%s;%f\n", d, e);
        writer.write(line);
    }

    private static void writeFeatureCsvHeader(String h1, String h2) throws IOException {
        String line = String.format("%s;%s\n", h1,h2);
        writer.write(line);
    }


    //55555555555

    public static void CsvRms(List<Double> rms) throws IOException {

        String activity = "rms";
        String type = "double";

        File root = Environment.getExternalStorageDirectory();
        File gpxfile = new File(root, activity + ".csv");

        try {
            writer = new FileWriter(gpxfile);
            writeFeatureCsvHeader("type", "avg");

            for (Double d: rms) {
                writeDoubleCsvData(type.toString(), d);
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

    private static void writeDoubleCsvData(String d, double e) throws IOException {
        String line = String.format("%s;%f\n", d, e);
        writer.write(line);
    }

    public static void CsvStd(List<Double> std) throws IOException {

        String activity = "std";
        String type = "double";

        File root = Environment.getExternalStorageDirectory();
        File gpxfile = new File(root, activity + ".csv");

        try {
            writer = new FileWriter(gpxfile);
            writeFeatureCsvHeader("type", "avg");

            for (Double d: std) {
                writeDoubleCsvData(type.toString(), d);
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


}
