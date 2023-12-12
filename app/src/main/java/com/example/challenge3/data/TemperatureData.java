package com.example.challenge3.data;


public class TemperatureData {
    private String timestamp;
    private String temperature;


    public TemperatureData(String timestamp, String temperature) {
        this.timestamp = timestamp;
        this.temperature = temperature;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

}
