package com.example.betterday.common.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.example.betterday.MainActivity;
import com.example.betterday.R;
import com.example.betterday.common.fileio.JsonUtil;
import com.example.betterday.common.model.Reminder;

import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {

    private static final int ALARM_DURATION = 60000; // 1 minute
    private static final int RINGTONE_INTERVAL = 30000; // 30 seconds
    private static final String CHANNEL_ID = "alarm_channel";

    private static Ringtone ringtone;
    private static Vibrator vibrator;
    private static Handler handler;
    private static Runnable ringtoneRunnable;
    private boolean isRunning = false;
    private static boolean stopAlarm = false;

    private ArrayList<Reminder> remindersList;

    @SuppressLint("InflateParams")
    @Override
    public void onReceive(Context context, Intent intent) {
        initHandler();
        remindersList = JsonUtil.readFromJson(context);
        if (remindersList == null) {
            remindersList = new ArrayList<>();
        }
        String itemName = intent.getStringExtra("item_name");
        int uniqueId = intent.getIntExtra("unique_id", 0);
        int itemId = intent.getIntExtra("item_id", -1);
        Toast.makeText(context, "Alarm for item: " + itemName, Toast.LENGTH_SHORT).show();

        startRingtone(context);
        startVibration(context);
        showNotification(context, itemName, uniqueId);
        handler.postDelayed(() -> handleAlarmTimeout(context, itemName, uniqueId), ALARM_DURATION);
        Intent broadcastIntent = new Intent("com.example.betterday.ALARM_EVENT");
        broadcastIntent.putExtra("alarmId", uniqueId);
        context.sendBroadcast(broadcastIntent);
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread("AlarmHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void startRingtone(Context context) {
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(context, notificationUri);

        if (ringtone == null) {
            notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(context, notificationUri);
        }

        if (ringtone != null) {
            startRingtoneLoop();
        }
    }

    private void startRingtoneLoop() {
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

    private void startVibration(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
            }
        }
    }

    private void handleAlarmTimeout(Context context, String itemName, int uniqueId) {
        if (!stopAlarm) {
            stopRingtoneAndVibration(context, uniqueId);
            showMissedNotification(context, itemName, uniqueId);
        }
    }

    public static void stopRingtoneAndVibration(Context context, int uniqueId) {
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
        notificationManager.cancel(uniqueId);
    }

    private void showNotification(Context context, String itemName, int uniqueId) {
        stopAlarm = false;
        createNotificationChannel(context);

        PendingIntent stopPendingIntent = createPendingIntent(context, StopAlarmReceiver.class, uniqueId);
        PendingIntent openAppPendingIntent = createOpenAppPendingIntent(context, uniqueId);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar_inactive)
                .setContentTitle("Alarm")
                .setContentText("Time for: " + itemName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_remove_icon_dark, "Stop", stopPendingIntent)
                .setContentIntent(openAppPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(uniqueId, notificationBuilder.build());
        isRunning = true;
    }

    private void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Notification", NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createPendingIntent(Context context, Class<?> cls, int uniqueId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("unique_id", uniqueId);
        return PendingIntent.getBroadcast(context, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent createOpenAppPendingIntent(Context context, int uniqueId) {
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        openAppIntent.putExtra("stop_alarm", true);
        openAppIntent.putExtra("unique_id", uniqueId);
        return PendingIntent.getActivity(context, uniqueId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void showMissedNotification(Context context, String itemName, int uniqueId) {
        createNotificationChannel(context);

        PendingIntent openAppPendingIntent = createOpenAppPendingIntent(context, uniqueId);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar_inactive)
                .setContentTitle("Task")
                .setContentText("Missed task: " + itemName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setContentIntent(openAppPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(uniqueId, notificationBuilder.build());
        isRunning = true;
    }

    private void updateReminder(Context context, int id) {
        for (int i = 0; i < remindersList.size(); i++) {
            Reminder reminder = remindersList.get(i);
            if (reminder.getId() == id) {
                reminder.setOpened(true);
                JsonUtil.writeToJson(context, remindersList);
                break;
            }
        }
    }

    public static class StopAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int uniqueId = intent.getIntExtra("unique_id", 0);
            stopRingtoneAndVibration(context, uniqueId);
            stopAlarm = true;
        }
    }
}
