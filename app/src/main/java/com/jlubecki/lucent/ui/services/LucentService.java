package com.jlubecki.lucent.ui.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jacob on 10/22/16.
 */

public class LucentService extends Service {
    private final int UPDATE_INTERVAL = 60 * 1000;
    private Timer timer = new Timer();
    private static final int NOTIFICATION_EX = 1;
    private NotificationManager notificationManager;

    public LucentService() {
    }

    @Override
    public void onCreate() {
        // Code to execute when the service is first created
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = android.R.drawable.stat_notify_sync;
        CharSequence tickerText = "Hello";
        long when = System.currentTimeMillis();
        Context context = getApplicationContext();
        CharSequence contentTitle = "My notification";
        CharSequence contentText = "Hello World!";

        // FIXME: Do the thing.
//        Intent notificationIntent = new Intent(this, Main.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);

//        Notification notification = new Notification(icon, tickerText, when);
//        notification.setLatestEventInfo(context, contentTitle, contentText,
//                contentIntent);


//        notificationManager.notify(NOTIFICATION_EX, notification);
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                // Check if there are updates here and notify if true
            }
        }, 0, UPDATE_INTERVAL);
        return START_STICKY;
    }

    private void stopService() {
        if (timer != null) timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
