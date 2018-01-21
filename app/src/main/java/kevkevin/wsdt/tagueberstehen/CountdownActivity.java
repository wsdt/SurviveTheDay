package kevkevin.wsdt.tagueberstehen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;

public class CountdownActivity extends AppCompatActivity {
    private AsyncTask<Double,Double,Double> countdownCounter;
    private int countdownId = (-1);
    private static final String TAG = "CountdownActivity";
    private Intent lastIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // SHOW FULL PAGE ADD
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        //make it possible to load and prevent stopping ui (also afterwards: because coutndown does not refresh!) --> esp. when fullpage from other activities opens to slow and gets closed in the countdownactivity
        //IMPORTANT: Do not place here fullpage ad because this blocks the countdown!
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.content_main));
        //adManager.loadFullPageAd(null, null);


        //Notifications regularly: How long do you need to work today or similar and easy type in maybe in notification bar!
        //motivational notifications: just 2 hours to go! etc.
        //Drink a glass of water and the day goes by faster etc.

        //ACTIVITY OPENED BY OTHER ACTIVITY: ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        this.setLastIntent(getIntent());
        this.countdownId = this.getLastIntent().getIntExtra("COUNTDOWN_ID",-1);
        //maybe by main menu or notification, but we get the same Extra: COUNTDOWN_ID with the ID
        startCountdownService(this.countdownId); //0 is default value

        //Wait until views are drawn (for size etc.)
        final RelativeLayout inAppNotification = ((RelativeLayout) findViewById(R.id.notificationContent));
        final ViewTreeObserver observer = inAppNotification.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                showInAppNotificationIfAvailable();
                inAppNotification.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }


    public Double loadCountdownFromSharedPreferences(int countdownId) {
        return new InternalCountdownStorageMgr(this).getCountdown(countdownId).getTotalSeconds();
    }

    public void startCountdownService(int countdownId) {
        try {
            //search in storage and get total seconds then start countdown (if not found because smaller 0 or deleted and notification referenced it
            this.countdownCounter = new CountdownCounter().execute(loadCountdownFromSharedPreferences(countdownId));
        } catch (NullPointerException e) {
            //else everything is implicit 0!
            Toast.makeText(this,"Countdown not found :/",Toast.LENGTH_LONG).show();
            Log.e(TAG,"CountdownActivity(): Countdown not found. ID: "+countdownId);
            e.printStackTrace();
        }
    }

    public Intent getLastIntent() {
        return lastIntent;
    }

    public void setLastIntent(Intent lastIntent) {
        this.lastIntent = lastIntent;
    }

    //TODO: REPLACEMENT FOR ASYNCTASK (memory leaks etc.) --> best maybe in complete own class (and there with StringBuilder() and runOnUiThread() and normal thread etc. and give activity context there to findTextViews
    /*private void countdownCounter_NEW(Double totalseconds) {
        Thread calculateCountdown = new Thread(new Runnable() {
            @Override
            public void run() {


                //Send to view
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.countdownCounterSeconds)).setText(String.format("%.2f", totalSeconds));
                        ((TextView) findViewById(R.id.countdownCounterMinutes)).setText(String.format("%.4f", totalMinutes));
                        ((TextView) findViewById(R.id.countdownCounterHours)).setText(String.format("%.6f", totalHours));
                        ((TextView) findViewById(R.id.countdownCounterDays)).setText(String.format("%.8f", totalDays));
                        ((TextView) findViewById(R.id.countdownCounterWeeks)).setText(String.format("%.10f", totalWeeks));
                        ((TextView) findViewById(R.id.countdownCounterMonths)).setText(String.format("%.12f", totalMonths));
                        ((TextView) findViewById(R.id.countdownCounterYears)).setText(String.format("%.15f", totalYears));

                        ((TextView) findViewById(R.id.countdownCounter)).setText(new StringBuilder()
                                .append(years).append(":")
                                .append(months).append(":")
                                .append(weeks).append(":")
                                .append(days).append(":")
                                .append(hours).append(":")
                                .append(minutes).append(":")
                                .append(seconds));
                    }
                });
            }
        });
        calculateCountdown.start();


    }*/



    //IMPORTANT: Use by: new CountdownCounter().execute(Übergabeparameter Long);
    private class CountdownCounter extends AsyncTask<Double,Double,Double> {
        //public Context countdownContext;
        private Double totalSeconds = 0D;
        private Double totalMinutes = 0D;
        private Double totalHours = 0D;
        private Double totalDays = 0D;
        private Double totalWeeks = 0D;
        private Double totalMonths = 0D;
        private Double totalYears = 0D;

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
            return 0D; //CountdownActivity at the end is always 0 (after that onPostExecute() ist started)
        }

        @Override
        protected void onPostExecute(Double result) {
            //what happens if countdown = 0
            setZeroForAll();
        }

        @Override
        protected void onProgressUpdate(Double... totalSeconds) {
            //values[0] set Progress
                //Change CountdownActivity values
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
                this.totalMonths = (this.totalDays) / 30; //pauschal mit 30 Tagen pro Monat gerechnet
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

    /*@Override
    protected void onResume() {
        super.onResume();
        //Restart asynctask (otherwise it would be stopped by onPause(), but stop it in onPause because when activity gets in Background!
        if (this.countdownId >= 0) {
            startCountdownService(this.countdownId);
        } else {
            Log.e(TAG, "onResume: CountdownId negative. Maybe onResume called before countdown loaded or countdown could not be loaded.");
        }
    }*/

    @Override
    protected void onPause() { //no onstop necessary because it comes after pause
        super.onPause();
        try {
            countdownCounter.cancel(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "onPause: NullpointerException while cancelling asynctask.");
            e.printStackTrace();
        }
    }


    // ################################################################################################################
    // NOTIFICATION-CONTENT SHOWER ####################################################################################
    // ################################################################################################################

    //Show in-app-notification (incl. animation)
    private void showInAppNotificationIfAvailable() {
        Log.d(TAG, "showInAppNotificationIfAvailable: Started method.");
        try {
            //TODO: not always correct notification is shown (older ones)
            //TODO: Only show green bg and animation if quote/intent found
            final RelativeLayout notificationContent = (RelativeLayout) findViewById(R.id.notificationContent);
            notificationContent.setY(notificationContent.getHeight()*(-1)); //assign height*-1 so notification will get exactly behind display
            final int hiddenPosition = (notificationContent.getHeight()*(-1)); //TODO: extra long quotes do not get hidden completely
            Log.d(TAG, "showInAppNotificationIfAvailable: Tried to positionate inapp-notification outside screen: "+hiddenPosition);

            final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(notificationContent, "y", 0); //get it back to positive animated
            objectAnimator.setDuration(1500); //1,5 seconds
            /*objectAnimator.setRepeatCount(1); //show it and hide it after duration expired [making this with count var and restarting animation in onAnimationEnd()]
            objectAnimator.setRepeatMode(ValueAnimator.REVERSE);*/
            objectAnimator.addListener(new Animator.AnimatorListener() {
                int count = 0;

                @Override
                public void onAnimationStart(Animator animator) {
                    Log.d(TAG, "onAnimationStart: Inapp notification animation started.");
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (count++ < 1) { //create new object animator because we want not the same animation but reverse
                        ObjectAnimator objectAnimatorReverse = ObjectAnimator.ofFloat(notificationContent, "y", hiddenPosition); //get it back to positive animated
                        objectAnimatorReverse.setDuration(1500);
                        objectAnimatorReverse.setStartDelay(5500); //how long should be notification displayed
                        objectAnimatorReverse.start(); //start again
                        Log.d(TAG, "onAnimationEnd: Inapp notification repeated with delay in onAnimationEnd.");
                    } else {
                        Log.d(TAG, "onAnimationEnd: Inapp notification animation finished.");
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    Log.d(TAG, "onAnimationCancel: Cancelled animation of inappnotification.");
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                    Log.d(TAG, "onAnimationRepeat: Repeated in app notification animation.");
                    //objectAnimator.setStartDelay(3000); //do not use animator (start delay would be ignored) does not work because not called
                }
            });
            objectAnimator.start();

            //Set notification contents
            ((ImageView) findViewById(R.id.notificationImage)).setImageBitmap(BitmapFactory.decodeResource(getResources(), this.getLastIntent().getIntExtra("SMALL_ICON",-1)));
            ((TextView) findViewById(R.id.notificationHeading)).setText(this.getLastIntent().getStringExtra("CONTENT_TITLE"));
            ((TextView) findViewById(R.id.notificationFullText)).setText(this.getLastIntent().getStringExtra("CONTENT_TEXT"));
            Log.d(TAG, "showInAppNotificationIfAvailable: Tried to assign values to view. ");


        } catch (Exception e) {
            Log.e(TAG, "showInApNotificationIfAvailable: Could not show inapp-notification. Maybe activity was opened from main menu.");
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setLastIntent(intent); //set last calling intent (because of single top would not call activity again, so maybe always same intent will be shown)
    }
}
