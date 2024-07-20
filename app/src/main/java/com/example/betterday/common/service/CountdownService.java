//package com.example.betterday.common.service;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.CountDownTimer;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import com.example.betterday.common.model.Reminder;
//
//import java.util.concurrent.TimeUnit;
//
//public class CountdownService extends Service {
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    public void startCountdown(@NonNull ReminderAdapter.ReminderViewHolder holder, Reminder reminder) {
//
//        final Handler uiHandler = new Handler(Looper.getMainLooper());
//        int duration = reminder.getSelectedDate().getDurationHour() * 3600 + reminder.getSelectedDate().getDurationMinute() * 60;
//
//        new CountDownTimer(duration * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                uiHandler.post(() -> {
//                    String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
//                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
//                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
//                    final String[] hourMinSec = time.split(":");
//                    holder.hourValue.setText(hourMinSec[0]);
//                    holder.minuteValue.setText(hourMinSec[1]);
//                    holder.secondValue.setText(hourMinSec[2]);
//                });
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();
//    }
//}
