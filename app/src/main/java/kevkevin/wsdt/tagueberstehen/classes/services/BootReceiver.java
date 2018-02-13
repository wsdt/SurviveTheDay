package kevkevin.wsdt.tagueberstehen.classes.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.LoadingScreenActivity;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;

import static android.content.Context.NOTIFICATION_SERVICE;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                //that method restarts all countdowns (so their broadcast receiver event gets restarted).
                if (new GlobalAppSettingsMgr(context).useForwardCompatibility()) {
                    (new CustomNotification(context, LoadingScreenActivity.class, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications(context);
                } else {
                    Log.d(TAG, "Bootreceiver ignored. Because Forward-Compatibility is OFF.");
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onReceive: Could not validate broadcast action.");
            e.printStackTrace();
        }
    }
}
