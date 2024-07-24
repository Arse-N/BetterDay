package com.example.betterday.common.model;

public class Reminder {

    private int id;
    private String title;
    private String time;
    private SelectedDate selectedDate;
    private boolean toggleOn;
    private boolean opened;
    private String color;

    private int position;

    private Reminder(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.time = builder.time;
        this.toggleOn = builder.toggleOn;
        this.opened = builder.opened;
        this.color = builder.color;
        this.selectedDate = builder.selectedDate;
        this.position = builder.position;
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
        this.color = color == null ? "green" : color;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setSelectedDate(SelectedDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public SelectedDate getSelectedDate() {
        return selectedDate;
    }

    public static class Builder {
        private int id;
        private String title;
        private String time;
        private SelectedDate selectedDate;
        private boolean toggleOn = true;
        private String color;
        private int position;

        private boolean opened = false;

        public Builder() {
        }

        public Builder id(int id) {
            this.id = id;
            return this;
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

        public Builder opened(boolean opened) {
            this.opened = opened;
            return this;
        }

        public Builder color(String color) {
            this.color = color == null ? "green" : color;
            return this;
        }

        public Builder position(int position) {
            this.position = position;
            return this;
        }

        public Reminder build() {
            return new Reminder(this);
        }
    }

}
