package com.example.betterday.common.service;

import android.text.TextUtils;
import com.example.betterday.common.model.Reminder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ValidationService {

    private final ArrayList<Reminder> remindersList;

    public ValidationService(ArrayList<Reminder> reminders) {
        this.remindersList = reminders;
    }

    public boolean validateInput(TextInputEditText titleEditText, TextInputLayout titleTextLayout) {
        String input = titleEditText.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            titleTextLayout.setError("Title cannot be empty!");
            return false;
        } else {
            titleTextLayout.setError(null);
            return true;
        }
    }

    public boolean checkNewItemsDate(Reminder newReminder) {
        int newReminderStartTime = newReminder.getSelectedDate().getHour() * 60 + newReminder.getSelectedDate().getMinute();
        for (Reminder reminder : remindersList) {
            if (reminder.isToggleOn()) {
                int existingReminderStartTime = reminder.getSelectedDate().getHour() * 60 + reminder.getSelectedDate().getMinute();
                int existingReminderEndTime = existingReminderStartTime +
                        (reminder.getSelectedDate().getDurationHour() * 60 + reminder.getSelectedDate().getDurationMinute());

                if (newReminderStartTime == existingReminderStartTime || (newReminderStartTime < existingReminderEndTime && newReminderStartTime > existingReminderStartTime)) {
                    for (int day : newReminder.getSelectedDate().getDays()) {
                        if (reminder.getSelectedDate().getDays().contains(day)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkNewItemsDuration(Reminder newReminder) {
        int newReminderStartTime = newReminder.getSelectedDate().getHour() * 60 + newReminder.getSelectedDate().getMinute();
        int newReminderEndTime = newReminderStartTime +
                (newReminder.getSelectedDate().getDurationHour() * 60 + newReminder.getSelectedDate().getDurationMinute());

        for (Reminder reminder : remindersList) {
            if (reminder.isToggleOn()) {
                int existingReminderStartTime = reminder.getSelectedDate().getHour() * 60 + reminder.getSelectedDate().getMinute();

                if ((newReminderEndTime > existingReminderStartTime && newReminderStartTime < existingReminderStartTime)) {
                    for (int day : newReminder.getSelectedDate().getDays()) {
                        if (reminder.getSelectedDate().getDays().contains(day)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
