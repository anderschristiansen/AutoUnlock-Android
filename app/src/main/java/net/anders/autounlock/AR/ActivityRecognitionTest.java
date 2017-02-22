package net.anders.autounlock.AR;

import com.google.android.gms.common.data.DataBuffer;

import net.anders.autounlock.AR.DataSegmentation.Segment;
import net.anders.autounlock.AR.DataSegmentation.Window;
import net.anders.autounlock.AR.SignalProcessing.Preprocessing;
import net.anders.autounlock.AR.SignalProcessing.Smoothing;
import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.AR.DataProcessing.AccelerometerFilter;
import net.anders.autounlock.CoreService;

import java.util.List;

/**
 * Created by Anders on 21-02-2017.
 */

public class ActivityRecognitionTest {

    List<Segment> segments;

    public static void gatherWindows (AccelerometerData accelerometerData) {

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
