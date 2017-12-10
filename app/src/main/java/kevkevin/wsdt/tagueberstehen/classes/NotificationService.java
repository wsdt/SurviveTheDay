package kevkevin.wsdt.tagueberstehen.classes;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;


public class NotificationService extends Service {
    private Timer timer;
    private TimerTask timerTask;
    private int intervalSeconds = 5; //default
    private final Handler handler = new Handler(); //use of handler to be able to run in our TimerTask
    private static final String TAG = "NotificationService";
    private Notification notificationManager;
    public static Integer lastStartId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Executed onStartCommand().");
        super.onStartCommand(intent, flags, startId);

        //Ensure that only one service instance is running at the same time - Start
        /*if (NotificationService.lastStartId != null && NotificationService.lastCountdownId != null) {
            if (NotificationService.lastCountdownId == (-1)) {

            }
            stopSelf(NotificationService.lastStartId); //stop last service
        }*/
        //TODO: stop Service with stopSelf(int); --> But not just the last one (the last service instance with the same countdown id has to be closed!)

        NotificationService.lastStartId = startId; //save this service id, so we can stop it before
        //Ensure that only one service instance is running at the same time - End

        //Set Notification Manager for Countdown
        this.notificationManager = new Notification(this,CountdownActivity.class, (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE),0); //intent.getParcelableExtra("notificationManager");
        startTimer();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"Executed onCreate().");
        //super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Executed onDestroy().");
        stopTimer();
        super.onDestroy();
    }

    public void startTimer() {
        Log.d(TAG, "Executed startTimer().");
        //set a new timer
        this.timer = new Timer();

        //initialize TimerTask's job
        initializeTimer();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        this.timer.schedule(timerTask, 5000, this.intervalSeconds*1000);
    }

    public void stopTimer() {
        Log.d(TAG, "Executed stopTimer().");
        //stop timer, if it's not already null
        if (this.timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimer() {
        Log.d(TAG, "Executed initializeTimer().");
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: call notification function
                        notificationManager.issueNotification(notificationManager.createNotification("ServiceTest","SUCCESSFULL TEST", R.drawable.campfire_white));
                    }
                });
            }
        };
    }

    //NO GETTER / SETTER useful, because startService only allows putExtra
}
