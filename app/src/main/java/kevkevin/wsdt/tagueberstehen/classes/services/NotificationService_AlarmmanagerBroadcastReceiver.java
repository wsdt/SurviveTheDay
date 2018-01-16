package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationService_AlarmmanagerBroadcastReceiver extends BroadcastReceiver {
    /*For short intervals the background service might be more performant (so this should be selectable)*/

    private static final String TAG = "AlarmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Fired broadcast.");
        //get countdown directly form storagemgr, because countdown could have been changed
        Countdown currCountdown = null;
        int alarmId = (-1);
        try {
            alarmId = intent.getIntExtra("COUNTDOWN_ID",-1);
            currCountdown = (new InternalCountdownStorageMgr(context)).getCountdown(alarmId);
        } catch (Exception e) {
            Log.e(TAG, "onReceive: Could not load countdown from countdown id.");
            e.printStackTrace();
        }


        if (currCountdown != null) {
            //If countdown found, then show random generated notification of loaded countdown (this function gets only called on its interval)
            if (currCountdown.isActive() && currCountdown.isUntilDateInTheFuture() && currCountdown.isStartDateInThePast()) {
                CustomNotification customNotificationManager = new CustomNotification(context, CountdownActivity.class, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));

                customNotificationManager.issueNotification(customNotificationManager.createRandomNotification(currCountdown));
                Log.d(TAG, "onReceive: Have shown countdown notification for ID: " + currCountdown.getCountdownId());
            } else {
                Log.d(TAG, "onReceive: Countdown is not active, startDateTime is in the future or UntilDateTime is in the past. Deleted it from broadcast receiver (not deleted in SharedPreferences.");
                deleteAlarmService(context, alarmId);
            }
        } else {
            if (alarmId > (-1)) {
                Log.e(TAG, "onReceive: Countdown is null! Tried to delete receiver for countdown.");
                try {
                    deleteAlarmService(context, alarmId);
                    Log.d(TAG, "onReceive: Deleted Alarmservice of " + alarmId);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onReceive: Could not delete broadcast for countdown! ");
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "Countdown Id is smaller or equal to -1: "+alarmId);
            }
        }
    }

    private void deleteAlarmService(Context context, int alarmId) throws NullPointerException {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(context, alarmId, new Intent(context, NotificationService_AlarmmanagerBroadcastReceiver.class), 0));
    }
}
