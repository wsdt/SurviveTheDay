package kevkevin.wsdt.tagueberstehen.classes;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.R;

public class CountdownCounter {
    //DO NOT USE ASYNCTASK (not ideal for long running operations and also stops when sth. comes) ++++++++++++++++++++++++++++++++++++++++++++++
    private Activity activityContext;
    private Countdown countdown;
    private Thread countdownCounterThread;
    private static final String TAG = "CountdownCounter";

    //Small countdowns
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

    public CountdownCounter(@NonNull Activity activityContext, @NonNull Countdown countdown) {
        this.setActivityContext(activityContext);
        this.setCountdown(countdown);
    }

    public void runOnUI() { //######## IMPORTANT: Ensure that thread gets interrupted in onDestroy() of an activity ###################
        this.setCountdownCounterThread(new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare(); //necessary for handler
                do {
                    //getTotalSeconds of Countdown included into loop [in getter of totalSeconds] (so interrupting is no problem because always from datetime calculated)
                    calculateParams();
                    Log.d(TAG, "runOnUI: Trying to update UI.");
                    updateUI(false); //false == use real values
                    try {
                        Thread.sleep(Constants.COUNTDOWN_COUNTER.REFRESH_UI_EVERY_X_MS); //a little bit smaller than 1 seconds so always the correct time
                    } catch (InterruptedException e) { //gets thrown when interrupted or/and when updateUI(true) is set!
                        Log.e(TAG, "runOnUI: Thread Sleep interrupted in countdownCounterThread! Exiting thread. This happens e.g. when countdown is 0 or activity called interrupt because it got destroyed/stopped, because we called interrupt().");
                        break;
                    }
                }
                while (getTotalSeconds_SYNCED() > 0); //would be an endless loop (because getTotalSeconds cannot go below 0, but we call interrupt in updateUI())
                updateUI(true); //set all values to zero [because last statement no need to verify isInterrupted() on thread]
                return;
            }
        }));
        this.getCountdownCounterThread().start(); //TODO: ensure that thread runs only on countdownActivity! (not multiple times in background)
    }


    private void updateUI(final boolean setZero) {
        getActivityContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (setZero) {
                    Log.d(TAG, "updateUI: Method called with setZero param! Setting all values to zero.");
                }
                //values[0] set Progress
                //Change CountdownActivity values
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterSeconds)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.2f", getTotalSeconds_SYNCED()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterMinutes)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.4f", getTotalMinutes()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterHours)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.6f", getTotalHours()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterDays)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.8f", getTotalDays()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterWeeks)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.10f", getTotalWeeks()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterMonths)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.12f", getTotalMonths()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterYears)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.15f", getTotalYears()));

                ((TextView) getActivityContext().findViewById(R.id.countdownCounter)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.BIG_COUNTDOWN_ZERO_VALUE :
                        getYears() + ":" +
                                getMonths() + ":" +
                                getWeeks() + ":" +
                                getDays() + ":" +
                                getHours() + ":" +
                                getMinutes() + ":" +
                                getSeconds());
            }
        });
    }

    private void calculateParams() {
        if (this.getTotalSeconds_SYNCED() <= 0) { //just in case additional validation
            Log.d(TAG, "calculateParams: Trying to interrupt countdownCounter-Thread.");
            getCountdownCounterThread().interrupt(); //good practice (validate in run() method)
        } else {
            this.setTotalMinutes(this.getTotalSeconds_SYNCED() / 60);
            this.setTotalHours((this.getTotalMinutes()) / 60);
            this.setTotalDays((this.getTotalHours()) / 24);
            this.setTotalWeeks((this.getTotalDays()) / 7);
            this.setTotalMonths((this.getTotalDays()) / 30); //pauschal mit 30 Tagen pro Monat gerechnet
            this.setTotalYears((this.getTotalMonths()) / 12);

            //Calculation for big countdown
            this.setSeconds(this.getTotalSeconds_SYNCED().longValue());
            Log.d(TAG, "calculateParams: Total seconds: " + this.getSeconds());
            this.setYears(this.getSeconds() / (365 * 24 * 60 * 60));
            this.setSeconds(this.getSeconds() - ((this.getYears() > 0) ? (365 * 24 * 60 * 60) * this.getYears() : 0)); //only subtract if years occurs at least 1 time
            Log.d(TAG, "calculateParams: Years: " + this.getYears() + " // Left seconds: " + this.getSeconds());
            this.setMonths(this.getSeconds() / (30 * 24 * 60 * 60));
            this.setSeconds(this.getSeconds() - ((this.getMonths() > 0) ? (30 * 24 * 60 * 60) * this.getMonths() : 0));  // * with months e.g. because there might be more than one month to substract
            Log.d(TAG, "calculateParams: Months: " + this.getMonths() + " // Left seconds: " + this.getSeconds());
            this.setWeeks(this.getSeconds() / (7 * 24 * 60 * 60));
            this.setSeconds(this.getSeconds() - ((this.getWeeks() > 0) ? (7 * 24 * 60 * 60) * this.getWeeks() : 0));
            Log.d(TAG, "calculateParams: Weeks: " + this.getWeeks() + " // Left seconds: " + this.getSeconds());
            this.setDays(this.getSeconds() / (24 * 60 * 60));
            this.setSeconds(this.getSeconds() - ((this.getDays() > 0) ? (24 * 60 * 60) * this.getDays() : 0));
            Log.d(TAG, "calculateParams: Days: " + this.getDays() + " // Left seconds: " + this.getSeconds());
            this.setHours(this.getSeconds() / (60 * 60));
            this.setSeconds(this.getSeconds() - ((this.getHours() > 0) ? (60 * 60) * this.getHours() : 0));
            Log.d(TAG, "calculateParams: Hours: " + this.getHours() + " // Left seconds: " + this.getSeconds());
            this.setMinutes(this.getSeconds() / 60);
            this.setSeconds(this.getSeconds() - ((this.getMinutes() > 0) ? (60) * this.getMinutes() : 0));
            Log.d(TAG, "calculateParams: Minutes: " + this.getMinutes() + " // Left seconds: " + this.getSeconds());
            //Seconds has the rest!
        }
    }


    //GETTER/SETTER ###########################################################
    public Activity getActivityContext() {
        return activityContext;
    }

    public void setActivityContext(Activity activityContext) {
        this.activityContext = activityContext;
    }

    public Countdown getCountdown() {
        return countdown;
    }

    public void setCountdown(Countdown countdown) {
        this.countdown = countdown;
    }

    public Double getTotalSeconds_SYNCED() {
        return this.getCountdown().getTotalSeconds(); //always return correct time
    }


    public Double getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Double totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public Double getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Double totalDays) {
        this.totalDays = totalDays;
    }

    public Double getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(Double totalWeeks) {
        this.totalWeeks = totalWeeks;
    }

    public Double getTotalMonths() {
        return totalMonths;
    }

    public void setTotalMonths(Double totalMonths) {
        this.totalMonths = totalMonths;
    }

    public Double getTotalYears() {
        return totalYears;
    }

    public void setTotalYears(Double totalYears) {
        this.totalYears = totalYears;
    }

    public Long getSeconds() {
        return seconds;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }

    public Long getMinutes() {
        return minutes;
    }

    public void setMinutes(Long minutes) {
        this.minutes = minutes;
    }

    public Long getHours() {
        return hours;
    }

    public void setHours(Long hours) {
        this.hours = hours;
    }

    public Thread getCountdownCounterThread() {
        return countdownCounterThread;
    }

    public void setCountdownCounterThread(Thread countdownCounterThread) {
        this.countdownCounterThread = countdownCounterThread;
    }

    public Long getDays() {
        return days;
    }

    public void setDays(Long days) {
        this.days = days;
    }

    public Long getWeeks() {
        return weeks;
    }

    public void setWeeks(Long weeks) {
        this.weeks = weeks;
    }

    public Long getMonths() {
        return months;
    }

    public void setMonths(Long months) {
        this.months = months;
    }

    public Long getYears() {
        return years;
    }

    public void setYears(Long years) {
        this.years = years;
    }
}
