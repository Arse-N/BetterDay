package com.example.betterday.common.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.betterday.common.model.Reminder;

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


    @SuppressLint("ScheduleExactAlarm")
    public void setAlarmForItem(Reminder newReminder) {
        ArrayList<Integer> selectedDays = newReminder.getSelectedDate().getDays();
        int uniqueId = generateUniqueId(newReminder.getTitle());

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("item_name", newReminder.getTitle());
        alarmIntent.putExtra("item_id", newReminder.getId());
        alarmIntent.putExtra("item_color", newReminder.getColor());
        alarmIntent.putExtra("item_position", newReminder.getTitle());
        alarmIntent.putExtra("item_duration", newReminder.getSelectedDate().getDurationHour() * 3600 + newReminder.getSelectedDate().getDurationMinute() * 60);
        alarmIntent.putExtra("unique_id", uniqueId);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        TimeZone localTimeZone = TimeZone.getDefault();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar currentCalendar = Calendar.getInstance(localTimeZone);
        selectedDays = sortForWeekdays(selectedDays, currentCalendar.get(Calendar.DAY_OF_WEEK));

        Calendar alarmCalendar = Calendar.getInstance(localTimeZone);
        alarmCalendar.set(Calendar.HOUR_OF_DAY, newReminder.getSelectedDate().getHour());
        alarmCalendar.set(Calendar.MINUTE, newReminder.getSelectedDate().getMinute());
        alarmCalendar.set(Calendar.SECOND, 0);
        alarmCalendar.set(Calendar.MILLISECOND, 0);

        if (selectedDays.contains(-1)) {
            // One-time alarm for the next day
            if (alarmCalendar.before(currentCalendar)) {
                alarmCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
        } else if (selectedDays.contains(100)) {
            // Daily repeating alarm
            if (alarmCalendar.before(currentCalendar)) {
                alarmCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            // Weekly repeating alarm for selected days
            for (int day : selectedDays) {
                alarmCalendar.set(Calendar.DAY_OF_WEEK, day);
                if (alarmCalendar.before(currentCalendar)) {
                    alarmCalendar.add(Calendar.WEEK_OF_YEAR, 1);
                }
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        }
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

    public static ArrayList<Integer> sortForWeekdays(ArrayList<Integer> arrayList, int startNumber) {

        int[] array = arrayList.stream().mapToInt(Integer::intValue).toArray();

        int index = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= startNumber) {
                index = i;
                break;
            }
        }

        ArrayList<Integer> sortedList = new ArrayList<>();

        for (int i = index; i < array.length; i++) {
            sortedList.add(array[i]);
        }

        for (int i = 0; i < index; i++) {
            sortedList.add(array[i]);
        }

        return sortedList;
    }

}
