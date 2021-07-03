package com.example.todomanager001;

public class realtimedata {
    private String key, heading, details, time, date, warning;

    public realtimedata() {
    }

    public realtimedata(String key, String heading, String details, String time, String date, String warning) {
        this.key = key;
        this.heading = heading;
        this.details = details;
        this.time = time;
        this.date = date;
        this.warning = warning;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWarning() { return warning; }

    public void setWarning(String warning) { this.warning = warning; }
}
