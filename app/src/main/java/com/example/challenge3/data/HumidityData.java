package com.example.challenge3.data;


public class HumidityData {
    private String timestamp;
    private String humidity;


    public HumidityData(String timestamp, String humidity) {
        this.timestamp = timestamp;
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

}
