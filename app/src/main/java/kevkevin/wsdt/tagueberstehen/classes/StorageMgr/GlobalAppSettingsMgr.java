package kevkevin.wsdt.tagueberstehen.classes.StorageMgr;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class GlobalAppSettingsMgr {
    private SharedPreferences globalSettings_SharedPref;
    private static final String TAG = "GlobalAppSettingsMgr";
    private Context context;

    public GlobalAppSettingsMgr (Context context) {
        this.setContext(context);
        this.setGlobalSettings_SharedPref(context.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE));
    }

    public boolean useForwardCompatibility() {
        /* DEFAULT: true
                If TRUE: Use broadcastReceivers and make Option available for saveBattery
                If FALSE: Use Background service (long intervals might not work)
        */
        return this.getGlobalSettings_SharedPref().getBoolean("USE_FORWARD_COMPATITBILITY", true); //default true, so broadcast receivers used
    }

    public void setUseForwardCompatibility(boolean useForwardCompatibility) {
        //TODO
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
        //TODO
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
}