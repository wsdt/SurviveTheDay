package kevkevin.wsdt.tagueberstehen.classes.StorageMgr;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.LoadingScreenActivity;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CustomNotification;
import kevkevin.wsdt.tagueberstehen.classes.services.CountdownCounterService;
import kevkevin.wsdt.tagueberstehen.classes.services.NotificationService;

import static android.content.Context.NOTIFICATION_SERVICE;


public class InternalCountdownStorageMgr {
    private SharedPreferences allCountdowns_SharedPref;
    private static final String TAG = "InternCountdownStorMgr";
    private HashMap<Integer, Countdown> allCountdowns;
    private Context context;


    public InternalCountdownStorageMgr(Context context) {
        setAllCountdowns_SharedPref(context.getSharedPreferences(Constants.STORAGE_MANAGERS.INTERNAL_COUNTDOWN_STR_MGR.SHAREDPREFERENCES_DBNAME, Context.MODE_PRIVATE));
        this.setContext(context);
    }

    public void deleteCountdown(int countdownId) {
        this.getAllCountdowns_SharedPref().edit().remove(Constants.MAIN_ACTIVITY.COUNTDOWN_VIEW_TAG_PREFIX + countdownId).apply();
        Log.d(TAG, "deleteCountdown: Deleted countdown with id: " + countdownId);

        //Restart service because countown got removed
        restartNotificationService();
    }

    public void deleteAllCountdowns() {
        //Deletes all countdowns ("COUNTDOWNS")
        this.getAllCountdowns_SharedPref().edit().clear().apply();

        //Restart service (because new/less services etc. / changed settings)
        restartNotificationService();
    }

    public int getNextCountdownId() {
        /* Returns Integer value of not existing countown id e.g.:
        * --> 1,2,3,4 [next: 5]
        * --> 1,2,4,5 [next: 3]
        * By filling the gaps we do not need to resave all countdowns when deleting a countdown
        * */
        int newCountdownId = (-1);
        //Load saved countdowns
        HashMap<Integer, Countdown> countdowns = this.getAllCountdowns(false, false);
        //int i = 0; //counter
        for (int i = 0; i < countdowns.size(); i++) {
            if (!countdowns.containsKey(i)) {
                Log.d(TAG, "getNextCountdownId: Next countdown id at: " + i);
                newCountdownId = i;
                break;
            }
        }
        if (newCountdownId < countdowns.size()) {
            newCountdownId = countdowns.size();
            Log.d(TAG, "getNextCountdownId: Found next countdown id after loop (just incremented/used from size): " + newCountdownId);
        } else {
            Log.d(TAG, "getNextCountdownId: Found next countdown id within loop (filled gap): " + newCountdownId);
        }
        return newCountdownId;
    }


    // GETTER/SETTER --------------------------------------------------------------------
    public Countdown getCountdown(int countdownId) {
        return getAllCountdowns(false, false).get(countdownId);
    }


    @SuppressLint("UseSparseArrays")
    public HashMap<Integer, Countdown> getAllCountdowns(boolean onlyActiveCountdowns, boolean onlyShowLiveCountdowns) { //Maps sharedpreferences on an arraylist of countdowns
        boolean fallbackResaveCountdown; //save countdown in sharedPreferences if e.g. sth. wrong saved (older versions etc.)

        //Override also if already loaded
        allCountdowns = new HashMap<>(); //delete list directly not over setter because this would also delete preferences!

        //Create for each entry an own Countdown instance and save it into arraylist
        Map<String, ?> keys = getAllCountdowns_SharedPref().getAll();

        try {
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                fallbackResaveCountdown = false; //evaluate for every countdown separetely
                Log.d(TAG, "getAllCountdowns: Saved entry: " + entry.getKey() + " / Value: " + entry.getValue().toString());
                ArrayList<String> lineArrList = new ArrayList<>();
                lineArrList.addAll(Arrays.asList((entry.getValue()).toString().split(";"))); //split entryvalue "1;title;descr;etc..." into array

                //[Line might get unnecessary in future versions, because nobody has old app version]: UPDATE ERROR AVOIDANCE (If index does not exist because of older sharedPrefs) ------------------------------
                if (lineArrList.size() == 10) {
                    Log.w(TAG, "getAllCountdowns: Index 11 did NOT exist! Added it manually (but tried to save it persistently)");
                    //10 indizes saved, but 11th [onlyShowLiveCountdowns] is saved into SP because of older app version
                    lineArrList.add(Boolean.toString(false)); //by default false if this error should happen
                    fallbackResaveCountdown = true;
                }
                //UPDATE ERROR AVOIDANCE - END

                //Countdown Id determines indexposition of that element in arraylist!
                Countdown tmp = new Countdown(this.getContext(), Integer.parseInt(lineArrList.get(0)), lineArrList.get(1), lineArrList.get(2), lineArrList.get(3), lineArrList.get(4), lineArrList.get(5), lineArrList.get(6), lineArrList.get(7), Boolean.parseBoolean(lineArrList.get(8)), Integer.parseInt(lineArrList.get(9)), Boolean.parseBoolean(lineArrList.get(10)));
                Log.d(TAG, "getAllCountdowns: PARSED-STRING: "+Integer.parseInt(lineArrList.get(0))+";"+lineArrList.get(1)+";"+lineArrList.get(2)+";"+lineArrList.get(3)+";"+ lineArrList.get(4)+";"+lineArrList.get(5)+";"+lineArrList.get(6)+";"+lineArrList.get(7)+";"+Boolean.parseBoolean(lineArrList.get(8))+";"+Integer.parseInt(lineArrList.get(9))+";"+Boolean.parseBoolean(lineArrList.get(10)));
                Log.d(TAG, "getAllCountdowns: Directly EXTRACTED-STRING: "+tmp.getCountdownId()+";"+tmp.getCountdownTitle()+";"+tmp.getCountdownDescription()+";"+tmp.getStartDateTime()+";"+tmp.getUntilDateTime()+";"+tmp.getCreatedDateTime()+";"+tmp.getLastEditDateTime()+";"+ tmp.getCategory()+";"+Boolean.toString(tmp.isActive())+";"+tmp.getNotificationInterval()+";"+Boolean.toString(tmp.isShowLiveCountdown()));
                Log.d(TAG, "getAllCountdowns: Countdown-String --> "+tmp.toString());


                if (fallbackResaveCountdown) {
                    //resave countdown if resolved error happened, so that this operation will not be necessary next time
                    tmp.savePersistently();
                    Log.d(TAG, "getAllCountdowns:fallbackResaveCountdown: Tried to save countdown persitently.");
                }

                if (onlyActiveCountdowns || onlyShowLiveCountdowns) {
                    if (tmp.isStartDateInThePast() && tmp.isUntilDateInTheFuture()) { //only add to activeCountdowns|onlyShowLiveCountdowns if startDate is in the past and untilDate is in Future (because otherwise service would run if motivateMe is on but startdate in the past
                        if (onlyActiveCountdowns) {
                            if (tmp.isActive()) { //is this specific countdown active? only then add it, because we only want active countdowns
                                allCountdowns.put(tmp.getCountdownId(), tmp);
                            }
                        } else { //else because with simple if countdown could be added twice (true, true)
                            if (tmp.isShowLiveCountdown()) { //is this specific countdown active? only then add it, because we only want active countdowns
                                allCountdowns.put(tmp.getCountdownId(), tmp);
                            }
                        }
                    } //both services are counting down, so we only want to add countdowns where until/startDate constraints are true
                } else {
                    //add all countdowns
                    allCountdowns.put(tmp.getCountdownId(), tmp);
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not parse String to Integer or boolean!");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "Entry or a splitted index of array is null!");
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Index does not exist! Maybe wrong implemented.");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unknown error!");
            e.printStackTrace();
        }
        Log.d(TAG, "getAllCountdowns: Length of returned arraylist is " + allCountdowns.size());
        return allCountdowns; //IMPORTANT: It is extremely important that the arraylist is ordered (for that we assign objects with index = countdown id
    }


    public void setSaveCountdown(Countdown countdown, boolean saveToPreferences) {
        /*if (this.allCountdowns == null) { //initialize hashmap if null (no countdowns do exist)
            this.allCountdowns = this.getAllCountdowns(false, false); --> CAUSES ENDLESS LOOP
        }*/

        Log.d(TAG, "setSaveCountdown: Entry might be replaced if it exists already.");
        if (this.allCountdowns != null) {
            this.allCountdowns.put(countdown.getCountdownId(), countdown);
        } else {
            Log.e(TAG, "setSaveCountdown: AllCountdowns HashMap is NULL! Cache might cause leaks.");
        }

        if (saveToPreferences) {
            Log.d(TAG, "setSaveCountdown: Saved countdown not only to list, but also to sharedpreferences.");
            //setSaveAllCountdowns(); //save modified arraylist to shared preferences
            getAllCountdowns_SharedPref().edit().putString(Constants.MAIN_ACTIVITY.COUNTDOWN_VIEW_TAG_PREFIX+countdown.getCountdownId(), countdown.toString()).apply();
            Log.d(TAG, "setSaveCountdown: Saved entry: (other countdowns not resaved) "+countdown.toString());
        } else {
            Log.d(TAG, "setSaveCountdown: Did not save countdown to sharedpreferences!");
        }

        //Restart service (because new/less services etc. / changed settings)
        restartNotificationService();
    }

    //private because arraylist could not be distinct
    private void setSaveAllCountdowns() {
        Log.d(TAG, "setSaveAllCountdowns: Length of unfiltered arraylist is " + allCountdowns.size());

        //Map arraylist onto sharedpreferences
        SharedPreferences.Editor editor = getAllCountdowns_SharedPref().edit();
        for (Map.Entry<Integer, Countdown> countdown : this.allCountdowns.entrySet()) {
            String countdownString = countdown.getValue().getCountdownId() + ";" + countdown.getValue().getCountdownTitle() + ";" + countdown.getValue().getCountdownDescription() + ";" + countdown.getValue().getStartDateTime() + ";" + countdown.getValue().getUntilDateTime() + ";" + countdown.getValue().getCreatedDateTime() + ";" + countdown.getValue().getLastEditDateTime() + ";" + countdown.getValue().getCategory() + ";" + Boolean.toString(countdown.getValue().isActive()) + ";" + countdown.getValue().getNotificationInterval()+";"+Boolean.toString(countdown.getValue().isShowLiveCountdown()); //save boolean as String so String.valueOf()
            Log.d(TAG, "setSaveAllCountdowns: Saved string is COUNTDOWN_" + countdown.getValue().getCountdownId() + "/" + countdownString);
            editor.putString(Constants.MAIN_ACTIVITY.COUNTDOWN_VIEW_TAG_PREFIX + countdown.getValue().getCountdownId(), countdownString);
        }
        editor.apply();

        //Restart service (because new/less services etc. / changed settings)
        restartNotificationService();
    }

    public void restartNotificationService() {
        //TODO: BEST: ONLY EXECUTE this method, IF especially a relevant setting got changed (but hard to implement, because shared prefs get overwritten) --> would solve comment below with only st
        Log.d(TAG, "restartNofificationService: Did not restart service (not necessary). Tried broadcast receiver.");
        //would not be necessary because on broadcastreceiver the current countdown gets automatically loaded!
        //except if countdown was created, then we have to reload it! (only changes/deletes do not require a reload) [but for bgservice mode it is necessary]

        if (new GlobalAppSettingsMgr(this.getContext()).useForwardCompatibility()) {
            //TODO: only do this when not already active (otherwise intervals will get restarted)
            (new CustomNotification(this.getContext(), LoadingScreenActivity.class, (NotificationManager) this.getContext().getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications(this.getContext());
            Log.d(TAG, "restartNotificationService: Rescheduled all broadcast receivers.");
        } else {
            Intent serviceIntent = new Intent(this.getContext(), NotificationService.class);
            try {
                this.getContext().stopService(serviceIntent);
                Log.d(TAG, "restartNotificationService: Tried to stop and restart service.");
                this.getContext().startService(serviceIntent);
            } catch (NullPointerException e) {
                Log.e(TAG, "restartNotificationService: ServiceIntent equals null! Could not restart service.");
            }
            Log.d(TAG, "restartNotificationService: Tried to restart service.");
        }

        //ALSO RESTART FOREGROUND SERVICE
        //TODO: does not work --> presumably because extra gets submitted despite removing it (also a new intent does not work!?)
        Intent foregroundServiceIntent = new Intent(this.getContext(), CountdownCounterService.class);
        try {
            foregroundServiceIntent.putExtra(Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE_LABEL,Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE);
            this.getContext().startService(foregroundServiceIntent); //startService instead of stopService, react to extra and stopSelf()
            Log.d(TAG, "restartNotificationService: Tried to stop and restart foregroundService.");
            foregroundServiceIntent.removeExtra(Constants.COUNTDOWNCOUNTERSERVICE.STOP_SERVICE_LABEL); //remove stopService Extra so service gets started
            this.getContext().startService(foregroundServiceIntent);
        } catch (NullPointerException e) {
            Log.e(TAG, "restartNotificationService: foregroundServiceIntent equals null! Could not restart foregroundService.");
        }
        Log.d(TAG, "restartNotficationService: Tried to restart foregroundService.");
    }


    //Private because this only the task of the storage mgr
    private SharedPreferences getAllCountdowns_SharedPref() {
        return allCountdowns_SharedPref;
    }

    private void setAllCountdowns_SharedPref(SharedPreferences allCountdowns_SharedPref) {
        this.allCountdowns_SharedPref = allCountdowns_SharedPref;
    }

    private Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }
}
