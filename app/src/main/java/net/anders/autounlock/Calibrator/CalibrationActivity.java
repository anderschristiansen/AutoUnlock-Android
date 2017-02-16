package net.anders.autounlock.Calibrator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.anders.autounlock.AccelerometerData;
import net.anders.autounlock.CoreService;
import net.anders.autounlock.Export.Export;
import net.anders.autounlock.MainActivity;
import net.anders.autounlock.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalibrationActivity extends Activity{

    private List<AccelerometerData> cali;
    private Button startCalibration;
    private EditText activityText;
    private TextView cTimerView;
    private CountDownTimer cTimer = null;
    private long startTime;
    private long endTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        startCalibration = (Button) findViewById(R.id.startCalibration);
        activityText = (EditText) findViewById(R.id.activity);
        cTimerView = (TextView) findViewById(R.id.cTimer);

    }

    public void onButtonClickStartCalibration(View v) {
        startCalibration.setEnabled(false);
        activityText.setEnabled(false);
        startCountDownTimer(120000);
        //startCountDownTimer(100);
    }

    //start timer function
    void startCountDownTimer(long millis) {
        startTime = System.currentTimeMillis();
        cTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                cTimerView.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                endTime = System.currentTimeMillis();
                cTimerView.setText("");
                startCalibration.setEnabled(true);
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

    // Send
    void ExtractCalibrationData(String activity) throws IOException {
        List<AccelerometerData> calibrationAccelerometer = new ArrayList<AccelerometerData>();
        for (AccelerometerData acc:CoreService.recordedAccelerometer) {
            if (acc.getTime() > startTime && acc.getTime() < endTime) {
                calibrationAccelerometer.add(acc);
            }
        }
        Export.Csv(calibrationAccelerometer, activity);
    }
}
