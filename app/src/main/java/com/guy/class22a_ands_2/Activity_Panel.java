package com.guy.class22a_ands_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class Activity_Panel extends AppCompatActivity {

    private MaterialButton panel_BTN_action;
    private MaterialButton panel_BTN_info;
    private MaterialButton panel_BTN_permission;
    private MaterialButton panel_BTN_crash;
    private MaterialTextView panel_LBL_info;

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra("EXTRA_LOCATION");
            Log.d("pttt", "onReceive" + location.getLatitude());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    panel_LBL_info.setText(
                            location.getAccuracy() + "\n"
                            + location.getBearing() + "\n"
                            + location.getAltitude() + "\n"
                            + location.getProvider() + "\n"
                            + location.getSpeed() + "\n"
                                    + location.getTime() + "\n"
                                    + location.getLatitude() + "\n"
                                    + location.getLongitude()
                    );
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        panel_BTN_action = findViewById(R.id.panel_BTN_action);
        panel_BTN_info = findViewById(R.id.panel_BTN_info);
        panel_BTN_permission = findViewById(R.id.panel_BTN_permission);
        panel_BTN_crash = findViewById(R.id.panel_BTN_crash);
        panel_LBL_info = findViewById(R.id.panel_LBL_info);
        panel_BTN_action.setOnClickListener(v -> func());
        panel_BTN_permission.setOnClickListener(v -> openPermissionPage());
        panel_BTN_info.setOnClickListener(v -> updateUI());
        panel_BTN_crash.setOnClickListener(v -> forceCrash());

        MyReminder.startReminder(this);
    }

    private void forceCrash() {
        Double.parseDouble("h");
    }

    private void openPermissionPage() {
        startActivity(new Intent(this, Activity_Location.class));
    }

    private void updateUI() {
        if (LocationService.isMyServiceRunning(this)) {
            panel_BTN_action.setText("STOP");
        } else {
            panel_BTN_action.setText("START");
        }
        panel_LBL_info.setText("Permission: " + (Activity_Location.checkForMissingPermission(this) == null ? "Granted" : "Denied") +
                "\nisRunning= " + LocationService.isMyServiceRunning(this));
    }

    private void func() {
        panel_BTN_action.setEnabled(false);
        if (LocationService.isMyServiceRunning(this)) {
            actionToService(LocationService.STOP_FOREGROUND_SERVICE);
        } else {
            actionToService(LocationService.START_FOREGROUND_SERVICE);
        }

        MyClockTickerV4.getInstance().addSingleCallback(() -> {
            runOnUiThread(() -> {
                updateUI();
                panel_BTN_action.setEnabled(true);
            });
        }, 500);
    }

    private void actionToService(String action) {
        Intent startIntent = new Intent(this, LocationService.class);
        startIntent.setAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(startIntent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);
        // Global Receiver
        //registerReceiver(myReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }


}