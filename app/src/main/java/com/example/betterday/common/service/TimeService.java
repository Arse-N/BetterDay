package com.example.betterday.common.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import com.example.betterday.common.constants.Day;
import com.example.betterday.common.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.IntStream;

public class TimeService {

    private final ArrayList<Reminder> remindersList;

    private ArrayList<Integer> selectedDays;

    private final Context context;

    public TimeService(ArrayList<Reminder> reminders, Context context) {
        this.remindersList = reminders;
        this.context = context;
    }

    public String getDay(ToggleButton[] days, ToggleButton ringOnce) {
        selectedDays = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (ringOnce.isChecked()) {
            selectedDays.add(-1);
            selectedDays.add(dayOfWeek);
            return Day.RING_ONCE;
        } else {
            StringBuilder a = new StringBuilder();
            long checkedCount = IntStream.range(0, days.length).filter(i -> days[i].isChecked()).count();

            if (checkedCount == 0) {
                selectedDays.add(-1);
                selectedDays.add(dayOfWeek);
                return Day.RING_ONCE;
            }

            boolean allChecked = checkedCount == days.length;
            IntStream.range(0, days.length).filter(i -> days[i].isChecked()).forEach(i -> {
                if (a.length() > 0) {
                    a.append(" ");
                }
                a.append(Day.WEEK_DAYS_SHORT[i]);
                selectedDays.add(i + 1);
            });

            if (allChecked) {
                selectedDays.add(100);
                return Day.EVERY_DAY;
            }

            return a.toString();
        }
    }

    public ArrayList<Integer> getSelectedDays() {
        return selectedDays;
    }

    public String getTime(TimePicker timePicker) {
        int hour, minute;
        String amPm;
        hour = timePicker.getHour();
        minute = timePicker.getMinute();

        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        } else {
            amPm = "AM";
            if (hour == 0) {
                hour = 12;
            }
        }
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    public void changeWeekDaysStatus(boolean status, ToggleButton[] days) {
        for (ToggleButton day : days) {
            day.setChecked(status);
            day.setEnabled(status);
        }
    }
}
