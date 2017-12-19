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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;


public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private Notification notificationManager;
    //private static ArrayList<String[]> activeServices; //every String[]: [0]:COUNTDOWNID / [1]:STARTID (every countdown id should only occur once!)
    private int thisService; //contains the serviceStartId
    private static SharedPreferences activeServices; //eigenes public sharedpreferences für nur active countdowns (foreach string)
    private ArrayList<Timer> timer; //do not make that static
    private ArrayList<TimerTask> timerTask;
    private ArrayList<Integer> intervalSeconds; // = 5; //default
    private /*final*/ ArrayList<Handler> handler; // = new Handler(); //use of handler to be able to run in our TimerTask
    private ArrayList<Countdown> activeCountdownObjs;
    private final String nameSharedPreferences = "ACTIVE_COUNTDOWNS";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isServiceForThatCountdownRunning(int countdownId) {
        boolean isServiceRunning = false; //being optimistic, to do anything in case of failure

        try {
            String[] strarrCountdownIds = (getActiveServices().getString(nameSharedPreferences, "")).split(";");
            for (String strCountdownId : strarrCountdownIds) {
                if (Integer.parseInt(strCountdownId) == countdownId) {
                    isServiceRunning = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"SharedPreferences of Countdowns could not be fetched/parsed. Maybe there is NO countdown active.");
            e.printStackTrace();
        }

        //service seems not to run, also when error occured then maybe NO services are running
        return isServiceRunning;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Executed onStartCommand(). StartId: " + startId);
        super.onStartCommand(intent, flags, startId);

        //Ensure that sharedPreferences is initialized
        if (NotificationService.getActiveServices() == null) {
            NotificationService.setActiveServices(getSharedPreferences("COUNTDOWNS", MODE_PRIVATE)); /*new ArrayList<String[]>()*/
        }

        //Define instance identifiers / startId is Übergabewert
        if (intent != null) {
            //Ignore 'new' service countdown sharedpreferences instance, when intent not null (which means it got explicitely called)
            int countdownId = intent.getIntExtra("COUNTDOWN_ID", -1);
            if (countdownId < 0) {
                Log.d(TAG, "Could not fetch Countdown id from intent: "+countdownId);
            } else if (isServiceForThatCountdownRunning(countdownId)) {
                Log.d(TAG, "This countdown already runs in service and will be ignored. Countdown-ID: " + countdownId + " / StartId: " + startId);
            } else {
                //Add countdown id to sharedPreferences
                SharedPreferences.Editor editor = NotificationService.getActiveServices().edit();
                //WARNING: Countdown IDs not ordered! At least do not assume it.
                editor.putString(nameSharedPreferences,NotificationService.getActiveServices().getString(nameSharedPreferences,"")+";"+countdownId);
                editor.apply();
                Log.d(TAG, "Tried to add new CountdownId to ActiveCountdown SharedPreferences.");
            }
        }


        this.thisService = startId; //save current service instance in variable

        //Set Notification Manager etc. for Countdown
        this.notificationManager = new Notification(this, CountdownActivity.class, (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE), 0); //intent.getParcelableExtra("notificationManager");
        this.handler = new ArrayList<>();
        this.intervalSeconds = new ArrayList<>();
        this.timer = new ArrayList<>();
        this.timerTask = new ArrayList<>();
        this.activeCountdownObjs = new ArrayList<>();

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
        for (String countdownId : getActiveServices().getString(nameSharedPreferences,"").split(";")) {
            if (!countdownId.equals("")) {
                Log.d(TAG,"Countdown-Id: "+countdownId);
                int indexCountdownId = Integer.parseInt(countdownId);
                this.timer.add(indexCountdownId, new Timer());
                this.intervalSeconds.add(indexCountdownId, 5);
                this.handler.add(indexCountdownId, new Handler());
                initializeTimer(indexCountdownId);
                //delay, time after interval starts
                this.timer.get(indexCountdownId).schedule(this.timerTask.get(indexCountdownId), 5000, this.intervalSeconds.get(indexCountdownId) * 1000);
            } else {
                Log.e(TAG,"Could not fetch countdownId: "+countdownId);
            }
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
        this.timerTask.add(countdownId,new TimerTask() {
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
    public static SharedPreferences getActiveServices() {
        return activeServices;
    }

    public static void setActiveServices(SharedPreferences activeServices) {
        NotificationService.activeServices = activeServices;
    }
}
