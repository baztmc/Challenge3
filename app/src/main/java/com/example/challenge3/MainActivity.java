package com.example.challenge3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.challenge3.fragments.DataFragment;
import com.example.challenge3.fragments.ChartFragment;

public class MainActivity extends AppCompatActivity implements DataFragment.OnNextButtonClick {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DataFragment())
                    .commit();
        }
    }

    @Override
    public void onNextButtonClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ChartFragment())
                .addToBackStack(null)
                .commit();
    }
}