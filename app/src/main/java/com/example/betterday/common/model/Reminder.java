package com.example.betterday.common.model;

public class Reminder {
    private String title;
    private String time;
    private String day;

    private byte durationHour;

    private byte durationMinute;

    private boolean toggleOn;

    public Reminder(String title, String time, String day, byte durationHour, byte durationMinute) {
        this.title = title;
        this.time = time;
        this.day = day;
        this.durationHour = durationHour;
        this.durationMinute = durationMinute;
        this.toggleOn = true;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public void setToggleOn(boolean toggleOn) {
        this.toggleOn = toggleOn;
    }

    public String getDay() {
        return day;
    }

    public boolean isToggleOn() {
        return toggleOn;
    }

    public byte getDurationHour() {
        return durationHour;
    }

    public byte getDurationMinute() {
        return durationMinute;
    }
}