package com.example.hoverfly;

public class TimeResponse {
    String date;
    String milliseconds_since_epoch;
    String time;

    public TimeResponse(){}

    public TimeResponse(String date, String milliseconds_since_epoch, String time) {
        this.date = date;
        this.milliseconds_since_epoch = milliseconds_since_epoch;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMilliseconds_since_epoch() {
        return milliseconds_since_epoch;
    }

    public void setMilliseconds_since_epoch(String milliseconds_since_epoch) {
        this.milliseconds_since_epoch = milliseconds_since_epoch;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "TimeResponse{" +
                "date='" + date + '\'' +
                ", milliseconds_since_epoch='" + milliseconds_since_epoch + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
