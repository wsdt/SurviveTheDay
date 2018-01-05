package kevkevin.wsdt.tagueberstehen.classes;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;
import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;


public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private Notification notificationManager;
    //private static ArrayList<String[]> activeServices; //every String[]: [0]:COUNTDOWNID / [1]:STARTID (every countdown id should only occur once!)
    private InternalStorageMgr storageMgr;
    private int startId;
    private HashMap<Integer, Countdown> allCountdowns;


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

        storageMgr = new InternalStorageMgr(this);
        this.allCountdowns = this.storageMgr.getAllCountdowns(true); //call this line AFTER assignment of internal storage mgr (otherwise nullpointerexc!)

        this.startId = startId; //save current service instance in variable

        //Set Notification Manager etc. for Countdown
        this.notificationManager = new Notification(this, CountdownActivity.class, (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE), 0); //intent.getParcelableExtra("notificationManager");

        startTimer(); //this function starts all countdowns

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
        for (Map.Entry<Integer, Countdown> countdown : this.allCountdowns.entrySet()) {
            Log.d(TAG, "Countdown-Id: " + countdown.getValue().getCountdownId());
            count++; //increment because at least one countdown is there

            initializeTimer(countdown.getValue());
            //delay, time after interval starts (random e.g. 0-4 seconds, so multiple countdown timer does not show notification at the same seconds)
            countdown.getValue().getTimer().schedule(countdown.getValue().getTimerTask(),0, countdown.getValue().getNotificationInterval() * 1000); //*1000 so every second * interval
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
        for (Map.Entry<Integer, Countdown> countdown : this.allCountdowns.entrySet()) {
            Log.d(TAG, "stopTimer: Tried to stop timerinstance. ");
            if (countdown.getValue().getTimer() != null) {
                countdown.getValue().getTimer().cancel();
                countdown.getValue().setTimer(null);
                Log.d(TAG, "stopTimer: Stopped timerinstance.");
            }
        }
    }


    public void initializeTimer(final Countdown countdown) {
        Log.d(TAG, "Executed initializeTimer().");
        countdown.setTimerTask(new TimerTask() {
            @Override
            public void run() {
                countdown.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            notificationManager.issueNotification(notificationManager.createRandomNotification(countdown));
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
