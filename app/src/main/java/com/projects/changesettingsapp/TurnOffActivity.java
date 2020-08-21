package com.projects.changesettingsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TurnOffActivity extends AppCompatActivity {

    Button mTurnOffButton;
    TimePicker mTimePicker;
    Button mCancelButton;

    private static final int REQUEST_SERVICE_IS_STOPPED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn_off);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTimePicker = findViewById(R.id.time_picker);
        mTimePicker.setIs24HourView(true);

        mTurnOffButton = findViewById(R.id.turn_off_button);
        mTurnOffButton.setVisibility(View.VISIBLE);
        mTurnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                long msTimer = getSecondsAmountBetweenTimes(Integer.parseInt
                                (new SimpleDateFormat("HH").format(calendar.getTime())),
                        Integer.parseInt(new SimpleDateFormat("mm").format(calendar.getTime())),
                        Integer.parseInt(new SimpleDateFormat("ss").format(calendar.getTime())),
                        mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute(), 0)
                        * 1000;

                String currentTime = new SimpleDateFormat("HH:mm:ss").format(calendar.getTime());

                StringBuilder timerTimeBuilder = new StringBuilder();
                if (mTimePicker.getCurrentHour() < 10) timerTimeBuilder.append("0");
                timerTimeBuilder.append(mTimePicker.getCurrentHour()).append(":");
                if (mTimePicker.getCurrentMinute() < 10) timerTimeBuilder.append("0");
                String timerTime = timerTimeBuilder.append(mTimePicker.getCurrentMinute())
                        .append(":00").toString();

                startService(TurnOffService.newIntent(TurnOffActivity.this,
                        msTimer, currentTime, timerTime,
                        createPendingResult(REQUEST_SERVICE_IS_STOPPED, new Intent(), 0)));

                mTurnOffButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
            }
        });

        mCancelButton = findViewById(R.id.cancel_button);
        mCancelButton.setVisibility(View.GONE);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(TurnOffActivity.this, TurnOffService.class));

                mTurnOffButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
            }
        });
    }

    long getSecondsAmountBetweenTimes(int currentHours, int currentMinutes, int currentSeconds,
                                 int nextHours, int nextMinutes, int nextSeconds) {
        long secondsInDay = 24*3600;
        long currentSecondsSum = (currentHours * 60 + currentMinutes) * 60 + currentSeconds;
        long nextSecondsSum = (nextHours * 60 + nextMinutes) * 60 + nextSeconds;
        long difference = nextSecondsSum - currentSecondsSum;
        if (difference < 0) {
            return difference + secondsInDay;
        }
        return difference;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_SERVICE_IS_STOPPED) {
            boolean isStopped = data
                    .getBooleanExtra(TurnOffService.EXTRA_IS_STOPPED, false);
            if (isStopped) {
                mTurnOffButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
            }
        }
    }
}
