package kevkevin.wsdt.tagueberstehen;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText countdownAdd;
    private RelativeLayout contentMain;
    private Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get main content view
        contentMain = (RelativeLayout) findViewById(R.id.content_main);
        thisContext = this; //set context globally

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Currently you only can add specific countdowns!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new CountdownCounter().execute(120D);
    }


    //IMPORTANT: Use by: new CountdownCounter().execute(Übergabeparameter Long);
    public class CountdownCounter extends AsyncTask<Double,Double,Double> {
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
                calculateParams(--this.totalSeconds);
                publishProgress(this.totalSeconds); //refresh countdown
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("CountdownCounter","Thread Sleep interrupted in doInBackground()!");
                    e.printStackTrace();
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
            ((TextView) findViewById(R.id.countdownCounterSeconds)).setText(String.format("%.2f",this.totalSeconds));
            ((TextView) findViewById(R.id.countdownCounterMinutes)).setText(String.format("%.4f",this.totalMinutes));
            ((TextView) findViewById(R.id.countdownCounterHours)).setText(String.format("%.6f",this.totalHours));
            ((TextView) findViewById(R.id.countdownCounterDays)).setText(String.format("%.8f",this.totalDays));
            ((TextView) findViewById(R.id.countdownCounterWeeks)).setText(String.format("%.10f",this.totalWeeks));
            ((TextView) findViewById(R.id.countdownCounterMonths)).setText(String.format("%.12f",this.totalMonths));
            ((TextView) findViewById(R.id.countdownCounterYears)).setText(String.format("%.15f",this.totalYears));

            ((TextView) findViewById(R.id.countdownCounter)).setText(
                    this.years+":"+
                    this.months+":"+
                    this.weeks+":"+
                    this.days+":"+
                    this.hours+":"+
                    this.minutes+":"+
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
                //TODO: big countdown seconds one too much (do not just decrement because then you get -1 sometimes!)
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

    }
}
