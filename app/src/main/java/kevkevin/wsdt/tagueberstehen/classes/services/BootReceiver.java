package kevkevin.wsdt.tagueberstehen.classes.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;

import static android.content.Context.NOTIFICATION_SERVICE;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                //that method restarts all countdowns (so their broadcast receiver event gets restarted).
                (new CustomNotification(context, CountdownActivity.class, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications(context);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onReceive: Could not validate broadcast action.");
            e.printStackTrace();
        }
    }
}
