package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.LoadingScreenActivity;
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
    private CustomNotification customNotificationMgr;
    private InternalCountdownStorageMgr internalCountdownStorageMgr;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Notification Manager and Internal Storage Mgr in shouldThisServiceBeKilled() NEEDED! (NullPointerException)
        this.setCustomNotificationMgr(new CustomNotification(this, LoadingScreenActivity.class, (NotificationManager) getSystemService(NOTIFICATION_SERVICE)));
        this.setInternalCountdownStorageMgr(new InternalCountdownStorageMgr(this));

        shouldThisServiceBeKilled(intent); //third function call should be this!! (because service gets killed with startService = goodPractice

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
        int foregroundNotificationCount = 0;
        if (this.getLoadedCountdownsForLiveCountdown() == null) {
            //Only do when null at first, because notifications would not be removed when expired! (so we will have to restart whole service for new countdowns)
            this.setLoadedCountdownsForLiveCountdown(getInternalCountdownStorageMgr().getAllCountdowns(false, true)); //false because this service should be also possible when motivateMe is off
        }

        for (Map.Entry<Integer, Countdown> countdown : this.getLoadedCountdownsForLiveCountdown().entrySet()) {
            Log.d(TAG, "startRefreshAllNotificationCounters: Found entry: "+countdown.getKey());
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

        //Kill service if NO countdowns should be active
        if (foregroundNotificationCount <= 0) {
            killThisService();
        }

        //Do after above lines, because so expired countdowns can be modified/removed in createCounterServiceNotification()
        this.setLoadedCountdownsForLiveCountdown(getInternalCountdownStorageMgr().getAllCountdowns(false, true)); //false because this service should be also possible when motivateMe is off
    }

    private void shouldThisServiceBeKilled(Intent intent) {
        if (intent != null) {
            try {
                if (Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE == intent.getIntExtra(Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE_LABEL,(Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE)*(-1))) {//*-1 so error value can NEVER equal to correct stopValue
                    killThisService();
                } else {
                    Log.e(TAG, "shouldThisServiceBeKilled: Maybe no extra found for killing this service or given value is wrong!. So this instance will stay alive.");
                }
            } catch (Exception e) {
                Log.e(TAG, "shouldThisServiceBeKilled: Error happened. Maybe no extra found for killing this service. So this instance will stay alive.");
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "shouldThisServiceBeKilled: Intent equals null! Maybe foreground service got restarted!");
        }
    }

    private void killThisService() {
        Log.d(TAG, "killThisService: Trying to kill myself.");
        stopForeground(true); //both lines are necessary
        //remove all notifications of this service
        removeAllServiceLiveCountdownNotifications();
        stopSelf();
        Log.d(TAG, "killThisService: Tried to kill service ungracefully.");
        android.os.Process.killProcess(android.os.Process.myPid()); //Important: Otherwise service would not stop because sth will still run in this instance
    }

    private void removeAllServiceLiveCountdownNotifications() {
        ArrayList<Integer> allLiveCountdownNotificationIds = new ArrayList<>();
        for (Map.Entry<Integer, Countdown> countdownEntry : getInternalCountdownStorageMgr().getAllCountdowns(false, true).entrySet()) {
            //false because this service should be also possible when motivateMe is off
            allLiveCountdownNotificationIds.add(Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID + countdownEntry.getValue().getCountdownId());
        }

        this.getCustomNotificationMgr().removeNotifications(allLiveCountdownNotificationIds);
    }


    public HashMap<Integer, Countdown> getLoadedCountdownsForLiveCountdown() {
        //Should be null so we can evaluate it
        /*if (this.loadedCountdownsForLiveCountdown == null) {
            this.loadedCountdownsForLiveCountdown = new HashMap<Integer, Countdown>();
            Log.d(TAG, "getLoadedCountdownsForLiveCountdown: Created empty HashMap, because Getter called before value assigned! (Preventing Nullpointers!)");
        }*/
        return loadedCountdownsForLiveCountdown;
    }

    public void setLoadedCountdownsForLiveCountdown(HashMap<Integer, Countdown> loadedCountdownsForLiveCountdown) {
        this.loadedCountdownsForLiveCountdown = loadedCountdownsForLiveCountdown;
    }

    public CustomNotification getCustomNotificationMgr() {
        return customNotificationMgr;
    }

    public void setCustomNotificationMgr(CustomNotification customNotificationMgr) {
        this.customNotificationMgr = customNotificationMgr;
    }

    public InternalCountdownStorageMgr getInternalCountdownStorageMgr() {
        return internalCountdownStorageMgr;
    }

    public void setInternalCountdownStorageMgr(InternalCountdownStorageMgr internalCountdownStorageMgr) {
        this.internalCountdownStorageMgr = internalCountdownStorageMgr;
    }
    //TODO: refresh notifications with BigCountdown_Stirng() from Countdown.class
}
