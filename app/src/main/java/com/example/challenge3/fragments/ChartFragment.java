package com.example.challenge3.fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.challenge3.R;
import com.example.challenge3.db.DataOpenHelper;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartFragment extends Fragment {

    private LineChart lineChart;
    private DataOpenHelper dbHelper;


    public ChartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChart = view.findViewById(R.id.lineChart);

        dbHelper = new DataOpenHelper(requireContext());

        // Populate the chart with data
        displayChartData();

        return view;
    }

    private void displayChartData() {
        List<Entry> temperatureEntries = new ArrayList<>();
        List<Entry> humidityEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();


        Cursor temperatureCursor = dbHelper.getLast24HoursTemperatureValues(); // Adjust as needed
        Cursor humidityCursor = dbHelper.getLast24HoursHumidityValues(); // Adjust as needed

        if (temperatureCursor.moveToFirst() && humidityCursor.moveToFirst()) {
            do {
                String temperatureTimestamp = temperatureCursor.getString(temperatureCursor.getColumnIndex(DataOpenHelper.COLUMN_TIMESTAMP));
                String temperatureValue = temperatureCursor.getString(temperatureCursor.getColumnIndex(DataOpenHelper.COLUMN_VALUE));

                String humidityTimestamp = humidityCursor.getString(humidityCursor.getColumnIndex(DataOpenHelper.COLUMN_TIMESTAMP));
                String humidityValue = humidityCursor.getString(humidityCursor.getColumnIndex(DataOpenHelper.COLUMN_VALUE));

                // Convert the timestamp to a readable format if needed
                String formattedTimestamp = convertTimestamp(temperatureTimestamp);

                temperatureEntries.add(new Entry(temperatureEntries.size(), Float.parseFloat(temperatureValue)));
                humidityEntries.add(new Entry(humidityEntries.size(), Float.parseFloat(humidityValue)));
                labels.add(formattedTimestamp);

            } while (temperatureCursor.moveToNext() && humidityCursor.moveToNext());
        }

        temperatureCursor.close();
        humidityCursor.close();
        dbHelper.close();

        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
        temperatureDataSet.setColor(Color.BLUE);
        temperatureDataSet.setCircleColor(Color.BLUE);
        temperatureDataSet.setDrawValues(false);
        temperatureDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        humidityDataSet.setColor(Color.GREEN);
        humidityDataSet.setCircleColor(Color.GREEN);
        humidityDataSet.setDrawValues(false);
        humidityDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(temperatureDataSet);
        //dataSets.add(humidityDataSet);

        LineData lineData = new LineData(dataSets);

        lineChart.setData(lineData);
        lineChart.getDescription().setText("");

        /*XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set X-axis position to bottom
        xAxis.setValueFormatter(new DateValueFormatter(labels)); // Set your custom formatter*/
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set X-axis position to bottom
        xAxis.setValueFormatter(new DateValueFormatter(labels)); // Set your custom formatter


        YAxis leftYAxis = lineChart.getAxisLeft();
        //leftYAxis.setValueFormatter(new DefaultValueFormatter(1));
        leftYAxis.setValueFormatter(new CustomValueFormatter(true));


        // Add a right Y-axis for humidity
        YAxis rightYAxis = lineChart.getAxisRight();
        //rightYAxis.setValueFormatter(new DefaultValueFormatter(1));
        rightYAxis.setValueFormatter(new CustomValueFormatter(false));
        rightYAxis.setAxisMinimum(0f);

        // Add humidity dataset to the chart
        dataSets.add(humidityDataSet);
        lineChart.setData(new LineData(dataSets));

        lineChart.invalidate();
    }



    private String convertTimestamp(String timestamp) {
        // Convert your timestamp to a readable format if needed
        // Example: "yyyy-MM-dd HH:mm:ss.SS" to "HH:mm:ss"
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.getDefault());
            Date date = sourceFormat.parse(timestamp);

            SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return targetFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return timestamp; // Return the original timestamp if conversion fails
        }
    }

    private static class DateValueFormatter extends ValueFormatter {
        private final List<String> labels;

        DateValueFormatter(List<String> labels) {
            this.labels = labels;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < labels.size()) {
                return labels.get(index);
            }
            return "";
        }
    }

    private static class CustomValueFormatter extends ValueFormatter {
        private final boolean isTemperature;

        public CustomValueFormatter(boolean isTemperature) {
            this.isTemperature = isTemperature;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            if (isTemperature) {
                // Format as °C for temperature
                return String.format(Locale.getDefault(), "%.0f°C", value);
            } else {
                // Format as percentage for humidity
                return String.format(Locale.getDefault(), "%.0f%%", value);
            }
        }
    }

}
