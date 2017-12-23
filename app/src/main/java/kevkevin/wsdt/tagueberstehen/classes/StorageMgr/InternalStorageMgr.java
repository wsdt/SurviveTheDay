package kevkevin.wsdt.tagueberstehen.classes.StorageMgr;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;


public class InternalStorageMgr {
    private SharedPreferences allCountdowns_SharedPref;
    private static final String TAG = "InternalStorageMgr";
    private ArrayList<Countdown> allCountdowns;
    private Context context;


    public InternalStorageMgr(Context context) {
        setAllCountdowns_SharedPref(context.getSharedPreferences("COUNTDOWNS",Context.MODE_PRIVATE));

        this.setContext(context);
    }


    // GETTER/SETTER --------------------------------------------------------------------
    public ArrayList<Countdown> getAllCountdowns(boolean onlyActiveCountdowns) { //Maps sharedpreferences on an arraylist of countdowns
        //Override also if already loaded
        allCountdowns = new ArrayList<>(); //delete list directly not over setter because this would also delete preferences!

        //Create for each entry an own Countdown instance and save it into arraylist
        Map<String,?> keys = getAllCountdowns_SharedPref().getAll();

        try {
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                Log.d(TAG, "getAllCountdowns: Saved entry: " + entry.getKey() + " / Value: " + entry.getValue().toString());
                String[] lineArr = (entry.getValue()).toString().split(";"); //split entryvalue "1;title;descr;etc..." into array
                //Countdown Id determines indexposition of that element in arraylist!
                if (onlyActiveCountdowns) {
                    if (Boolean.parseBoolean(lineArr[7])) { //is this specific countdown active? only then add it, because we only want active countdowns
                        allCountdowns.add(Integer.parseInt(lineArr[0]), new Countdown(this.getContext(), Integer.parseInt(lineArr[0]), lineArr[1], lineArr[2], lineArr[3], lineArr[4], lineArr[5], lineArr[6], Boolean.parseBoolean(lineArr[7])));
                    }
                } else {
                    allCountdowns.add(Integer.parseInt(lineArr[0]), new Countdown(this.getContext(), Integer.parseInt(lineArr[0]), lineArr[1], lineArr[2], lineArr[3], lineArr[4], lineArr[5], lineArr[6], Boolean.parseBoolean(lineArr[7])));
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG,"Could not prase Strint to Integer or boolean! ");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG,"Entry or a splitted index of array is null!");
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "Array index does not exist! Maybe wrong implemented.");
            e.printStackTrace();
        }

        Log.d(TAG,"getAllCountdowns: Length of returned arraylist is "+allCountdowns.size());
        return allCountdowns;
    }

    public void setSaveCountdown(Countdown countdown, boolean saveToPreferences) {
        //IMPORTANT: This function should ensure that the arraylist and hence the sharedpreferences are distinct
        try {
            if (this.allCountdowns.get(countdown.getCountdownId()) != null) {
                Log.d(TAG,"setSaveCountdown: Entry does exist in list and we override it with set.");
                this.allCountdowns.set(countdown.getCountdownId(),countdown);
            } else {
                Log.e(TAG,"setSaveCountdown: This error should not occur!");
            }
        } catch (IndexOutOfBoundsException e) {
            //means that entry does not exist yet so we use add instead of set
            Log.e(TAG,"setSaveCountdown: Entry does not exist in list yet so we added it to list.");
            this.allCountdowns.add(countdown.getCountdownId(),countdown);
        }
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
        for (Countdown countdown : this.allCountdowns) {
            String countdownString = countdown.getCountdownId()+";"+countdown.getCountdownTitle()+";"+countdown.getCountdownDescription()+";"+countdown.getUntilDateTime()+";"+countdown.getCreatedDateTime()+";"+countdown.getLastEditDateTime()+";"+countdown.getCategory()+";"+countdown.isActive();
            Log.d(TAG,"setSaveAllCountdowns: Saved string is COUNTDOWN_"+countdown.getCountdownId()+"/"+countdownString);
            editor.putString("COUNTDOWN_"+countdown.getCountdownId(),countdownString);
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
