package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.DatabaseMgr;

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
            alarmId = intent.getIntExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID,-1);
            currCountdown = DatabaseMgr.getSingletonInstance(context).getCountdown(context,alarmId);
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
        //throws ok because called in deleteAllAlarmServices.
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(context, alarmId, new Intent(context, NotificationService_AlarmmanagerBroadcastReceiver.class), 0));
    }

    public static void deleteAllAlarmServices(Context context) {
        SparseArray<Countdown> allCountdowns = DatabaseMgr.getSingletonInstance(context).getAllCountdowns(context,true,false);
        for (int i=0;i<allCountdowns.size();i++) {
            try {
                ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(context, allCountdowns.valueAt(i).getCountdownId(), new Intent(context, NotificationService_AlarmmanagerBroadcastReceiver.class), 0));
                Log.d(TAG, "deleteAllAlarmServices: Deleted broadcast for countdown: "+allCountdowns.valueAt(i).getCountdownId());
            } catch (NullPointerException e) {
                Log.e(TAG, "deleteAllAlarmServices: Could not delete alarmservice of countdown: "+allCountdowns.valueAt(i).getCountdownId());
                e.printStackTrace();
            }
        }
    }
}
