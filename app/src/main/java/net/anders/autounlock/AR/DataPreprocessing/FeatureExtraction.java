package net.anders.autounlock.AR.DataPreprocessing;

import net.anders.autounlock.AR.DataSegmentation.CoordinateData;
import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 15-02-2017.
 */

public class FeatureExtraction {

    /*
    As the three activities we are considering are mostly composed by different postures
    (sitting, standing, lying), we use two features from each sensor to represent
    the data.

    The first feature metric is a binary variable, with 1 indicating moving status
    and 0 indicating static status. It is equal to 1 when the standard deviation of acceleration data
    within a moving window is greater than a threshold and equal to 0 when the
    standard deviation is less than a threshold. The threshold is determined based on our experiments and is set
    to 0.3g in our current system

     The second feature is the inclination angle
    of the Y axis of the sensor, which indicates the orientation
    of the torso and leg. This feature can be used to distinguish
    three main postural conditions, standing, sitting, and lying
    /*

    /*
    Activity Recognition of Construction Tasks via Hidden Markov Models
    http://www2.isye.gatech.edu/~aozlu3/Project2_paper.pdf

    Mean, standard deviations, energy, entropy, mean value of Minmax sums
    and correlation are the most commonly extracted features.
    Feature extraction is usually performed through both overlapping and non-overlapping sliding windows.
    Sliding window is a common and successful approach in
    HAR, especially due to its role in recognizing some pattern
    in the data that is over some time interval [7]. We extracted
    features using time windows of 10 samples from each
    measurement, which correspond to 0.1 seconds of
    accelerometer data, with both non-overlapping windows
    and 50% of overlapping between windows. From each time
    window, 2 features were extracted: mean and standard
    deviation of x, y, and z-axis accelerometer readings. These
    two features were chosen because (1) axis values
    demonstrated different mean values noticeable for each
    activity, and (2) their ease of computation. Figure 3 shows
    how the mean x values in overlapping sliding windows
    changes for each action.

     */


    /*
    standard deviation
    minimum
    maximum

    Mean acc
    std
    min
    max
    energy
    correlation coefficents


    mean
    standard deviation, std
    correlation
    range
    root mean square
    Signal-Magnitude Area
    Autoregressive Coefficients (AC)
    Binned
    Distribution
    */

    // magnitude of the acceleration
    // double accTot = Math.sqrt(x*x + y*y + z*z);

    //static float magTot;
    static float meanTot;
    static double rmsTot;
    static double stdTot;

    public static void getFeatures(List<AccelerometerData> window) {

        /*float mean = 0;
        double rms = 0;
        float speed_x = 0;
        float speed_y = 0;
        float ori = 0;

        double c_x = 0;
        double c_y = 0;
        double prevOri = 0;*/

        long currentTime = System.currentTimeMillis();
        float meanAccX, sumAccX = 0;
        float meanAccY, sumAccY = 0;


        for (AccelerometerData acc : window) {
            sumAccX += acc.getAccelerationX();
            sumAccY += acc.getAccelerationY();
        }

        meanAccX = sumAccX / window.size();
        meanAccY = sumAccY / window.size();

        CoreService.windows.add(new AccelerometerData(meanAccX, meanAccY, 0, 0, 0, 0, "", currentTime, 0));

       /* for (AccelerometerData acc : window) {

            // Mean of Accelerometer X-,Y-,Z- values
//            mean += (acc.getAccelerationX() + acc.getAccelerationY() + acc.getAccelerationZ()) / 3;
            mean += (acc.getAccelerationX() + acc.getAccelerationY()) / 2;

            // Mean of resultant accelerometer values (magnitude of the acceleration)
            // TODO Måske bør der være filter på inden at rms findes?
            // rms er ikke korrekt? rms her er magnitude og rms skal findes efterfølgende

            // TODO Mean, Min, Max, Std, RMS, Diff, Bin, Cross
            // http://tuprints.ulb.tu-darmstadt.de/3014/4/20120620_Dissertation_Nickel_final.pdf
            rms += Math.sqrt(acc.getAccelerationX()*acc.getAccelerationX() + acc.getAccelerationY()*acc.getAccelerationY() + acc.getAccelerationZ()*acc.getAccelerationZ());

            speed_x += acc.getSpeedX();
            speed_y += acc.getSpeedY();
            ori += acc.getOrientation();
        }

        float speed_x_mean = speed_x/window.size();
        float speed_y_mean = speed_y/window.size();
        float ori_mean = ori/window.size();

        //c_x = Math.abs(speed_x_mean)*Math.cos(ori_mean);
        //c_y = Math.abs(speed_y_mean)*Math.sin(ori_mean);

        CoordinateData coordinate = new CoordinateData(speed_x_mean, speed_y_mean,ori_mean);

        AccelerometerData temp = new AccelerometerData(0, 0, 0, 0, 0, 0, "be", 0, 0);
        temp.setSpeedX(speed_x_mean);
        temp.setSpeedY(speed_y_mean);
        temp.setOrientation(ori/window.size());

        meanTot = mean/window.size();
        rmsTot = rms/window.size();
        stdTot = getStdDev(window);

        //CoreService.windowAvg.add(meanTot);
        //CoreService.windowRms.add(rmsTot);
        //CoreService.windowStd.add(stdTot);
        CoreService.windowAcc.add(temp);
        CoreService.windowCoor.add(coordinate);
*/
        //TODO AVC + Angle
    }

    static double getStdDev(List<AccelerometerData> list)
    {
        return Math.sqrt(getVariance(list));
    }

    static double getVariance(List<AccelerometerData> list) {
        double mean = meanTot;
        double temp = 0;
        float mag = 0;

        for(AccelerometerData acc : list)
            mag = (acc.getAccelerationX() + acc.getAccelerationZ() + acc.getAccelerationY()) / 3;

        temp += (mag-mean)*(mag-mean);
        return temp/list.size();
    }

    // ACCELEROMETER DATA PREPARATION FOR ACTIVITY RECOGNITION
    // http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=0A7C9D550ABF74EAA6C2964BE7A0CABA?doi=10.1.1.724.7693&rep=rep1&type=pdf
    // AVC + angle φx
}
