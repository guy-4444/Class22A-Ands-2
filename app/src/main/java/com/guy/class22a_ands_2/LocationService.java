package com.guy.class22a_ands_2;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class LocationService extends Service {

    public static final String BROADCAST_NEW_LOCATION_DETECTED = "BROADCAST_NEW_LOCATION_DETECTED";

    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String PAUSE_FOREGROUND_SERVICE = "PAUSE_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";


    public static int NOTIFICATION_ID = 153;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.class.background.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.class.background.locationservice.action.main";

    private NotificationCompat.Builder notificationBuilder;
    private boolean isServiceRunningRightNow = false;

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null  ||  intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        Log.d("pttt", "Service Action= " + action);

        if (action.equals(START_FOREGROUND_SERVICE)) {
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }

            isServiceRunningRightNow = true;
            notifyToUserForForegroundService();
            startRecording();
            return START_STICKY;
        } else if (action.equals(PAUSE_FOREGROUND_SERVICE)) {

        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            stopRecording();
            stopForeground(true);
            stopSelf();
            isServiceRunningRightNow = false;
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult.getLastLocation() != null) {
                Log.d("pttt", ":getLastLocation");
                newLocationDetected(locationResult.getLastLocation());
            } else {
                Log.d("pttt", "Location information isn't available.");
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    private void newLocationDetected(Location lastLocation) {
        if (lastLocation == null  ||  lastLocation.getLatitude() == 0)  {
            return;
        }

        float accuracy = lastLocation.getAccuracy();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            float x1 = lastLocation.getBearingAccuracyDegrees();
            float x2 = lastLocation.getSpeedAccuracyMetersPerSecond();
            float x3 = lastLocation.getVerticalAccuracyMeters();
        }
        boolean x4 = lastLocation.hasAccuracy();
        double x5 = lastLocation.getLatitude();
        double x6 = lastLocation.getLongitude();
        double x7 = lastLocation.getAltitude();
        float x8 = lastLocation.getBearing();
        long x9 = lastLocation.getElapsedRealtimeNanos();
        String x10 = lastLocation.getProvider();
        float x11 = lastLocation.getSpeed();
        long x12 = lastLocation.getTime();

        // Calculate Distance
        //float[] results = new float[3];
        //Location.distanceBetween(subjectHomeLat, subjectHomeLon, lastLocation.getLatitude(), lastLocation.getLongitude(), results);
        //String str = new SimpleDateFormat("dd/MM/yy HH:mm").format(System.currentTimeMillis()) + " [" +lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "] distance:" + new DecimalFormat("##.##").format(results[0]) + " accuracy:" + new DecimalFormat("##.##").format(accuracy) + "\n";

        // update panel activity ui
        Log.d("pttt", "newLocationDetected");
        Intent intent = new Intent(BROADCAST_NEW_LOCATION_DETECTED);
        intent.putExtra("EXTRA_LOCATION", lastLocation);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        // Global Receiver
        // sendBroadcast(intent);

        // update notification
        //updateNotification("Distance from home: " + String.format("%.2f", results[0]) + " m");
    }

    private void startRecording() {
        // Keep CPU working
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();



        // Run GPS
        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setSmallestDisplacement(0.5f);
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(500);
            //locationRequest.setMaxWaitTime(TimeUnit.MINUTES.toMillis(1));
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        // Clock ticker
        MyClockTickerV4.getInstance().addCallback(new MyClockTickerV4.CycleTicker() {
            @Override
            public void secondly(int repeatsRemaining) {
                Log.d("pttt", "secondly");
            }

            @Override
            public void done() { }
        }, MyClockTickerV4.CONTINUOUSLY_REPEATS, 1000);
    }

    private void stopRecording() {
        // Stop Click Ticker
        MyClockTickerV4.getInstance().removeAllCallbacks();

        // Release CPU Holding
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

        // Stop Location Listener
        // Stop GPS
        if (fusedLocationProviderClient != null) {
            Task<Void> task = fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("pttt", "stop Location Callback removed.");
                        stopSelf();
                    } else {
                        Log.d("pttt", "stop Failed to remove Location Callback.");
                    }
                }
            });
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    // // // // // // // // // // // // // // // // Notification  // // // // // // // // // // // // // // //


    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, Activity_Panel.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder.setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_sattelite)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("App in progress")
                .setContentText("Content")
        ;

        Notification notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Cycling map channel";
        String description = notifications_channel_description;
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runs = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
