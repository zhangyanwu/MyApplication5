package com.example.mycomputer.myapplication;

import android.app.Application;
import android.content.Intent;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Administrator on 2016/6/24.
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        SDKInitializer.initialize(getApplicationContext());
        super.onCreate();
    }
}
