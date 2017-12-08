package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Countdown extends AppCompatActivity {
    private EditText countdownAdd;
    private RelativeLayout contentMain;
    private AsyncTask<Double,Double,Double> countdownCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get main content view
        contentMain = (RelativeLayout) findViewById(R.id.content_main);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Currently this option is disabled.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //Start Service
        SchedulePersistCountdown countdownManager = new SchedulePersistCountdown(getSharedPreferences("COUNTDOWN_"+0,MODE_PRIVATE));

        /*Intent service = new Intent(getBaseContext(),old_CountdownService.class);
        service.putExtra("TOTAL_SECONDS",60D);
        startService(service);*/



        //Notification tmp = new Notification(this,Countdown.class,(NotificationManager) getSystemService(NOTIFICATION_SERVICE),0);
        //tmp.issueNotification(tmp.createNotification("TEST","TESTTEXT",R.drawable.campfire_red));

        //Notifications regularly: How long do you need to work today or similar and easy type in maybe in notification bar!
        //motivational notifications: just 2 hours to go! etc.
        //Drink a glass of water and the day goes by faster etc.

        //ACTIVITY OPENED BY OTHER ACTIVITY: ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        Intent intent = getIntent();
        //maybe by main menu or notification, but we get the same Extra: COUNTDOWN_ID with the ID
        int countdownId = intent.getIntExtra("COUNTDOWN_ID",-1); //0 is default value
        if (countdownId >= 0) {
            //search in storage and get total seconds then start countdown
            countdownCounter = startCountdownService(loadCountdownFromSharedPreferences(countdownId));
        } else {
            //else everything is implicit 0!
            Toast.makeText(this,"Countdown not found :/",Toast.LENGTH_LONG).show();
            Log.e("getIntentCountdownId","Countdown not found. ID: "+countdownId);
        }
    }

    public Double loadCountdownFromSharedPreferences(int countdownId) {
        Double totalSeconds = 0D; //intial value
        //TODO: search in storage and return Date and Timestamp --> calculate totalseconds for current session! (because what if app stopps!)

        if (countdownId == 0) {
            totalSeconds = 135D;
        }

        return totalSeconds;
    }

    public AsyncTask<Double,Double,Double> startCountdownService(Double totalSeconds) {
        return new CountdownCounter().execute(totalSeconds);
    }

    //IMPORTANT: Use by: new CountdownCounter().execute(Übergabeparameter Long);
    private class CountdownCounter extends AsyncTask<Double,Double,Double> {
        //public Context countdownContext;
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

        //private Double serviceOrActivity = 0D;

        @Override
        protected Double doInBackground(Double... totalSeconds) {
            this.totalSeconds = totalSeconds[0];
            //this.serviceOrActivity = (totalSeconds[1] != 0D) ? 1D : 0D; //0 for loading into UI / 1 for service

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
                //Change Countdown values
                ((TextView) findViewById(R.id.countdownCounterSeconds)).setText(String.format("%.2f", this.totalSeconds));
                ((TextView) findViewById(R.id.countdownCounterMinutes)).setText(String.format("%.4f", this.totalMinutes));
                ((TextView) findViewById(R.id.countdownCounterHours)).setText(String.format("%.6f", this.totalHours));
                ((TextView) findViewById(R.id.countdownCounterDays)).setText(String.format("%.8f", this.totalDays));
                ((TextView) findViewById(R.id.countdownCounterWeeks)).setText(String.format("%.10f", this.totalWeeks));
                ((TextView) findViewById(R.id.countdownCounterMonths)).setText(String.format("%.12f", this.totalMonths));
                ((TextView) findViewById(R.id.countdownCounterYears)).setText(String.format("%.15f", this.totalYears));

                ((TextView) findViewById(R.id.countdownCounter)).setText(
                        this.years + ":" +
                                this.months + ":" +
                                this.weeks + ":" +
                                this.days + ":" +
                                this.hours + ":" +
                                this.minutes + ":" +
                                this.seconds);
             /* } else {
                //service
                Notification tmp = new Notification(this,Countdown.class,(NotificationManager) getSystemService(NOTIFICATION_SERVICE),0);
                tmp.issueNotification(tmp.createNotification("TEST","TESTTEXT",R.drawable.campfire_red));
            }*/
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


    @Override
    protected void onPause() { //no onstop necessary because it comes after pause
        super.onPause();
        countdownCounter.cancel(true);
    }
}
