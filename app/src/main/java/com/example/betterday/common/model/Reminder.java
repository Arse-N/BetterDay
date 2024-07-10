package com.example.betterday.common.model;

public class Reminder {
    private String title;
    private String time;
    private SelectedDate selectedDate;
    private boolean toggleOn;
    private String color;

    private Reminder(Builder builder) {
        this.title = builder.title;
        this.time = builder.time;
        this.toggleOn = builder.toggleOn;
        this.color = builder.color;
        this.selectedDate = builder.selectedDate;
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

    public boolean isToggleOn() {
        return toggleOn;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSelectedDate(SelectedDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public SelectedDate getSelectedDate() {
        return selectedDate;
    }

    public static class Builder {
        private String title;
        private String time;
        private SelectedDate selectedDate;
        private boolean toggleOn = true;
        private String color;

        public Builder() {
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder selectedDate(SelectedDate selectedDate) {
            this.selectedDate = selectedDate;
            return this;
        }

        public Builder toggleOn(boolean toggleOn) {
            this.toggleOn = toggleOn;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Reminder build() {
            return new Reminder(this);
        }
    }

}
