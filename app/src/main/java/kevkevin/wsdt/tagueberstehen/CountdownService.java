package kevkevin.wsdt.tagueberstehen;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class CountdownService extends Service {
    private Intent callingActivity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CountdownService","Service started.");
        this.setCallingActivity(intent);

        try {
            Log.d("onStartCommand","Tried to start Subservices");
            startSubService(this.getCallingActivity().getDoubleExtra("TOTAL_SECONDS",0D));
        } catch (NullPointerException e) {
            Log.e("CALLING_startSubService","Could not find Intent Extra. Service not started.");
            e.printStackTrace();
        }

        return START_STICKY;
    }


    //TODO: STATT MIT TOTALSECONDS mit ENDZEITPUNKT (DATE/TIME) rechnen und so Hälfte etc ausrechnen (so auch egal wenn Service stoppt) --> Ebenfalls so abspeichern!
    public void startSubService(Double totalSeconds) {
        new CountdownCounterService().execute(totalSeconds);
    }


    private class CountdownCounterService extends AsyncTask<Double,Double,Double> {
        //public Context countdownContext;
        protected Double totalSeconds = 0D;
        protected Double totalMinutes = 0D;
        protected Double totalHours = 0D;
        protected Double totalDays = 0D;
        protected Double totalWeeks = 0D;
        protected Double totalMonths = 0D;
        protected Double totalYears = 0D;

        @Override
        protected Double doInBackground(Double... totalSeconds) {
            this.totalSeconds = totalSeconds[0];

            do {
               // if (!isCancelled()) { //examine whether asynctask is stopped so we have to stop the thread manually
                    calculateParams(--this.totalSeconds);
                    publishProgress(this.totalSeconds); //refresh countdown
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e("CountdownService", "Thread Sleep interrupted in doInBackground()! ");
                        e.printStackTrace();
                    }
                /*} else {
                    this.totalSeconds = 0D;
                    Log.d("CountdownService","AsyncTask successfully stopped.");
                }*/
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

            //service
            Notification tmp = new Notification(getApplicationContext(),Countdown.class,(NotificationManager) getSystemService(NOTIFICATION_SERVICE),0);
            tmp.issueNotification(tmp.createNotification("TEST"+this.totalSeconds,"TESTTEXT",R.drawable.campfire_black));

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
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setZeroForAll();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CountdownService","Service exited.");
    }

    // GETTER/SETTER -----------------------------------
    public Intent getCallingActivity() {
        return callingActivity;
    }

    public void setCallingActivity(Intent callingActivity) {
        this.callingActivity = callingActivity;
    }
}
