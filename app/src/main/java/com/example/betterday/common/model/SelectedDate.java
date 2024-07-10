package com.example.betterday.common.model;

import java.util.ArrayList;

public class SelectedDate {

    private ArrayList<Integer> days;
    private int hour;
    private int minute;
    private String dayStringFormat;

    private byte durationHour;
    private byte durationMinute;

    private SelectedDate(Builder builder) {
        this.days = builder.days;
        this.hour = builder.hour;
        this.minute = builder.minute;
        this.dayStringFormat = builder.dayStringFormat;
    }

    public ArrayList<Integer> getDays() {
        return days;
    }

    public void setDays(ArrayList<Integer> days) {
        this.days = days;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public byte getDurationHour() {
        return durationHour;
    }

    public void setDurationHour(byte durationHour) {
        this.durationHour = durationHour;
    }

    public byte getDurationMinute() {
        return durationMinute;
    }

    public void setDurationMinute(byte durationMinute) {
        this.durationMinute = durationMinute;
    }

    public String getDayStringFormat() {
        return dayStringFormat;
    }

    public void setDayStringFormat(String dayStringFormat) {
        this.dayStringFormat = dayStringFormat;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public static class Builder {
        private ArrayList<Integer> days;
        private int hour;
        private int minute;
        private String dayStringFormat;

        private byte durationHour;
        private byte durationMinute;

        public Builder() {
            // Optional: Initialize with default values
        }

        public Builder days(ArrayList<Integer> days) {
            this.days = days;
            return this;
        }

        public Builder hour(int hour) {
            this.hour = hour;
            return this;
        }

        public Builder minute(int minute) {
            this.minute = minute;
            return this;
        }

        public Builder dayStringFormat(String dayStringFormat) {
            this.dayStringFormat = dayStringFormat;
            return this;
        }

        public Builder durationHour(byte durationHour) {
            this.durationHour = durationHour;
            return this;
        }

        public Builder durationMinute(byte durationMinute) {
            this.durationMinute = durationMinute;
            return this;
        }

        public SelectedDate build() {
            return new SelectedDate(this);
        }
    }

}
