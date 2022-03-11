package com.voyah.eventutils;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}