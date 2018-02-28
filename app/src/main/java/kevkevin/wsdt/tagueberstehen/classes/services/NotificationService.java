package kevkevin.wsdt.tagueberstehen.classes.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.DatabaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;


public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private CustomNotification customNotificationManager;
    //private static ArrayList<String[]> activeServices; //every String[]: [0]:COUNTDOWNID / [1]:STARTID (every countdown id should only occur once!)
    private int startId;
    private SparseArray<Countdown> allCountdowns;
    private InAppPurchaseManager inAppPurchaseManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("UseSparseArrays")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Executed onStartCommand(). StartId: " + startId);
        super.onStartCommand(intent, flags, startId);

        this.allCountdowns = DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this,false,true, false); //call this line AFTER assignment of internal storage mgr (otherwise nullpointerexc!)

        //Warning context is NOT an activity, so do NOT launch purchase workflows or similar! (class cast exception)
        this.inAppPurchaseManager = new InAppPurchaseManager(this);

        this.startId = startId; //save current service instance in variable

        //Set CustomNotification Manager etc. for Countdown
        this.customNotificationManager = new CustomNotification(this, CountdownActivity.class, (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE)); //intent.getParcelableExtra("customNotificationManager");

        startTimer(); //this function starts all countdowns

        //So it can be stopped from outside
        (new GlobalAppSettingsMgr(this)).setBackgroundServicePid(android.os.Process.myPid());
        Log.d(TAG, "onStartCommand: Current process pid: "+android.os.Process.myPid());

        return START_STICKY; //START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Executed onCreate().");
        //super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Executed onDestroy().");
        stopTimer();
        super.onDestroy();
        killNotificationServiceUngracefully();
    }

    public void killNotificationServiceUngracefully() {
        Log.d(TAG, "killNotificationServiceUngracefully: Tried to kill service ungracefully.");
        android.os.Process.killProcess(android.os.Process.myPid()); //Important: Otherwise service would not stop because sth will still run in this instance
    }

    public void startTimer() {
        Log.d(TAG, "Executed startTimer().");
        //set a new timer

        //for each countdown do that following
        //iterate through all countdown data sets
        int count = 0;
        // only start more than one if more nodes package is bought
        for (int i = 0;i<allCountdowns.size();i++) {
            final Countdown currCountdown = allCountdowns.valueAt(i); //because i cannot be final

            Log.d(TAG, "Countdown-Id: " + currCountdown.getCountdownId());
            if ((count++) > 0) { //increment because at least one countdown is there
                this.inAppPurchaseManager.executeIfProductIsBought(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "startTimer: UseMoreCountdownNodes-Package bought. Adding more to service.");
                        initializeTimer(currCountdown);
                        //delay, time after interval starts (random e.g. 0-4 seconds, so multiple countdown timer does not show notification at the same seconds)
                        currCountdown.getTimer().schedule(currCountdown.getTimerTask(),0, currCountdown.getNotificationInterval() * 1000); //*1000 so every second * interval
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "startTimer: UseMoreCountdownNodes-Package NOT bought. Not adding any more.");
                    }
                });
            }

        }
        if (count <= 0) {
            //Kill Service if there is no countdown! Save energy.
            killNotificationServiceUngracefully();
        }
        // END - FOR EACH COUNTDOWN
    }

    public void stopTimer() {
        Log.d(TAG, "Executed stopTimer().");
        //stop timer, if it's not already null for all countdowns!
        for (int i = 0; i<allCountdowns.size();i++) {
            Log.d(TAG, "stopTimer: Tried to stop timerinstance. ");
            if (allCountdowns.valueAt(i).getTimer() != null) {
                allCountdowns.valueAt(i).getTimer().cancel();
                allCountdowns.valueAt(i).setTimer(null);
                Log.d(TAG, "stopTimer: Stopped timerinstance.");
            }
        }
    }

    //############### WARNING: Countdowns might be outdated! (external process, we would need getAllCountdowns(forcereload=true)) #########################
    public void initializeTimer(final Countdown countdown) {
        Log.d(TAG, "Executed initializeTimer().");
        countdown.setTimerTask(new TimerTask() {
            @Override
            public void run() {
                countdown.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //TODO: Stop this timeTask instance when countdown has expired! otherwise it would still run until new stark of service (what will happen surely a few minutes later or similar)
                            if (countdown.isUntilDateInTheFuture()) {
                                customNotificationManager.issueNotification(customNotificationManager.createRandomNotification(countdown));
                            } else {
                                Log.d(TAG, "initializeTimer: Countdown has expired! Tried to stopTimer.");
                                //TODO: does not work
                                countdown.getTimer().cancel();
                                countdown.setTimer(null);
                                Log.d(TAG, "initializeTimer: Stopped timer for specific countdown.");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "initializeTimer: Error occured (see stacktrace below).");
                            e.printStackTrace();
                        }
                    }

                });
            }
        });
    }


    //NO GETTER / SETTER useful, because startService only allows putExtra
}
