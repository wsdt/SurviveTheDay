package kevkevin.wsdt.tagueberstehen.classes.services;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppPurchaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

public class LiveCountdown_ForegroundService extends Service {
    /**
     * GENERATES FOR ALL SAVED COUNTDOWN WITH ACTIVATED FOREGROUND SERVICE an own notification "foreground service"
     * EACH COUNTDOWN GETS ITS NON REMOVEABLE NOTIFICATION CREATED IN FOREGROUND SERVICE SO NOTIFICATION IS MORE CUSTUMABLE WITH PROGRESSBAR ETC.
     * BUT THE FIRST COUNTDOWN NOTIFICATION HAS TO BE SENT INTO THE STARTFOREGROUND() CALL
     */
    private static final String TAG = "LiveCountdown";
    private SparseArray<Countdown> loadedCountdownsForLiveCountdown;
    private CustomNotification customNotificationMgr;
    private InAppPurchaseMgr inAppPurchaseMgr;
    public static Thread refreshAllNotificationCounters_Interval_Thread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (refreshAllNotificationCounters_Interval_Thread != null) {
            Log.d(TAG, "onStartCommand: Thread is not null. Killing it to provide restart of service.");
            refreshAllNotificationCounters_Interval_Thread.interrupt();
            refreshAllNotificationCounters_Interval_Thread = null;
        }
        //set everything null
        this.setLoadedCountdownsForLiveCountdown(null);
        this.setCustomNotificationMgr(null);
        this.setInAppPurchaseMgr(null);


        //Notification Manager in shouldThisServiceBeKilled() NEEDED! (NullPointerException)
        this.setCustomNotificationMgr(new CustomNotification(this, CountdownActivity.class, (NotificationManager) getSystemService(NOTIFICATION_SERVICE)));

        //normally inapp purchase mgr context should be an activity, but as long as we do not try to launch purchases we will not get an error!
        this.setInAppPurchaseMgr(new InAppPurchaseMgr(this));

        shouldThisServiceBeKilled(intent); //third function call should be this!! (because service gets killed with startService = goodPractice

        //Only do when null at first, because notifications would not be removed when expired! (so we will have to restart whole service for new countdowns)
        this.setLoadedCountdownsForLiveCountdown(DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this, false, false, true)); //false because this service should be also possible when motivateMe is off

        refreshAllNotificationCounters_Interval();

        Log.d(TAG, "Finished onStartCommand().");

        return START_STICKY;
    }

    private void refreshAllNotificationCounters_Interval() {
        Log.d(TAG, "refreshALlNotificationCounters_Interval: Started method.");
        refreshAllNotificationCounters_Interval_Thread = new Thread(new Runnable() {
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
                        break;
                    }
                }
            }
        });
        refreshAllNotificationCounters_Interval_Thread.start();
    }

    private void startRefreshAllNotificationCounters() {
        int foregroundNotificationCount = 0;

        for (int i = 0; i<this.getLoadedCountdownsForLiveCountdown().size(); i++) {
            final Countdown currCountdown = this.getLoadedCountdownsForLiveCountdown().valueAt(i); //because i cannot be final!
            Log.d(TAG, "startRefreshAllNotificationCounters: Found entry: " + this.getLoadedCountdownsForLiveCountdown().keyAt(i));
            //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]
            //only show if setting set for that countdown
            //NO FURTHER VALIDATION NECESSARY [untilStartDateTime Value constraints AND onlyLiveCountdowns are all validated in getAllCountdowns]
            int foregroundServiceNotificationId = Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID + currCountdown.getCountdownId();
            Log.d(TAG, "startRefreshAllNotificationCounters: foregroundServiceNotification-Id: " + foregroundServiceNotificationId + " (foregroundCount: " + foregroundNotificationCount + ")");
            if ((foregroundNotificationCount++) <= 0) {
                //only make foreground notification for first countdown, others just get a non-removable notification
                startForeground(foregroundServiceNotificationId, customNotificationMgr.createCounterServiceNotification(currCountdown));//customNotificationMgr.getNotifications().get((long) foregroundServiceNotificationId).build());
            } else {
                //only do this if more than one node-package bought! (better realize in getLoadedCountdowns() --> harder to implement so maybe better here in service)
                //non-removable notifications
                this.getInAppPurchaseMgr().executeIfProductIsBought(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "startRefreshAllNotificationCounters: UseMoreCountdownNodes-Package bought. Loaded more live countdowns.");
                        customNotificationMgr.getmNotifyMgr().notify(Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID + currCountdown.getCountdownId(), customNotificationMgr.createCounterServiceNotification(currCountdown));
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "startRefreshAllNotificationCounters: UseMoreCountdownNodes-Package NOT bought. Will not load any more.");
                    }
                });
            }
        }

        //Kill service if NO countdowns should be active
        if (foregroundNotificationCount <= 0) {
            Log.d(TAG, "startRefreshAllNotificationCounters: No live countdown candidate found. Killing myself.");
            killThisService();
        }

        //Do after above lines, because so expired countdowns can be modified/removed in createCounterServiceNotification()
        // --> forceReload, because object cannot be updated by mainThread (because service has it's OWN process and so it's own addressspace!)
        //TODO: maybe forceReload only every 10th loop or so (acc. to battery saving etc.)
        this.setLoadedCountdownsForLiveCountdown(DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this, true,false, true)); //false because this service should be also possible when motivateMe is off
    }

    private void shouldThisServiceBeKilled(Intent intent) {
        if (intent != null) {
            try {
                int stopServiceLabel = intent.getIntExtra(Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE_LABEL, (Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE) * (-1));
                if (Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE == stopServiceLabel) {//*-1 so error value can NEVER equal to correct stopValue
                    Log.d(TAG, "shouldThisServiceBeKilled: Service will be killed: " + stopServiceLabel);
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

        for (int i=0, nsize=this.getLoadedCountdownsForLiveCountdown().size();i<nsize;i++) {
            //false because this service should be also possible when motivateMe is off
            allLiveCountdownNotificationIds.add(Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID + this.getLoadedCountdownsForLiveCountdown().valueAt(i).getCountdownId());
        }

        this.getCustomNotificationMgr().removeNotifications(allLiveCountdownNotificationIds);
    }


    public SparseArray<Countdown> getLoadedCountdownsForLiveCountdown() {
        return loadedCountdownsForLiveCountdown;
    }

    public void setLoadedCountdownsForLiveCountdown(SparseArray<Countdown> loadedCountdownsForLiveCountdown) {
        this.loadedCountdownsForLiveCountdown = loadedCountdownsForLiveCountdown;
    }

    public CustomNotification getCustomNotificationMgr() {
        return customNotificationMgr;
    }

    public void setCustomNotificationMgr(CustomNotification customNotificationMgr) {
        this.customNotificationMgr = customNotificationMgr;
    }

    public InAppPurchaseMgr getInAppPurchaseMgr() {
        return inAppPurchaseMgr;
    }

    public void setInAppPurchaseMgr(InAppPurchaseMgr inAppPurchaseMgr) {
        this.inAppPurchaseMgr = inAppPurchaseMgr;
    }
    //TODO: refresh notifications with BigCountdown_Stirng() from Countdown.class
}
