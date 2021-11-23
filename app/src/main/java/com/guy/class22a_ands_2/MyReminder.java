package com.guy.class22a_ands_2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import java.util.Calendar;

// https://developer.android.com/reference/android/app/PendingIntent#FLAG_UPDATE_CURRENT

public class MyReminder {

    public static void startReminder(Context context) {
        context = context.getApplicationContext();
        cancelReminder(context);

        Log.d("pttt", "DeviceBootReceiver startReminder");

        if (true) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, 1);

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + 5000, 10000, getIntent(context));
        }

    }

    public static void cancelReminder(Context context) {
        context = context.getApplicationContext();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getIntent(context));
    }

    private static PendingIntent getIntent(Context context) {
        context = context.getApplicationContext();
        Intent alarmIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                12,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return pendingIntent;
    }


}
