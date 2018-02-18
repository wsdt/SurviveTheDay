package kevkevin.wsdt.tagueberstehen.classes.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.LoadingScreenActivity;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;

import static android.content.Context.NOTIFICATION_SERVICE;


/** This broadcast receiver gets called when smartphone is turned on AND when created countdown's startdate is not longer in the future (so
 * we would not have to open the app to start all services) */
public class Kickstarter_BootAndGeneralReceiver extends BroadcastReceiver {
    private static final String TAG = "KickstarterReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals(Constants.KICKSTARTER_BOOTANDGENERALRECEIVER.BROADCASTRECEIVER_ACTION_RESTART_ALL_SERVICES)) {
                    //first action gets invoked when smartphone gets started
                    //second action gets invoked when a newly saved or edited countdown has a startdate in future. If startdate expires then this broadcast receiver will be called

                    //that method restarts all countdowns (so their broadcast receiver event gets restarted).
                    new InternalCountdownStorageMgr(context).restartNotificationService();
                    Log.d(TAG, "onReceive: Tried to restart backgroundservice/broadcast receivers or/and live countdowns!");
                } else {
                    Log.w(TAG, "onReceive: Unknown broadcast receiver action. Did nothing: "+action);
                }
            } else {
                Log.e(TAG, "onReceive: Action is null! Did nothing. :(");
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: Could not validate broadcast action.");
            e.printStackTrace();
        }
    }


    /* TODO: Check on playstore for app update and send notification on boot (only)
    private class GetVersionCode extends AsyncTask<Void, String, String> {
    @Override
    protected String doInBackground(Void... voids) {

        String newVersion = null;
        try {
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName() + "&hl=it")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
            return newVersion;
        } catch (Exception e) {
            return newVersion;
        }
    }

    @Override
    protected void onPostExecute(String onlineVersion) {
        super.onPostExecute(onlineVersion);
        if (onlineVersion != null && !onlineVersion.isEmpty()) {
            if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
                //show dialog
            }
        }
        Log.d("update", "Current version " + currentVersion + "playstore version " + onlineVersion);
    }
    */
}
