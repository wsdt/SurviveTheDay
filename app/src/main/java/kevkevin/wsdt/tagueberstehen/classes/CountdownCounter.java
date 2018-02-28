package kevkevin.wsdt.tagueberstehen.classes;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;

public class CountdownCounter {
    //DO NOT USE ASYNCTASK (not ideal for long running operations and also stops when sth. comes) ++++++++++++++++++++++++++++++++++++++++++++++
    private CountdownActivity activityContext;
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
    private String bigCountdownStr = Constants.COUNTDOWN_COUNTER.BIG_COUNTDOWN_ZERO_VALUE;
    /*private Long seconds = 0L; // [0-59]
    private Long minutes = 0L; // [0-59]
    private Long hours = 0L; // [0-23]
    private Long days = 0L; // [0-6]
    private Long weeks = 0L; // [0-51/52]
    private Long months = 0L; // [0-11]
    private Long years = 0L; // [0 - /]*/

    public CountdownCounter(@NonNull CountdownActivity activityContext, @NonNull Countdown countdown) {
        this.setActivityContext(activityContext);
        this.setCountdown(countdown);
    }

    // COUNTDOWN DATA (random quote, etc.) -> everything that needs regular refresh like random quotes
    private int automaticRefreshBuffer = 0;
    private void automaticRefreshRandomQuote() { //gets called in updateUI() so it's handled on the Mainthread!
        if ((automaticRefreshBuffer++) > Constants.COUNTDOWN_COUNTER.REFRESH_RANDOM_QUOTE_MULTIPLIKATOR && CountdownActivity.runGeneratingRandomQuotes) { //with this procedure we can handle this action in the same additional thread
            automaticRefreshBuffer = 0; //reset buffer
            this.getActivityContext().setNewRandomQuote(null); //method gets only called acc. buffer AND if swipeLayout is showing surfaceview (=quote view)
        }
    }


    // COUNTDOWN ITSELF -------------------------------------------------------------------------------------------------
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
        this.getCountdownCounterThread().start();
    }


    private void updateUI(final boolean setZero) {
        getActivityContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (setZero) {
                    Log.d(TAG, "updateUI: Method called with setZero param! Setting all values to zero.");
                    //TODO: Maybe dialog with share us or similar
                }
                //Set value for progressbar
                ((ProgressBar) getActivityContext().findViewById(R.id.countdownProgressBar)).setProgress((setZero) ? Constants.COUNTDOWN_COUNTER.PROGRESS_ZERO_VALUE : (int) countdown.getRemainingPercentage(2, false));

                //values[0] set Progress
                //Change CountdownActivity values
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterSeconds)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.2f", getTotalSeconds_SYNCED()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterMinutes)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.4f", getTotalMinutes()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterHours)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.6f", getTotalHours()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterDays)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.8f", getTotalDays()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterWeeks)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.10f", getTotalWeeks()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterMonths)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.12f", getTotalMonths()));
                ((TextView) getActivityContext().findViewById(R.id.countdownCounterYears)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.TOTAL_TIMEUNIT_ZERO_VALUE : String.format(Constants.GLOBAL.LOCALE,"%.15f", getTotalYears()));

                ((TextView) getActivityContext().findViewById(R.id.countdownCounter)).setText((setZero) ? Constants.COUNTDOWN_COUNTER.BIG_COUNTDOWN_ZERO_VALUE : getBigCountdownStr()); //show previous no only, if not zero!

                automaticRefreshRandomQuote(); //refresh random quote after updating countdown
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
            this.setBigCountdownStr(craftBigCountdownString(this.getTotalSeconds_SYNCED().longValue()));
        }
    }

    //Also used by liveCountdown in notification!
    public static String craftBigCountdownString(long totalSeconds) {
        //IMPORTANT: Lieber so extra nochmal rechnen (zwar mehr code, aber weniger abarbeitung
        Log.d("craftBigCountdownString", "Total seconds: " + totalSeconds);
        Long years = totalSeconds / (365 * 24 * 60 * 60);
        totalSeconds -= (years > 0) ? (365 * 24 * 60 * 60) * years : 0; //only subtract if years occurs at least 1 time
        Log.d("craftBigCountdownString", "Years: " + years + " // Left seconds: " + totalSeconds);
        Long months = totalSeconds / (30 * 24 * 60 * 60);
        totalSeconds -= (months > 0) ? (30 * 24 * 60 * 60) * months : 0;  // * with months e.g. because there might be more than one month to substract
        Log.d("craftBigCountdownString", "Months: " + months + " // Left seconds: " + totalSeconds);
        Long weeks = totalSeconds / (7 * 24 * 60 * 60);
        totalSeconds -= (weeks > 0) ? (7 * 24 * 60 * 60) * weeks : 0;
        Log.d("craftBigCountdownString", "Weeks: " + weeks + " // Left seconds: " + totalSeconds);
        Long days = totalSeconds / (24 * 60 * 60);
        totalSeconds -= (days > 0) ? (24 * 60 * 60) * days : 0;
        Log.d("craftBigCountdownString", "Days: " + days + " // Left seconds: " + totalSeconds);
        Long hours = totalSeconds / (60 * 60);
        totalSeconds -= (hours > 0) ? (60 * 60) * hours : 0;
        Log.d("craftBigCountdownString", "Hours: " + hours + " // Left seconds: " + totalSeconds);
        Long minutes = totalSeconds / 60;
        totalSeconds -= (minutes > 0) ? (60) * minutes : 0;
        Log.d("craftBigCountdownString", "Minutes: " + minutes + " // Left seconds: " + totalSeconds);
        //Seconds has the rest!

        Character separator = ':';
        return new StringBuilder() //only add non-zero values (so vorangestellte nullen entfernt)
                .append((years == 0) ? "" : years).append((years == 0) ? "" : separator)
                .append((months == 0) ? "" : months).append((months == 0) ? "" : separator)
                .append((weeks == 0) ? "" : weeks).append((weeks == 0) ? "" : separator)
                .append((days == 0) ? "" : days).append((days == 0) ? "" : separator)
                .append((hours == 0) ? "" : hours).append((hours == 0) ? "" : separator)
                .append((minutes == 0) ? "" : minutes).append((minutes == 0) ? "" : separator)
                .append(totalSeconds).toString(); //if seconds zero, then return zero
    }


    //GETTER/SETTER ###########################################################
    public CountdownActivity getActivityContext() {
        return activityContext;
    }

    public void setActivityContext(CountdownActivity activityContext) {
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

    public Thread getCountdownCounterThread() {
        return countdownCounterThread;
    }

    public void setCountdownCounterThread(Thread countdownCounterThread) {
        this.countdownCounterThread = countdownCounterThread;
    }

    public String getBigCountdownStr() {
        return bigCountdownStr;
    }

    public void setBigCountdownStr(String bigCountdownStr) {
        this.bigCountdownStr = bigCountdownStr;
    }
}
