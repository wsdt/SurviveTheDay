package kevkevin.wsdt.tagueberstehen.classes.StorageMgr;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.services.NotificationService;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GlobalAppSettingsMgr {
    private SharedPreferences globalSettings_SharedPref;
    private static final String TAG = "GlobalAppSettingsMgr";
    private Context context;

    public GlobalAppSettingsMgr (@NonNull Context context) {
        this.setContext(context);
        this.setGlobalSettings_SharedPref(context.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE));
    }

    public boolean useForwardCompatibility() {
        /* DEFAULT: true
                If TRUE: Use broadcastReceivers and make Option available for saveBattery
                If FALSE: Use Background service (long intervals might not work)
        */
        return this.getGlobalSettings_SharedPref().getBoolean("USE_FORWARD_COMPATIBILITY", true); //default true, so broadcast receivers used
    }

    public void setUseForwardCompatibility(boolean useForwardCompatibility) {
        this.getGlobalSettings_SharedPref().edit().putBoolean("USE_FORWARD_COMPATIBILITY", useForwardCompatibility).apply();
        Log.d(TAG, "setUseForwardCompatibility: Saved new forwardcompatiblity setting.");
    }

    public boolean saveBattery() {
        /*ONLY when broadcast receiver mode active:
               If TRUE: inexactRepeating() USED [battery saving]
               If FALSE: setRepeating() USED [battery draining]*/

        boolean saveBattery = this.getGlobalSettings_SharedPref().getBoolean("SAVE_BATTERY",false); //default is false, so more precise

        if (!useForwardCompatibility()) {
             Log.d(TAG, "saveBattery: useForwardCompatibility FALSE! Background service is used so this option is redundant!");
        }
        return saveBattery;
    }

    public void setSaveBattery(boolean saveBattery) {
        this.getGlobalSettings_SharedPref().edit().putBoolean("SAVE_BATTERY", saveBattery).apply();
        Log.d(TAG, "setSaveBattery: Saved new saveBattery setting.");
    }


    private SharedPreferences getGlobalSettings_SharedPref() {
        return this.globalSettings_SharedPref;
    }

    private void setGlobalSettings_SharedPref(SharedPreferences globalSettings_SharedPref) {
        this.globalSettings_SharedPref = globalSettings_SharedPref;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void startBroadcastORBackgroundService() {
        if (this.useForwardCompatibility()) {
            //TODO: Kill Bg service before hand


            //USE broadcast receivers
            (new CustomNotification(this.getContext(), CountdownActivity.class, (NotificationManager) this.getContext().getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications(this.getContext());
            Log.d(TAG, "OnCreate: Scheduled broadcast receivers. ");
        } else {
            //TODO: Stop all broadcast receivers

            //Use background service
            this.getContext().startService(new Intent(this.getContext(),NotificationService.class)); //this line should be only called once
            Log.d(TAG, "OnCreate: Started background service.");
        }
    }
}
