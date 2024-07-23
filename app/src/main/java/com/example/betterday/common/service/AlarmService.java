package com.example.betterday.common.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.example.betterday.common.model.Reminder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class AlarmService {

    private final ArrayList<Reminder> remindersList;
    private final Context context;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    public AlarmService(ArrayList<Reminder> reminders, Context context) {
        this.remindersList = reminders;
        this.context = context;
    }

    private boolean canScheduleExactAlarms() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        } else {
            return true;
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setAlarmForItem(Reminder newReminder) {
        if (!canScheduleExactAlarms()) {
            Toast.makeText(context, "Exact alarm permission is required for this feature", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Integer> selectedDays = newReminder.getSelectedDate().getDays();
        int uniqueId = generateUniqueId(newReminder.getTitle());

        TimeZone localTimeZone = TimeZone.getDefault();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar currentCalendar = Calendar.getInstance(localTimeZone);

        Intent alarmIntent = getIntent(newReminder, uniqueId);

        if (selectedDays.contains(-1)) {  // Case: One-time alarm
            Calendar alarmCalendar = Calendar.getInstance(localTimeZone);
            alarmCalendar.set(Calendar.HOUR_OF_DAY, newReminder.getSelectedDate().getHour());
            alarmCalendar.set(Calendar.MINUTE, newReminder.getSelectedDate().getMinute());
            alarmCalendar.set(Calendar.SECOND, 0);
            alarmCalendar.set(Calendar.MILLISECOND, 0);

            if (alarmCalendar.before(currentCalendar)) {
                alarmCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
        } else if (selectedDays.contains(100)) {  // Case: Daily alarm
            Calendar alarmCalendar = Calendar.getInstance(localTimeZone);
            alarmCalendar.set(Calendar.HOUR_OF_DAY, newReminder.getSelectedDate().getHour());
            alarmCalendar.set(Calendar.MINUTE, newReminder.getSelectedDate().getMinute());
            alarmCalendar.set(Calendar.SECOND, 0);
            alarmCalendar.set(Calendar.MILLISECOND, 0);

            if (alarmCalendar.before(currentCalendar)) {
                alarmCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {  // Case: Custom days alarm
            for (int day : selectedDays) {
                Calendar alarmCalendar = Calendar.getInstance(localTimeZone);
                alarmCalendar.set(Calendar.HOUR_OF_DAY, newReminder.getSelectedDate().getHour());
                alarmCalendar.set(Calendar.MINUTE, newReminder.getSelectedDate().getMinute());
                alarmCalendar.set(Calendar.SECOND, 0);
                alarmCalendar.set(Calendar.MILLISECOND, 0);

                int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
                int daysUntilNextAlarm = (day - currentDayOfWeek + 7) % 7;
                if (daysUntilNextAlarm == 0 && alarmCalendar.before(currentCalendar)) {
                    daysUntilNextAlarm = 7;
                }
                alarmCalendar.add(Calendar.DAY_OF_YEAR, daysUntilNextAlarm);

                int requestCode = uniqueId + day;
                Intent customAlarmIntent = getIntent(newReminder, requestCode);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, customAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
                alarmCalendar.add(Calendar.WEEK_OF_YEAR, 1);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }
        }
    }

    @NotNull
    private Intent getIntent(Reminder newReminder, int uniqueId) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("item_name", newReminder.getTitle());
        alarmIntent.putExtra("item_id", newReminder.getId());
        alarmIntent.putExtra("item_color", newReminder.getColor());
        alarmIntent.putExtra("item_position", newReminder.getTitle());
        alarmIntent.putExtra("item_duration", newReminder.getSelectedDate().getDurationHour() * 3600 + newReminder.getSelectedDate().getDurationMinute() * 60);
        alarmIntent.putExtra("unique_id", uniqueId);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return alarmIntent;
    }

    public void cancelAlarmForItem(int position) {
        String itemName = remindersList.get(position).getTitle();
        int uniqueId = generateUniqueId(itemName);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private int generateUniqueId(String itemName) {
        long timestamp = System.currentTimeMillis();
        String uniqueString = itemName + timestamp;
        return uniqueString.hashCode();
    }


}
