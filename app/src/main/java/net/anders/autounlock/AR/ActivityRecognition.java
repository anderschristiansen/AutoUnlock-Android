package net.anders.autounlock.AR;

import net.anders.autounlock.AR.DataPreprocessing.FeatureExtraction;
import net.anders.autounlock.AR.DataProcessing.SlidingWindow;
import net.anders.autounlock.AR.DataSegmentation.Segment;
import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.Export.Export;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 21-02-2017.
 */

public class ActivityRecognition {

    List<Segment> segments;

    public static void accelerometerEvent (AccelerometerData accelerometerData) {

        // If sliding window is full it is added to the circle buffer
        // List<List> list = SlidingWindow.insertAccelerometerIntoWindow(accelerometerData);

        CoreService.window.add(accelerometerData);

        if (CoreService.window.size() >= 100) {

            //CoreService.windowCircleBuffer.add(CoreService.window);

            FeatureExtraction.getFeatures(CoreService.window);
            CoreService.window.clear();
        }

        // Export.Csv(list);


        //Smoothing.apply5PointSmoothing(accelerometerData);

//        DataBuffer circularBuffer = new DataBuffer<>();
//        segments.add();
//
//        Window w = new Window(10, );

        //AccelerometerFilter.NoiseFilter(accelerometerData);

        //Preprocessing.Preprocess(accelerometerData);

        //Noise Reduction

        //Linearization

        //Smoothing

    }

}
