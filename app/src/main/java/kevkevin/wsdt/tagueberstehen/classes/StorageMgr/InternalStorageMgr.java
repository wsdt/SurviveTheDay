package kevkevin.wsdt.tagueberstehen.classes.StorageMgr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;


public class InternalStorageMgr {
    private SharedPreferences allCountdowns_SharedPref;
    private static final String TAG = "InternalStorageMgr";
    private HashMap<Integer,Countdown> allCountdowns;
    private Context context;


    public InternalStorageMgr(Context context) {
        setAllCountdowns_SharedPref(context.getSharedPreferences("COUNTDOWNS",Context.MODE_PRIVATE));
        this.setContext(context);
    }

    public void deleteAllCountdowns() {
        //Deletes all countdowns ("COUNTDOWNS")
        this.getAllCountdowns_SharedPref().edit().clear().apply();
    }

    public int getNextCountdownId() {
        /* Returns Integer value of not existing countown id e.g.:
        * --> 1,2,3,4 [next: 5]
        * --> 1,2,4,5 [next: 3]
        * By filling the gaps we do not need to resave all countdowns when deleting a countdown
        * */
        int newCountdownId = (-1);
        //Load saved countdowns
        HashMap<Integer,Countdown> countdowns = this.getAllCountdowns(false);
        //int i = 0; //counter
        for (int i = 0; i < countdowns.size(); i++) {
            if (!countdowns.containsKey(i)) {
                Log.d(TAG, "getNextCountdownId: Next countdown id at: "+i);
                newCountdownId = i;
                break;
            }
        }
        if (newCountdownId < countdowns.size()) {
            newCountdownId = countdowns.size();
            Log.d(TAG, "getNextCountdownId: Found next countdown id after loop (just incremented/used from size): "+newCountdownId);
        } else {
            Log.d(TAG, "getNextCountdownId: Found next countdown id within loop (filled gap): "+newCountdownId);
        }
        return newCountdownId;
    }


    // GETTER/SETTER --------------------------------------------------------------------
    public Countdown getCountdown(int countdownId) {
        return getAllCountdowns(false).get(countdownId);
    }

    @SuppressLint("UseSparseArrays")
    public HashMap<Integer, Countdown> getAllCountdowns(boolean onlyActiveCountdowns) { //Maps sharedpreferences on an arraylist of countdowns
        //Override also if already loaded
        allCountdowns = new HashMap<>(); //delete list directly not over setter because this would also delete preferences!

        //Create for each entry an own Countdown instance and save it into arraylist
        Map<String,?> keys = getAllCountdowns_SharedPref().getAll();

        try {
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                Log.d(TAG, "getAllCountdowns: Saved entry: " + entry.getKey() + " / Value: " + entry.getValue().toString());
                String[] lineArr = (entry.getValue()).toString().split(";"); //split entryvalue "1;title;descr;etc..." into array
                //Countdown Id determines indexposition of that element in arraylist!
                if (onlyActiveCountdowns) {
                    if (Boolean.parseBoolean(lineArr[8])) { //is this specific countdown active? only then add it, because we only want active countdowns
                            allCountdowns.put(Integer.parseInt(lineArr[0]),new Countdown(this.getContext(), Integer.parseInt(lineArr[0]), lineArr[1], lineArr[2], lineArr[3], lineArr[4], lineArr[5], lineArr[6], lineArr[7], Boolean.parseBoolean(lineArr[8])));
                       }
                } else {
                    allCountdowns.put(Integer.parseInt(lineArr[0]),new Countdown(this.getContext(), Integer.parseInt(lineArr[0]), lineArr[1], lineArr[2], lineArr[3], lineArr[4], lineArr[5], lineArr[6], lineArr[7], Boolean.parseBoolean(lineArr[8])));
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG,"Could not prase Strint to Integer or boolean! ");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG,"Entry or a splitted index of array is null!");
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Index does not exist! Maybe wrong implemented.");
            e.printStackTrace();
        }
        Log.d(TAG,"getAllCountdowns: Length of returned arraylist is "+allCountdowns.size());
        return allCountdowns; //IMPORTANT: It is extremely important that the arraylist is ordered (for that we assign objects with index = countdown id
    }


    public void setSaveCountdown(Countdown countdown, boolean saveToPreferences) {
        if (this.allCountdowns == null) { //initialize hashmap if null (no countdowns do exist)
            this.allCountdowns = this.getAllCountdowns(false);
        }

        Log.d(TAG, "setSaveCountdown: Entry might be replaced if it exists already.");
        this.allCountdowns.put(countdown.getCountdownId(), countdown);

        if (saveToPreferences) {
            Log.d(TAG,"setSaveCountdown: Saved countdown not only to list, but also to sharedpreferences.");
            setSaveAllCountdowns(); //save modified arraylist to shared preferences
        } else {
            Log.d(TAG,"setSaveCountdown: Did not save countdown to sharedpreferences!");
        }
    }

    //private because arraylist could not be distinct
    private void setSaveAllCountdowns() {
        Log.d(TAG,"setSaveAllCountdowns: Length of unfiltered arraylist is "+allCountdowns.size());

        //Map arraylist onto sharedpreferences
        SharedPreferences.Editor editor = getAllCountdowns_SharedPref().edit();
        for (Map.Entry<Integer,Countdown> countdown : this.allCountdowns.entrySet()) {
            String countdownString = countdown.getValue().getCountdownId()+";"+countdown.getValue().getCountdownTitle()+";"+countdown.getValue().getCountdownDescription()+";"+countdown.getValue().getStartDateTime()+";"+countdown.getValue().getUntilDateTime()+";"+countdown.getValue().getCreatedDateTime()+";"+countdown.getValue().getLastEditDateTime()+";"+countdown.getValue().getCategory()+";"+Boolean.toString(countdown.getValue().isActive()); //save boolean as String so String.valueOf()
            Log.d(TAG,"setSaveAllCountdowns: Saved string is COUNTDOWN_"+countdown.getValue().getCountdownId()+"/"+countdownString);
            editor.putString("COUNTDOWN_"+countdown.getValue().getCountdownId(),countdownString);
        }
        editor.apply();
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
