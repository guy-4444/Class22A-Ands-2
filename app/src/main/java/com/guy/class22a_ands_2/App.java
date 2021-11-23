package com.guy.class22a_ands_2;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MyClockTickerV4.initHelper();
    }
}
