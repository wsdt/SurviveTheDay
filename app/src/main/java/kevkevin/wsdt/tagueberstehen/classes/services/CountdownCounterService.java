package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;

public class CountdownCounterService extends Service {
    /**
     * GENERATES FOR ALL SAVED COUNTDOWN WITH ACTIVATED FOREGROUND SERVICE an own notification "foreground service"
     * EACH COUNTDOWN GETS ITS NON REMOVEABLE NOTIFICATION CREATED IN FOREGROUND SERVICE SO NOTIFICATION IS MORE CUSTUMABLE WITH PROGRESSBAR ETC.
     * BUT THE FIRST COUNTDOWN NOTIFICATION HAS TO BE SENT INTO THE STARTFOREGROUND() CALL
     */
    private static final String TAG = "CountdownCounterService";
    private HashMap<Integer, Countdown> loadedCountdownsForLiveCountdown;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //TODO: THIS PLUGIN SHOULD BE ADDED IF EVERYTHING IS TESTED AND IMPLEMENTED (GOOGLE SERVICES ETC.)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO: Mit Progressbar für jeden aktiven Countdown (not isActive Countdowns sondern eigene Einstellung dafür machen!)
        //TODO: Jede notification hier mit progressbar (in methode machen)
        //TODO: startForeground() und dort alle aktiven notifications updaten inklusive Progressbar (evtl. getRemainingPercentage() from Countdown nutzen!)
        //TODO: Before rollout tomorrow change ad ids to real ads again!

        //todo for testing
        refreshAllNotificationCounters_Interval();

        Log.d(TAG, "Finished onStartCommand().");
        return START_STICKY;
    }

    private void refreshAllNotificationCounters_Interval() {
        Log.d(TAG, "refreshALlNotificationCounters_Interval: Started method.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare(); //so Handler in Countdown.class can be created (only necessary for background service)
                while (true) {
                    startRefreshAllNotificationCounters(); //refresh all (before first Thread.sleep, so called as only method in onstart()
                    try {
                        //TODO: Make seconds configurable (for battery saving [show only when battery saving ON]
                        Thread.sleep(1000); //refresh after 1 second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void startRefreshAllNotificationCounters() {
        CustomNotification customNotificationMgr = new CustomNotification(this, CountdownActivity.class, (NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        InternalCountdownStorageMgr internalCountdownStorageMgr = new InternalCountdownStorageMgr(this);

        this.setLoadedCountdownsForLiveCountdown(internalCountdownStorageMgr.getAllCountdowns(false, true)); //false because this service should be also possible when motivateMe is off
        int foregroundNotificationCount = 0;
        for (Map.Entry<Integer, Countdown> countdown : this.getLoadedCountdownsForLiveCountdown().entrySet()) {
            //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]
            //only show if setting set for that countdown
            //NO FURTHER VALIDATION NECESSARY [untilStartDateTime Value constraints AND onlyLiveCountdowns are all validated in getAllCountdowns]
            int foregroundServiceNotificationId = Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID + countdown.getValue().getCountdownId();
            if ((foregroundNotificationCount++) > 0) {
                //only make foreground notification for first countdown, others just get a non-removable notification
                startForeground(foregroundServiceNotificationId, customNotificationMgr.createCounterServiceNotification(countdown.getValue()));//customNotificationMgr.getNotifications().get((long) foregroundServiceNotificationId).build());
            } else {
                //non-removable notifications
                customNotificationMgr.getmNotifyMgr().notify(foregroundServiceNotificationId, customNotificationMgr.createCounterServiceNotification(countdown.getValue()));
            }
        }
    }


    public HashMap<Integer, Countdown> getLoadedCountdownsForLiveCountdown() {
        if (this.loadedCountdownsForLiveCountdown == null) {
            this.loadedCountdownsForLiveCountdown = new HashMap<Integer, Countdown>();
            Log.d(TAG, "getLoadedCountdownsForLiveCountdown: Created empty HashMap, because Getter called before value assigned! (Preventing Nullpointers!)");
        }
        return loadedCountdownsForLiveCountdown;
    }

    public void setLoadedCountdownsForLiveCountdown(HashMap<Integer, Countdown> loadedCountdownsForLiveCountdown) {
        this.loadedCountdownsForLiveCountdown = loadedCountdownsForLiveCountdown;
    }
    //TODO: refresh notifications with BigCountdown_Stirng() from Countdown.class
}
