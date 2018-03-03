package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;


import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.CreditsActivity;
import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * GLOBALAPPSETTINGS SHOULD BE REALIZED WITH SHAREDPREFS! (faster and makes more sense, otherwise settingstable would have only one line!)
 */
public class GlobalAppSettingsMgr {
    private SharedPreferences globalSettings_SharedPref;
    private static final String TAG = "GlobalAppSettingsMgr";
    private Context context;

    public GlobalAppSettingsMgr(@NonNull Context context) {
        this.setContext(context);
        this.setGlobalSettings_SharedPref(context.getSharedPreferences(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.SHAREDPREFERENCES_DBNAME, Context.MODE_MULTI_PROCESS)); //Multi_mode process necessary so we have update contents in our services
    }

    //Save system.currentTimeMillis()+rewardedValue() in Milliseconds or so as new grenzwert
    public void setRemoveAdsTemporarlyInMinutes(int adFreeMinutes) {
        this.getGlobalSettings_SharedPref().edit().putLong(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.REMOVE_ADS_TEMPORARLY_IN_MINUTES, System.currentTimeMillis() + (adFreeMinutes * 60 * 1000)).apply(); //convert to milliseconds (as long current timestamp is smaller ads will be hidden)
        Log.d(TAG, "setRemoveAdsTemporarlyInMinutes: Tried to save new adFreeTemporarly value.");
    }


    public boolean isRemoveAdsTemporarlyInMinutesActiveValid() {
        boolean isRemoveAdsTemporarlyInMinutesActiveValid = false;
        //FALSE = Show all ads / TRUE = hide all ads except rewarded video ads

        long lastRewardedVideoSeenCheckpoint = this.getGlobalSettings_SharedPref().getLong(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.REMOVE_ADS_TEMPORARLY_IN_MINUTES, -1); //-1 by default, because an impossible value
        if (lastRewardedVideoSeenCheckpoint < 0) {
            Log.e(TAG, "isRemoveAdsTemporarlyInMinutesActiveValid: Never watched a rewarded video, because no last temporary ad free video watched.");
        } else {
            //Found value
            long systemCurrentTimeMillis = System.currentTimeMillis();
            if ((lastRewardedVideoSeenCheckpoint) > (systemCurrentTimeMillis)) {
                //lastCheckpoint is bigger than now so we can hide ads (because var = thatMoment+rewardedMinutes)
                Log.d(TAG, "isRemoveAdsTemporarlyInMinutesActiveValid: Ads will be hidden, because last checkpoint is within constraints: " + lastRewardedVideoSeenCheckpoint + " - Now: " + systemCurrentTimeMillis);
                isRemoveAdsTemporarlyInMinutesActiveValid = true;
            } else {
                Log.d(TAG, "isRemoveAdsTemporarlyInMinutesActiveValid: Ads will be shown, because last checkpoint is outside constraints: " + lastRewardedVideoSeenCheckpoint + " - Now: " + systemCurrentTimeMillis);
            }
        }


        return isRemoveAdsTemporarlyInMinutesActiveValid;
    }

    // ENSURING THAT PEOPLE ARE WATCHING ADS [every ad than cannot be displayed increments this parameter] #################################
    //Value is incremented, but until now only a message will be shown if int e.g. is higher than 5 [do not forget the user experience, so do not block user from using app AND do not forget playstore guidelines) --> because of that until now only a message]
    public void incrementNoInternetConnectionCounter() {
        int newNoInternetConnectionValue = getNoInternetConnectionCounter() + 1;
        if (newNoInternetConnectionValue >= Constants.ADMANAGER.NO_INTERNET_CONNECTION_MAX) {
            CustomNotification customNotification = (new CustomNotification(this.getContext(), CreditsActivity.class, (NotificationManager) this.getContext().getSystemService(NOTIFICATION_SERVICE)));
            String notificationText = this.getContext().getResources().getString(R.string.adManager_noInternetConnectionMaxExceeded_notification_normalAndBigText);
            customNotification.issueNotification(customNotification.createNotCountdownRelatedNotification(this.getContext().getResources().getString(R.string.adManager_noInternetConnectionMaxExceeded_notification_title), notificationText, notificationText, R.drawable.light_notification_warning));
            resetNoInternetConnectionCounter();
        } else {
            this.getGlobalSettings_SharedPref().edit().putInt(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.NO_INTERNET_CONNECTION_COUNTER, newNoInternetConnectionValue).apply(); //increase about 1 when ad cannot be displayed
            Log.d(TAG, "incrementNoInternetConnectionCounter: Incremented NoInternetConnection counter. Curr. value: " + newNoInternetConnectionValue);
        }
    }

    public void resetNoInternetConnectionCounter() {
        this.getGlobalSettings_SharedPref().edit().putInt(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.NO_INTERNET_CONNECTION_COUNTER, 0).apply(); //must be zero for resetting
        Log.d(TAG, "resetNoInternetConnectionCounter: Resetted ad ensurer.");
    }

    public int getNoInternetConnectionCounter() {
        Log.d(TAG, "getNoInternetConnectionCounter: Tried to return ad ensurer value.");
        return this.getGlobalSettings_SharedPref().getInt(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.NO_INTERNET_CONNECTION_COUNTER, 0);
    }
    // END: ENSURING THAT PEOPLE ARE WATCHING ADS [every ad than cannot be displayed increments this parameter] #################################


    public void setBackgroundServicePid(int bgServicePid) {
        //only used to kill the process of bg service
        this.getGlobalSettings_SharedPref().edit().putInt(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.SPIDENTIFIER_BG_SERVICE_PID, bgServicePid).apply();
        Log.d(TAG, "setBackgroundServicePid: Tried to save process id of bg service.");
    }

    public int getBackgroundServicePid() {
        return this.getGlobalSettings_SharedPref().getInt(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.SPIDENTIFIER_BG_SERVICE_PID, -1);
    }

    public boolean saveBattery() {
        return this.getGlobalSettings_SharedPref().getBoolean(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.SPIDENTIFIER_SAVE_BATTERY, false); //default is false, so more precise
    }

    public void setSaveBattery(boolean saveBattery) {
        //no restart necessary value gets called dynamically
        this.getGlobalSettings_SharedPref().edit().putBoolean(Constants.STORAGE_MANAGERS.GLOBAL_APPSETTINGS_STR_MGR.SPIDENTIFIER_SAVE_BATTERY, saveBattery).apply();
        Log.d(TAG, "setSaveBattery: Saved new saveBattery setting.");
    }

    public void setInAppNotificationShowDurationInS(int seconds) {
        this.getGlobalSettings_SharedPref().edit().putInt("INAPP_NOTIFICATION_SHOW_DURATION", 1000 * seconds).apply();
        Log.d(TAG, "setSaveInAppNotificationShowDuration: Saved new inapp notification setting.");
    }

    public int getInAppNotificationShowDurationInMs() {
        Log.d(TAG, "getSaveInAppNotificationShowDuration: Returned inapp notification setting.");
        return this.getGlobalSettings_SharedPref().getInt("INAPP_NOTIFICATION_SHOW_DURATION", 7000); //7 seconds as default
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

    public void startBroadcastReceiver() {
        //Kill Bg service before hand
        Log.d(TAG, "startBroadcastORBackgroundService: Trying to kill service ungracefully.");
        try {
            android.os.Process.killProcess(this.getBackgroundServicePid()); //Important: Otherwise service would not stop because sth will still run in this instance
            Log.d(TAG, "startBroadcastORBackgroundService: Tried to kill bg process.");
        } catch (Exception e) {
            Log.e(TAG, "startBroadcastORBackgroundService: Could not kill Notificationservice.");
            e.printStackTrace();
        }

        //USE broadcast receivers
        (new CustomNotification(this.getContext(), CountdownActivity.class, (NotificationManager) this.getContext().getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications();
        Log.d(TAG, "OnCreate: Scheduled broadcast receivers. ");
    }
}
