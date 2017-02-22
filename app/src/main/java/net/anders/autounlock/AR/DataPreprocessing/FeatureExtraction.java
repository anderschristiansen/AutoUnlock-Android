package net.anders.autounlock.AR.DataPreprocessing;

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


}
