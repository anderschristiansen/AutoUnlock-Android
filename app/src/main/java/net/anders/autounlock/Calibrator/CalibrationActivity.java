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

import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.Export.Export;
import net.anders.autounlock.R;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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

//        for (List<AccelerometerData> e: CoreService.windowCircleBuffer.getAll()) {
//            for (AccelerometerData acc: e) {
//                if (acc.getTime() > startTime && acc.getTime() < endTime) {
//                    //calibrationAccelerometer.add(acc);
//                }
//            }
//        }

        List<AccelerometerData> calibrationAccelerometer = new ArrayList<AccelerometerData>();
        for (AccelerometerData acc:CoreService.recordedAccelerometer) {
            if (acc.getTime() > startTime && acc.getTime() < endTime) {
                calibrationAccelerometer.add(acc);
            }
        }
        Export.Csv(calibrationAccelerometer, activity);
        Export.CsvMean(CoreService.windowAvg);
        Export.CsvRms(CoreService.windowRms);
        Export.CsvStd(CoreService.windowStd);
    }
}
