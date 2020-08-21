package com.projects.changesettingsapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

    private PendingIntent mPendingIntent;

    private static final String EXTRA_DELAY =
            "com.projects.change_settings_app.delay";
    private static final String EXTRA_CURRENT_TIME =
            "com.projects.change_settings_app.current_time";
    private static final String EXTRA_TIMER_TIME =
            "com.projects.change_settings_app.timer_time";
    private static final String CHANNEL_ID = "turn_off_service_channel_id";
    private static final int NOTIFICATION_ID = 1;

    private static final String EXTRA_PENDING_INTENT = "com.projects.change_settings_app.pending_intent";
    public static final String EXTRA_IS_STOPPED = "is_stopped";

    public TurnOffService() {
    }

    public static Intent newIntent (Context packageContext, long msTimer, String currentTime,
                                    String timerTime, PendingIntent pendingIntent) {

        Intent intent = new Intent(packageContext, TurnOffService.class);
        intent.putExtra(EXTRA_DELAY, msTimer);
        intent.putExtra(EXTRA_CURRENT_TIME, currentTime);
        intent.putExtra(EXTRA_TIMER_TIME, timerTime);

        intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent);

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
        mDelay = intent.getLongExtra(EXTRA_DELAY, 5000);

        mPendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(intent.getStringExtra(EXTRA_CURRENT_TIME),
                intent.getStringExtra(EXTRA_TIMER_TIME)));

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

        sendInfoToActivity();

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

        int allSeconds = (int) mDelay / 1000;
        int hours = allSeconds / 3600;
        int minutes = allSeconds / 60 - hours * 60;
        int seconds = allSeconds - hours * 3600 - minutes * 60;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.time_icon)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(String.format
                        (getResources().getString(R.string.short_notification_text),
                                timerTime.substring(0, timerTime.length() - 3)))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format
                                (getResources().getString(R.string.long_notification_text),
                                        currentTime, hours, minutes, seconds, timerTime)))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setShowWhen(true)
                .setColor(getResources().getColor(R.color.colorAccent));

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getResources().getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getResources().getString(R.string.notification_channel_description));
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendInfoToActivity() {
        try {
            Intent intent = new Intent().putExtra(EXTRA_IS_STOPPED, true);
            mPendingIntent.send(TurnOffService.this,
                    Activity.RESULT_OK, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
