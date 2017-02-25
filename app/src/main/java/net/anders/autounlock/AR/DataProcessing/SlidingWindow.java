package net.anders.autounlock.AR.DataProcessing;

import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 21-02-2017.
 */

public class SlidingWindow {

     /*
    Ready-to-Use Activity Recognition for Smartphones
    file:///C:/Users/Anders/Downloads/06597218.pdf

    Sliding window: Activity recognition was done using a sliding window technique.
    The signals from the sensors were divided into equal-sized smaller sequences, also called
    windows. From these windows, features were extracted and finally the classification
    of the sequences was done using models trained based on these features.

    In this study, the windows were of the length of 300 observations, which is
    7.5 seconds, because the sampling frequency was 40Hz. In offline recognition,
    the slide between two sequential windows was 75 observations,
    while in online recognition, the slide was set to 150 observations to load the processor less

    Moreover, to reduce the number of misclassified windows, the final classification was done
    based on the majority voting of the classification results of three adjacent windows.
    Therefore, when an activity changes, a new activity can be detected when two adjacent
    windows are classified as a new activity. For instance, if the slide is 150 observations,
    a new activity can be detected after 450 observations, which is around eleven seconds
    if the sampling rate is 40Hz
    */

    /*
    Layered hidden Markov models to recognize activity with built-in sensors on Android smartphone:
    The smaller window size is not effective to consider certain long-term activities and the
    larger window size may include noises since multiple activities could exist.

    */


    /*
    3 windows:
        -sliding windows
        -event-defined windows
        -activity-defined windows
    The sliding window approach does not require pre-processing of the sensor signal
    and is therefore ideally suited to real-time applications
    */

    public static List<List> insertAccelerometerIntoWindow(AccelerometerData accelerometerData) {

        CoreService.window.add(accelerometerData);

        if (CoreService.window.size() >= 100) {
            //CoreService.windowCircleBuffer.add(CoreService.window);

            List<List> list = new ArrayList<>();
            list.add(CoreService.window);
            CoreService.window.clear();
            return list;
        }
        return null;
    }
}
