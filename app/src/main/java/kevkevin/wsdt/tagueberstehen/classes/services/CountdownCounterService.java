package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;

public class CountdownCounterService extends Service {
    /** GENERATES FOR ALL SAVED COUNTDOWN WITH ACTIVATED FOREGROUND SERVICE an own notification "foreground service"
     * EACH COUNTDOWN GETS ITS NON REMOVEABLE NOTIFICATION CREATED IN FOREGROUND SERVICE SO NOTIFICATION IS MORE CUSTUMABLE WITH PROGRESSBAR ETC.
     * BUT THE FIRST COUNTDOWN NOTIFICATION HAS TO BE SENT INTO THE STARTFOREGROUND() CALL */
    private static final String TAG = "CountdownCounterService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Only one countdown

    //TODO: THIS PLUGIN SHOULD BE ADDED IF EVERYTHING IS TESTED AND IMPLEMENTED (GOOGLE SERVICES ETC.)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO: Mit Progressbar für jeden aktiven Countdown (not isActive Countdowns sondern eigene Einstellung dafür machen!)
        //TODO: Jede notification hier mit progressbar (in methode machen)
        //TODO: startForeground() und dort alle aktiven notifications updaten inklusive Progressbar (evtl. getRemainingPercentage() from Countdown nutzen!)
        //TODO: Before rollout tomorrow change ad ids to real ads again!

        CustomNotification customNotificationMgr = new CustomNotification(this, CountdownActivity.class, (NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        InternalStorageMgr internalStorageMgr = new InternalStorageMgr(this);
        Countdown countdown = internalStorageMgr.getCountdown(0);
        customNotificationMgr.createIssueCounterServiceNotification(this, countdown);
        Log.d(TAG, "Finished onStartCommand().");
        return START_STICKY;
    }
}
