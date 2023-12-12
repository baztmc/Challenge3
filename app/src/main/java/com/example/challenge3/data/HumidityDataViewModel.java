package com.example.challenge3.data;

import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HumidityDataViewModel extends ViewModel {

    private ArrayList<HumidityData> data;

    public HumidityDataViewModel(){
        data = new ArrayList<>();
    }

    public void addHumidityData(String humidity){
        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        // Convert milliseconds to Date
        Date date = new Date(currentTimeMillis);
        // Format the Date as a string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SS");
        String formattedDate = dateFormat.format(date);

        data.add(new HumidityData(formattedDate,humidity));
    }

    public String getLastHumidity() {
        return data.get(data.size()-1).getHumidity();
    }
}
