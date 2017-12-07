package kevkevin.wsdt.tagueberstehen;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by kevin on 07.12.2017.
 */

public class CountdownService extends IntentService {
    private static String serviceName = "CountdownService";
    private Context targetContext;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *  Used to name the worker thread, important only for debugging.
     */
    public CountdownService(Context targetContext) {
        super(serviceName);
        this.targetContext = targetContext;
    }
    public CountdownService() {
        super(serviceName);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        new CountdownCounterService().execute(intent.getDoubleExtra("TOTAL_SECONDS",0D));
    }

    //IMPORTANT: Use by: new CountdownCounter().execute(Übergabeparameter Long);
    public class CountdownCounterService extends AsyncTask<Double,Double,Double> {
        protected Double totalSeconds = 0D;
        protected Double totalMinutes = 0D;
        protected Double totalHours = 0D;
        protected Double totalDays = 0D;
        protected Double totalWeeks = 0D;
        protected Double totalMonths = 0D;
        protected Double totalYears = 0D;

        //Big countdown parameters
        private Long seconds = 0L; // [0-59]
        private Long minutes = 0L; // [0-59]
        private Long hours = 0L; // [0-23]
        private Long days = 0L; // [0-6]
        private Long weeks = 0L; // [0-51/52]
        private Long months = 0L; // [0-11]
        private Long years = 0L; // [0 - /]

        @Override
        protected Double doInBackground(Double... totalSeconds) {
            this.totalSeconds = totalSeconds[0];

            do {
                if (!isCancelled()) { //examine whether asynctask is stopped so we have to stop the thread manually
                    calculateParams(--this.totalSeconds);
                    publishProgress(this.totalSeconds); //refresh countdown
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e("CountdownCounter", "Thread Sleep interrupted in doInBackground()! ");
                        e.printStackTrace();
                    }
                } else {
                    this.totalSeconds = 0D;
                    Log.d("doInBackground","AsyncTask successfully stopped.");
                }
            } while (this.totalSeconds > 0);

            //publishProgress(count) for calling onProgressUpdate()
            return 0D; //Countdown at the end is always 0 (after that onPostExecute() ist started)
        }

        @Override
        protected void onPostExecute(Double result) {
            //what happens if countdown = 0
            setZeroForAll();
        }

        @Override
        protected void onProgressUpdate(Double... totalSeconds) {
            //values[0] set Progress
            //Call random motivational notifications etc.
            Notification tmp = new Notification(targetContext,Countdown.class,(NotificationManager) getSystemService(NOTIFICATION_SERVICE),0);
            tmp.issueNotification(tmp.createNotification("TEST","SERVICE TRIGGERED",R.drawable.campfire_black));

        }

        protected void calculateParams(Double totalSeconds) {
            if (totalSeconds <= 0) { //just in case additional validation
                setZeroForAll();
            } else {
                this.totalSeconds = totalSeconds;
                this.totalMinutes = totalSeconds / 60;
                this.totalHours = (this.totalMinutes) / 60;
                this.totalDays = (this.totalHours) / 24;
                this.totalWeeks = (this.totalDays) / 7;
                this.totalMonths = (this.totalWeeks) / 30; //pauschal mit 30 Tagen pro Monat gerechnet
                this.totalYears = (this.totalMonths) / 12;

                //Calculation for big countdown
                this.seconds = this.totalSeconds.longValue();
                Log.d("calculateParams","Total seconds: "+this.seconds);
                this.years = this.seconds / (365*24*60*60); this.seconds -= (this.years > 0) ? (365*24*60*60)*this.years : 0; //only subtract if years occurs at least 1 time
                Log.d("calculateParams","Years: "+this.years+" // Left seconds: "+this.seconds);
                this.months = this.seconds / (30*24*60*60); this.seconds -= (this.months > 0) ? (30*24*60*60)*this.months : 0;  // * with months e.g. because there might be more than one month to substract
                Log.d("calculateParams","Months: "+this.months+" // Left seconds: "+this.seconds);
                this.weeks = this.seconds / (7*24*60*60); this.seconds -= (this.weeks > 0) ? (7*24*60*60)*this.weeks : 0;
                Log.d("calculateParams","Weeks: "+this.weeks+" // Left seconds: "+this.seconds);
                this.days = this.seconds / (24*60*60); this.seconds -= (this.days > 0) ? (24*60*60)*this.days : 0;
                Log.d("calculateParams","Days: "+this.days+" // Left seconds: "+this.seconds);
                this.hours = this.seconds / (60*60); this.seconds -= (this.hours > 0) ? (60*60)*this.hours : 0;
                Log.d("calculateParams","Hours: "+this.hours+" // Left seconds: "+this.seconds);
                this.minutes = this.seconds / 60; this.seconds -= (this.minutes > 0) ? (60)*this.minutes : 0;
                Log.d("calculateParams","Minutes: "+this.minutes+" // Left seconds: "+this.seconds);
                //Seconds has the rest!
            }
        }

        private void setZeroForAll() {
            this.totalSeconds = 0D;
            this.totalMinutes = 0D;
            this.totalHours = 0D;
            this.totalDays = 0D;
            this.totalWeeks = 0D;
            this.totalMonths = 0D;
            this.totalYears = 0D;
            this.seconds = 0L;
            this.minutes = 0L;
            this.hours = 0L;
            this.days = 0L;
            this.weeks = 0L;
            this.months = 0L;
            this.years = 0L;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setZeroForAll();
        }
    }
}
