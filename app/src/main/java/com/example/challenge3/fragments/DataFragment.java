package com.example.challenge3.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.challenge3.MQTTHelper;
import com.example.challenge3.R;
import com.example.challenge3.db.DataOpenHelper;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;


public class DataFragment extends Fragment /*implements MQTTHelper.ConnectionCallback*/ {
    public interface OnNextButtonClick{
        void onNextButtonClick();
    }

    private MQTTHelper mqttHelper;
    private SwitchCompat temperatureSwitch,humiditySwitch;
    private TextView temperatureTextView, humidityTextView, temperatureSeekBarValueTextView, humiditySeekBarValueTextView;
    private Button ledButton, getDataButton, showChartButton;
    private OnNextButtonClick onNextButtonClickListener;
    private double temperatureThreshold = 50, humidityThreshold = 80;
    private SeekBar temperatureSeekBar, humiditySeekBar;
    private DataOpenHelper dbHelper;

    public DataFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data, container, false);


        mqttHelper = new MQTTHelper(getContext(), UUID.randomUUID().toString(), "");
        //mqttHelper.setConnectionCallback(this);
        mqttHelper.connect();
        //mqttHelper.publishLedControl("ON");

        dbHelper = new DataOpenHelper(getContext());

        ledButton = view.findViewById(R.id.ledControlButton);
        getDataButton = view.findViewById(R.id.getDataButton);
        showChartButton = view.findViewById(R.id.showChartButton);

        temperatureSwitch = view.findViewById(R.id.temperatureSwitch);
        humiditySwitch = view.findViewById(R.id.humiditySwitch);

        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        humidityTextView = view.findViewById(R.id.humidityTextView);
        temperatureSeekBarValueTextView = view.findViewById(R.id.temperatureSeekBarValueTextView);
        humiditySeekBarValueTextView = view.findViewById(R.id.humiditySeekBarValueTextView);

        temperatureSeekBar = view.findViewById(R.id.temperatureSeekBar);
        humiditySeekBar = view.findViewById(R.id.humiditySeekBar);


        ledButton.setOnClickListener( v -> {
            boolean ledStatus = mqttHelper.getLastLedStatus();
            if(!ledStatus)
                mqttHelper.publishLedControl("ON");
            else
                mqttHelper.publishLedControl("OFF");
        });


        // Check if any message is arrived
        mqttHelper.setMessageArrivedCallback(new MQTTHelper.MessageArrivedCallback() {
            @Override
            public void onMessageArrived(String topic, MqttMessage message) {
                getDataButton.setEnabled(true);
            }
        });

        // Set listener for the getDataButton
        getDataButton.setOnClickListener(v -> {
            displayReceivedData();
        });


        showChartButton.setOnClickListener(v -> {
            if(onNextButtonClickListener != null){
                    onNextButtonClickListener.onNextButtonClick();
                }
        });

        temperatureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // Map the progress value to the corresponding real-world value
                double realWorldValue = mapTemperatureProgressToRealValue(progress);
                temperatureThreshold = realWorldValue;
                // Update the TextView with the current real-world value
                temperatureSeekBarValueTextView.setText(String.valueOf(realWorldValue).concat("°C"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        humiditySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                humidityThreshold = progress;
                // Update the TextView
                humiditySeekBarValueTextView.setText(String.valueOf(progress).concat("%"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return view;
    }



    private double mapTemperatureProgressToRealValue(int progress) {
        // Assuming a linear mapping from progress to real-world value
        double minValue = -40.0;
        double maxValue = 80.0;
        double range = maxValue - minValue;

        return (progress * range / temperatureSeekBar.getMax()) + minValue;
    }


    private void displayReceivedData() {
        String temperatureData = dbHelper.getLastTemperatureValue();
        String humidityData = dbHelper.getLastHumidityValue();


        if (temperatureSwitch.isChecked() && !humiditySwitch.isChecked()) {
            // Display only temperature data
            temperatureTextView.setText("Temperature: " + temperatureData);
            humidityTextView.setText("");

            if(Double.parseDouble(temperatureData) > temperatureThreshold){
                Toast.makeText(requireContext(), "Temperature is too high", Toast.LENGTH_LONG).show();
            }
        } else if (!temperatureSwitch.isChecked() && humiditySwitch.isChecked()) {
            // Display only humidity data
            temperatureTextView.setText("");
            humidityTextView.setText("Humidity: " + humidityData);

            if(Double.parseDouble(humidityData) > humidityThreshold){
                Toast.makeText(requireContext(), "Humidity is too high", Toast.LENGTH_LONG).show();
            }
        } else if (temperatureSwitch.isChecked() && humiditySwitch.isChecked()) {
            // Display both temperature and humidity data
            temperatureTextView.setText("Temperature: " + temperatureData);
            humidityTextView.setText("Humidity: " + humidityData);

            if(Double.parseDouble(temperatureData) > temperatureThreshold){
                Toast.makeText(requireContext(), "Temperature is too high", Toast.LENGTH_LONG).show();
            }
            if(Double.parseDouble(humidityData) > humidityThreshold){
                Toast.makeText(requireContext(), "Humidity is too high", Toast.LENGTH_LONG).show();
            }
        } else {
            // Both switches are off
            temperatureTextView.setText("");
            humidityTextView.setText("");
        }




    }

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        if(context instanceof OnNextButtonClick){
            onNextButtonClickListener = (OnNextButtonClick) context;
        }else {
            throw new RuntimeException(context.toString());
        }
    }
    @Override
    public void onDetach(){
        super.onDetach();
        onNextButtonClickListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mqttHelper.stop();
    }

    /*@Override
    public void onConnect() {
        // Connection successful, now you can publish messages

    }

    @Override
    public void onConnectionFailure() {
        // Handle connection failure, if needed
        //Log.e(TAG, "MQTT connection failed");
    }*/
}