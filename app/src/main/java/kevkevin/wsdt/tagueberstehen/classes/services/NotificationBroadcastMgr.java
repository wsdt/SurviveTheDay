package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.entities.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.manager.NotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

import static android.content.Context.NOTIFICATION_SERVICE;
import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_NotificationMgr.IDENTIFIER_COUNTDOWN_ID;

public class NotificationBroadcastMgr extends BroadcastReceiver {
    private static final String TAG = "AlarmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Fired broadcast.");
        //get countdown directly form storagemgr, because countdown could have been changed
        Countdown currCountdown = null;
        int alarmId = (-1);
        try {
            alarmId = intent.getIntExtra(IDENTIFIER_COUNTDOWN_ID,-1);
            currCountdown = DatabaseMgr.getSingletonInstance(context).getAllCountdowns(context,false).get(alarmId);
        } catch (Exception e) {
            Log.e(TAG, "onReceive: Could not load countdown from countdown id.");
            e.printStackTrace();
        }


        if (currCountdown != null) {
            //If countdown found, then show random generated notification of loaded countdown (this function gets only called on its interval)
            if (currCountdown.isCouIsMotivationOn() && currCountdown.isUntilDateInTheFuture() && currCountdown.isStartDateInThePast()) {
                NotificationMgr notificationMgrManager = new NotificationMgr(context, CountdownActivity.class, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));

                notificationMgrManager.issueNotification(notificationMgrManager.createRandomNotification(currCountdown));
                Log.d(TAG, "onReceive: Have shown countdown notification for ID: " + currCountdown.getCouId());
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

    private static void deleteAlarmService(@NonNull Context context, int alarmId) throws NullPointerException {
        //throws ok because called in deleteAllAlarmServices.
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(context, alarmId, new Intent(context, NotificationBroadcastMgr.class), 0));
    }

    public static void deleteAllAlarmServices(@NonNull Context context) {
        SparseArray<Countdown> allCountdowns = DatabaseMgr.getSingletonInstance(context).getAllCountdowns(context,false,true,false);
        for (int i=0;i<allCountdowns.size();i++) {
            try {
                deleteAlarmService(context,(int) allCountdowns.valueAt(i).getCouId());
                Log.d(TAG, "deleteAllAlarmServices: Deleted broadcast for countdown: "+allCountdowns.valueAt(i).getCouId());
            } catch (NullPointerException e) {
                Log.e(TAG, "deleteAllAlarmServices: Could not delete alarmservice of countdown: "+allCountdowns.valueAt(i).getCouId());
                e.printStackTrace();
            }
        }
    }
}
