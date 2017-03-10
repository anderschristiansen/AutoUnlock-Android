package net.anders.autounlock.ML;

import net.anders.autounlock.ML.DataProcessing.Feature;
import net.anders.autounlock.ML.DataSegmentation.WindowData;
import net.anders.autounlock.RingProcessorService;

import java.io.IOException;

/**
 * Created by Anders on 09-03-2017.
 */

public class RecordUnlock {

    public static void ProcessManualUnlock()  {
        WindowData[] snapshot = RingProcessorService.getSnapshot();

        try {
            Feature.getFeatures(snapshot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
