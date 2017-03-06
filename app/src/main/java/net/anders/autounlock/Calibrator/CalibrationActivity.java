package net.anders.autounlock.Calibrator;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.data.DataBuffer;

import net.anders.autounlock.AR.DataSegmentation.CoordinateData;
import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.Export.Export;
import net.anders.autounlock.R;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CalibrationActivity extends Activity{

    private List<AccelerometerData> cali;
    private Button startCalibration;
    private Button startFullCalibration;
    private EditText activityText;
    private TextView cTimerView;
    private CountDownTimer cTimer = null;
    private long startTime;
    private long endTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        startCalibration = (Button) findViewById(R.id.startCalibration);
        startFullCalibration = (Button) findViewById(R.id.startFullCalibration);
        activityText = (EditText) findViewById(R.id.activity);
        cTimerView = (TextView) findViewById(R.id.cTimer);
    }

    public void onButtonClickStartCalibration(View v) {
        startCalibration.setEnabled(false);
        startFullCalibration.setEnabled(false);
        activityText.setEnabled(false);

        CoreService.window.clear();
        CoreService.windows.clear();

        startCountDownTimer(10000);
    }

    public void onButtonClickStartFullCalibration(View v) {
        startCalibration.setEnabled(false);
        startFullCalibration.setEnabled(false);
        activityText.setEnabled(false);

        // Walk -> Run -> Walk -> Stand -> Walk -> Sit
        startCountDownTimer(120000 * 6);
    }


    //start timer function
    void startCountDownTimer(long millis) {

        startTime = System.currentTimeMillis();
        cTimer = new CountDownTimer(millis, 1000) {

            public void onTick(long millisUntilFinished) {

                long timeLeft = millisUntilFinished / 1000;
                cTimerView.setText(String.valueOf(timeLeft));

                if (timeLeft == 0 || timeLeft == 120 || timeLeft == 240 || timeLeft == 360 || timeLeft == 480 || timeLeft == 600) {
                    notificationSound();
                }
            }

            public void onFinish() {
                endTime = System.currentTimeMillis();
                cTimerView.setText("");
                startCalibration.setEnabled(true);
                startFullCalibration.setEnabled(true);
                activityText.setEnabled(true);
                try {
                    ExtractCalibrationData(activityText.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    private void notificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send
    void ExtractCalibrationData(String activity) throws IOException {

//        List<AccelerometerData> calibrationAccelerometer = new ArrayList<AccelerometerData>();
//        for (AccelerometerData acc:CoreService.recordedAccelerometer) {
//            if (acc.getTime() > startTime && acc.getTime() < endTime) {
//                calibrationAccelerometer.add(acc);
//            }
//        }

//        List<AccelerometerData> calibrationAccelerometer = new ArrayList<AccelerometerData>();
//        for (AccelerometerData acc : CoreService.windowAcc) {
//            if (acc.getTime() > startTime && acc.getTime() < endTime) {
//                calibrationAccelerometer.add(acc);
//            }
//        }

        //Collections.reverse(CoreService.windowAcc);

        //Export.CsvRawAcc(calibrationAccelerometer, activity);
        //Export.CsvMean(CoreService.windowAvg);
        //Export.CsvRms(CoreService.windowRms);
        //Export.CsvStd(CoreService.windowStd);
        Export.CsvWindows(CoreService.windows);
        //Export.CsvCoord(makeCumul(CoreService.windowCoor));

        //CoreService.windowCoor.clear();
        //CoreService.windowAcc.clear();
    }

    public static List<CoordinateData> makeCumul(List<CoordinateData> list) {

        List<CoordinateData> cumulList = new ArrayList<>();

        float sum_x = 0;
        float sum_y = 0;
        float prevOri = 0;

        for (CoordinateData coor : list) {
            float x = coor.getX();
            float y = coor.getY();
            float ori = coor.getOri();

            float x2 = Math.abs(x)*(float)Math.cos(ori-prevOri);
            float y2 = Math.abs(y)*(float)Math.sin(ori-prevOri);

            CoordinateData cumulData = new CoordinateData(x2 + sum_x, y2 + sum_y, ori);
            cumulList.add(cumulData);

            sum_x = sum_x + x2;
            sum_y = sum_y + y2;
            prevOri = ori;
        }
        return cumulList;
    }
}

//c_x = Math.abs(speed_x_mean)*Math.cos(ori_mean);
//c_y = Math.abs(speed_y_mean)*Math.sin(ori_mean);