package com.guy.class22a_ands_2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("pttt", "ReminderReceiver onReceive");

        if (checkToActivateReminder(context)) {
//            context.startActivity(new Intent(context, Activity_Panel.class));
            actionToService(context, LocationService.START_FOREGROUND_SERVICE);
        }
    }

    private boolean checkToActivateReminder(Context context) {
        Log.d("pttt", "checkToActivateReminder A");

        context = context.getApplicationContext();
        if (!LocationService.isMyServiceRunning(context)) {
            Log.d("pttt", "checkToActivateReminder B");

            // TODO: 23/11/2021 Ask if service should run right now
            if (true) {
                // Should run now
                return true;
            }
        }
        Log.d("pttt", "checkToActivateReminder F");

        return false;
    }

    private void actionToService(Context context, String action) {
        Intent startIntent = new Intent(context, LocationService.class);
        startIntent.setAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            context.startService(startIntent);
        }
    }
}
