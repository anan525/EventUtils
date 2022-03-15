package com.voyah.eventutils;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.voyah.eventbuss.EventbusUtils;
import com.voyah.compiler.Subscribe;
import com.voyah.compiler.ThreadMode;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventbusUtils.getDefault().register(this);
    }


    @Subscribe(threadMode = ThreadMode.MAINTHREAD)
    public void event(Object obj) {

    }

    @Subscribe(isSticky = true)
    public void sticky(Object obj) {
    }
}