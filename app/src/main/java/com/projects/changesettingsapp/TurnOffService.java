package com.projects.changesettingsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class TurnOffService extends Service {

    private Timer mTimer;
    private DisableBluetoothTimerTask mDisableBluetoothTimerTask;
    private long mDelay;

    private static final String DELAY =
            "com.projects.change_settings_app.delay";
    private static final String CURRENT_TIME =
            "com.projects.change_settings_app.current_time";
    private static final String TIMER_TIME =
            "com.projects.change_settings_app.timer_time";
    private static final String CHANNEL_ID = "turn_off_service_channel_id";
    private static final int NOTIFICATION_ID = 1;

    public TurnOffService() {
    }

    public static Intent newIntent (Context packageContext, long msTimer,
                                    String currentTime, String timerTime) {

        Intent intent = new Intent(packageContext, TurnOffService.class);
        intent.putExtra(DELAY, msTimer);
        intent.putExtra(CURRENT_TIME, currentTime);
        intent.putExtra(TIMER_TIME, timerTime);

        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mDisableBluetoothTimerTask = new DisableBluetoothTimerTask();
        mDelay = intent.getLongExtra(DELAY, 5000);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(intent.getStringExtra(CURRENT_TIME),
                intent.getStringExtra(TIMER_TIME)));

        mTimer.schedule(mDisableBluetoothTimerTask, mDelay);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
        mDisableBluetoothTimerTask = null;
        super.onDestroy();
    }

    class DisableBluetoothTimerTask extends TimerTask {

        @Override
        public void run() {
            //Disable bluetooth
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
            stopSelf();
        }
    }

    private Notification createNotification(String currentTime, String timerTime) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.time_icon)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(String.format
                        (getResources().getString(R.string.short_notification_text), timerTime))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format
                                (getResources().getString(R.string.long_notification_text),
                                        currentTime, mDelay / 1000, timerTime)))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setShowWhen(true)
                .setColor(getResources().getColor(R.color.colorAccent));

        return builder.build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getResources().getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getResources().getString(R.string.notification_channel_description));
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
