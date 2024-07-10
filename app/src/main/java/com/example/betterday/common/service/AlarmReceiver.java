package com.example.betterday.common.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.betterday.MainActivity;
import com.example.betterday.R;

public class AlarmReceiver extends BroadcastReceiver {

    private static Ringtone ringtone;
    private static Vibrator vibrator;
    private static Handler handler;
    private static Runnable ringtoneRunnable;
    private static final int ALARM_DURATION = 60000; // 1 minutes
    private static final int RINGTONE_INTERVAL = 30000; // 30 seconds
    private static boolean isRunning = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        handler = new Handler();

        String itemName = intent.getStringExtra("item_name");

        Toast.makeText(context, "Alarm for item: " + itemName, Toast.LENGTH_SHORT).show();

        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(context, notificationUri);
        if (ringtone == null) {
            notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(context, notificationUri);
        }

        if (ringtone != null) {
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
            startRingtone();
        }

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
            }
        }

        showNotification(context, itemName);

        handler.postDelayed(() -> {
            stopRingtoneAndVibration(context);
            if (handler != null) {
                handler.removeCallbacks(ringtoneRunnable);
            }
        }, ALARM_DURATION);
    }

    private void startRingtone() {
        ringtoneRunnable = new Runnable() {
            @Override
            public void run() {
                if (ringtone != null && !isRunning) {
                    ringtone.play();
                    handler.postDelayed(this, RINGTONE_INTERVAL);
                }
            }
        };
        handler.post(ringtoneRunnable);
    }

    public static void stopRingtoneAndVibration(Context context) {
        isRunning = false;
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (handler != null && ringtoneRunnable != null) {
            handler.removeCallbacks(ringtoneRunnable);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void showNotification(Context context, String itemName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "alarm_channel";
        String channelName = "Alarm Notification";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }

        Intent stopIntent = new Intent(context, StopAlarmReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        openAppIntent.putExtra("stop_alarm", true);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_calendar_inactive)
                .setContentTitle("Alarm")
                .setContentText("Time for: " + itemName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Remove notification on "Stop"
                .setOngoing(true)
                .addAction(R.drawable.ic_remove_icon_dark, "Stop", stopPendingIntent)
                .setContentIntent(openAppPendingIntent);

        notificationManager.notify(0, notificationBuilder.build());

        isRunning = true;
    }

    public static class StopAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopRingtoneAndVibration(context);
        }
    }
}
