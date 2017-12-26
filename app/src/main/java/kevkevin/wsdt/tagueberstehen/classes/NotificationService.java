package kevkevin.wsdt.tagueberstehen.classes;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;


public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private Notification notificationManager;
    //private static ArrayList<String[]> activeServices; //every String[]: [0]:COUNTDOWNID / [1]:STARTID (every countdown id should only occur once!)
    private InternalStorageMgr storageMgr;
    private ArrayList<Timer> timer; //do not make that static
    private ArrayList<TimerTask> timerTask;
    private ArrayList<Integer> intervalSeconds; // = 5; //default
    private /*final*/ ArrayList<Handler> handler; // = new Handler(); //use of handler to be able to run in our TimerTask
    private int startId;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Executed onStartCommand(). StartId: " + startId);
        super.onStartCommand(intent, flags, startId);


        this.storageMgr = new InternalStorageMgr(this);
        this.startId = startId; //save current service instance in variable

        //Set Notification Manager etc. for Countdown
        this.notificationManager = new Notification(this, CountdownActivity.class, (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE), 0); //intent.getParcelableExtra("notificationManager");
        this.handler = new ArrayList<>();
        this.intervalSeconds = new ArrayList<>();
        this.timer = new ArrayList<>();
        this.timerTask = new ArrayList<>();

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
    }

    public void startTimer() {
        Log.d(TAG, "Executed startTimer().");
        //set a new timer

        //for each countdown do that following
        //iterate through all countdown data sets
        for (Map.Entry<Integer,Countdown> countdown : this.storageMgr.getAllCountdowns(true).entrySet()) {
            Log.d(TAG, "Countdown-Id: " + countdown.getValue().getCountdownId());
            this.timer.add(countdown.getValue().getCountdownId(), new Timer());
            this.intervalSeconds.add(countdown.getValue().getCountdownId(), 5);
            this.handler.add(countdown.getValue().getCountdownId(), new Handler());
            initializeTimer(countdown.getValue().getCountdownId());
            //delay, time after interval starts
            this.timer.get(countdown.getValue().getCountdownId()).schedule(this.timerTask.get(countdown.getValue().getCountdownId()), 5000, this.intervalSeconds.get(countdown.getValue().getCountdownId()) * 1000);
        }
        // END - FOR EACH COUNTDOWN
    }

    public void stopTimer() {
        Log.d(TAG, "Executed stopTimer().");
        //stop timer, if it's not already null for all countdowns!
        if (this.timer != null) {
            for (Timer timerInstance : this.timer) {
                if (timerInstance != null) {
                    timerInstance.cancel();
                    timerInstance = null;
                }
            }
        }
    }

    public void initializeTimer(final int countdownId) {
        Log.d(TAG, "Executed initializeTimer().");
        this.timerTask.add(countdownId, new TimerTask() {
            @Override
            public void run() {
                handler.get(countdownId).post(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: call notification function
                        notificationManager.issueNotification(notificationManager.createNotification("ServiceTest", "SUCCESSFULL TEST", R.drawable.campfire_white));
                    }

                });
            }
        });
    }

    //NO GETTER / SETTER useful, because startService only allows putExtra
}
