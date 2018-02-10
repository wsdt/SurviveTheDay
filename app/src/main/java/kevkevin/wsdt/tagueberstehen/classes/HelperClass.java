package kevkevin.wsdt.tagueberstehen.classes;


import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import kevkevin.wsdt.tagueberstehen.R;

public class HelperClass {
    private static final String TAG = "HelperClass";

    public static Spinner setIntervalSpinnerConfigurations(Spinner spinner, int arrayIntervalSpinnerLabels) {
        //Spinner spinner = (Spinner) findViewById(R.id.notificationIntervalSpinner);
        //Create an arrayadapter using string array from strings.xml and default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(spinner.getContext(),
                arrayIntervalSpinnerLabels/*R.array.countdownIntervalSpinner_LABELS*/, R.layout.spinner_intervall_item);
        //specify layout to use when list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply adapter to spinner
        spinner.setAdapter(adapter);
        Log.d(TAG, "setIntervalSpinnerConfigurations: Tried to set spinner properties.");
        return spinner;
    }

    //So we can pass future values to other methods when they are successfully loaded
    public interface InAppPurchaseMgrReadyListener<T> {
        void isDataLoaded(T arg);
    }

    /*public static void letThreadSleepInMilliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Log.e(TAG, "letThreadSleepInMilliseconds: Tried to wait for "+milliseconds+" ms.");
            e.printStackTrace();
        }
    }*/
}
