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
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.manager.NotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppPurchaseMgr;
import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_InAppPurchaseMgr.*;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

import static kevkevin.wsdt.tagueberstehen.classes.services.interfaces.IConstants_LiveCountdown_ForegroundService.NOTIFICATION_ID;

public class LiveCountdown_ForegroundService extends Service {
    /**
     * GENERATES FOR ALL SAVED COUNTDOWN WITH ACTIVATED FOREGROUND SERVICE an own notification "foreground service"
     * EACH COUNTDOWN GETS ITS NON REMOVEABLE NOTIFICATION CREATED IN FOREGROUND SERVICE SO NOTIFICATION IS MORE CUSTUMABLE WITH PROGRESSBAR ETC.
     * BUT THE FIRST COUNTDOWN NOTIFICATION HAS TO BE SENT INTO THE STARTFOREGROUND() CALL
     */
    private static final String TAG = "LiveCountdown";
    private SparseArray<Countdown> loadedCountdownsForLiveCountdown;
    private NotificationMgr notificationMgrMgr;
    private InAppPurchaseMgr inAppPurchaseMgr;
    public static Thread refreshAllThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Notification Manager in shouldThisServiceBeKilled() NEEDED! (NullPointerException)
        this.setNotificationMgrMgr(new NotificationMgr(this, CountdownActivity.class, (NotificationManager) getSystemService(NOTIFICATION_SERVICE)));

        //normally inapp purchase mgr context should be an activity, but as long as we do not try to launch purchases we will not get an error!
        this.setInAppPurchaseMgr(new InAppPurchaseMgr(this));

        //Only do when null at first, because notifications would not be removed when expired! (so we will have to restart whole service for new countdowns)
        this.setLoadedCountdownsForLiveCountdown(DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this, false, false, true)); //false because this service should be also possible when motivateMe is off

        startRefreshAll();

        Log.d(TAG, "Finished onStartCommand().");

        return START_STICKY;
    }

    /** Private and non-static because only livecountdownmgr should do this! Stopping is allowed to everyone. */
    private void startRefreshAll() {
        Log.d(TAG, "refreshALlNotificationCounters_Interval: Started method.");
        refreshAllThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare(); //so Handler in Countdown.class can be created (only necessary for background service)
                while (true) {
                    doRefreshAll(); //refresh all (before first Thread.sleep, so called as only method in onstart()
                    try {
                        //TODO: Make seconds configurable (for battery saving [show only when battery saving ON]
                        //TODO: Better: Make a pause button for akku saving / after click to resume
                        Thread.sleep(1000); //refresh after 1 second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        refreshAllThread.start();
    }

    /** Private and non-static because only livecountdownmgr should do this! Stopping is allowed to everyone. */
    private void doRefreshAll() {
        int foregroundNotificationCount = 0;

        for (int i = 0; i<this.getLoadedCountdownsForLiveCountdown().size(); i++) {
            final Countdown currCountdown = this.getLoadedCountdownsForLiveCountdown().valueAt(i); //because i cannot be final! (valueAt because we want index and not id)
            Log.d(TAG, "startRefreshAll: Found entry: " + this.getLoadedCountdownsForLiveCountdown().keyAt(i));
            //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]
            //only show if setting set for that countdown
            //NO FURTHER VALIDATION NECESSARY [untilStartDateTime Value constraints AND onlyLiveCountdowns are all validated in getAllCountdowns]
            int foregroundServiceNotificationId = NOTIFICATION_ID + currCountdown.getCountdownId();
            Log.d(TAG, "startRefreshAll: foregroundServiceNotification-Id: " + foregroundServiceNotificationId + " (foregroundCount: " + foregroundNotificationCount + ")");
            if ((foregroundNotificationCount++) <= 0) {
                //only make foreground notification for first countdown, others just get a non-removable notification
                startForeground(foregroundServiceNotificationId, notificationMgrMgr.createCounterServiceNotification(currCountdown));//notificationMgrMgr.getNotifications().get((long) foregroundServiceNotificationId).build());
            } else {
                //only do this if more than one node-package bought! (better realize in getLoadedCountdowns() --> harder to implement so maybe better here in service)
                //non-removable notifications
                this.getInAppPurchaseMgr().executeIfProductIsBought(INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "startRefreshAll: UseMoreCountdownNodes-Package bought. Loaded more live countdowns.");
                        //do not use issueNotification at this moment, because we would send a normal notification and not a countdown counter one btw. a null notification, because livecountdowns have another id range
                        notificationMgrMgr.getmNotifyMgr().notify(NOTIFICATION_ID + currCountdown.getCountdownId(), notificationMgrMgr.createCounterServiceNotification(currCountdown));
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "startRefreshAll: UseMoreCountdownNodes-Package NOT bought. Will not load any more.");
                    }
                });
            }
        }

        //Kill service if NO countdowns should be active
        if (foregroundNotificationCount <= 0) {
            Log.d(TAG, "startRefreshAll: No live countdown candidate found. Killing myself.");
            killThisService();
        }

        //Do after above lines, because so expired countdowns can be modified/removed in createCounterServiceNotification()
        // --> forceReload, because object cannot be updated by mainThread (because service has it's OWN process and so it's own addressspace!)
        //Maybe no problem because of Sqlitehelper caching: maybe forceReload only every 10th loop or so (acc. to battery saving etc.)
        this.setLoadedCountdownsForLiveCountdown(DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this, true,false, true)); //false because this service should be also possible when motivateMe is off
    }

    public static void stopRefreshAll() {
        if (refreshAllThread != null) {
            Log.d(TAG, "stopRefreshAll: Thread is not null. Killing it to provide restart of service.");
            refreshAllThread.interrupt();
            refreshAllThread = null;
        }
    }

    public void killThisService() {
        Log.d(TAG, "killThisService: Trying to kill myself.");
        stopRefreshAll();
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
            //Only add here running live countdowns and not expired ones, because we want to show expired msg also when service is off
            Countdown tmpCountdown = this.getLoadedCountdownsForLiveCountdown().valueAt(i);
            if (tmpCountdown.isUntilDateInTheFuture()) { //if in past, then just do not remove it
                allLiveCountdownNotificationIds.add(NOTIFICATION_ID + tmpCountdown.getCountdownId());
            } //Important: We have to ensure that expired countdowns are already removeable!
        }

        this.getNotificationMgrMgr().removeNotifications(allLiveCountdownNotificationIds);
    }


    public SparseArray<Countdown> getLoadedCountdownsForLiveCountdown() {
        return loadedCountdownsForLiveCountdown;
    }

    public void setLoadedCountdownsForLiveCountdown(SparseArray<Countdown> loadedCountdownsForLiveCountdown) {
        this.loadedCountdownsForLiveCountdown = loadedCountdownsForLiveCountdown;
    }

    public NotificationMgr getNotificationMgrMgr() {
        return notificationMgrMgr;
    }

    public void setNotificationMgrMgr(NotificationMgr notificationMgrMgr) {
        this.notificationMgrMgr = notificationMgrMgr;
    }

    public InAppPurchaseMgr getInAppPurchaseMgr() {
        return inAppPurchaseMgr;
    }

    public void setInAppPurchaseMgr(InAppPurchaseMgr inAppPurchaseMgr) {
        this.inAppPurchaseMgr = inAppPurchaseMgr;
    }
    //TODO: refresh notifications with BigCountdown_Stirng() from Countdown.class
}
