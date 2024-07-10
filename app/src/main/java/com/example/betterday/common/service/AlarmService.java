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
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("item_name", newReminder.getTitle());
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        int uniqueId = generateUniqueId(newReminder.getTitle());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        TimeZone localTimeZone = TimeZone.getDefault();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        selectedDays = sortForWeekdays(selectedDays, calendar.get(Calendar.DAY_OF_WEEK));
        for (int day : selectedDays) {
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTimeZone(localTimeZone);
            alarmCalendar.set(Calendar.HOUR_OF_DAY, newReminder.getSelectedDate().getHour());
            alarmCalendar.set(Calendar.MINUTE, newReminder.getSelectedDate().getMinute());
            alarmCalendar.set(Calendar.SECOND, 0);
            alarmCalendar.set(Calendar.MILLISECOND, 0);
            alarmCalendar.set(Calendar.DAY_OF_WEEK, day);

            if (alarmCalendar.before(Calendar.getInstance(localTimeZone))) {
                alarmCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            }
            Long j = alarmCalendar.getTimeInMillis();
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(alarmCalendar.getTimeInMillis(), pendingIntent), pendingIntent);
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
