package kevkevin.wsdt.tagueberstehen.classes;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kevkevin.wsdt.tagueberstehen.R;

public class HelperClass {
    private static final String TAG = "HelperClass";
    private static final Random random = new Random();
    private static Thread doPeriodicallyThread; //ONLY allow one thread at the same time (so we can manage it)

    /** Used for Userlibrary download for mapping to java obj. */
    public static List<String> convertJsonArrayToList(@NonNull JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "convertJsonArrayToList: Could not transform jsonarray to stringList.");
            e.printStackTrace();
        }
        return list;
    }

    /** Do sth periodically, this method does support UI updating!
     * --> IMPORTANT: Stop thread with stopPeriodically if not needed anymore.*/
    public static void doPeriodically(@NonNull final Activity activity, final int intervall, final ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        doPeriodicallyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!doPeriodicallyThread.isInterrupted()) { //do until not interrupted
                        Thread.sleep(intervall);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG, "doPeriodically: Thread interrupted. Ending thread.");
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                }
            }
        });
        doPeriodicallyThread.start();
        Log.d(TAG, "doPeriodically: Tried to start doPeriodically thread.");
    }

    public static void stopPeriodically() {
        //ExecuteFalse will be executed! (provided in doPeriodically())
        if (doPeriodicallyThread != null) {
            doPeriodicallyThread.interrupt();
            Log.d(TAG, "stopPeriodically: Tried to stop periodically thread.");
        }
        Log.d(TAG, "stopPeriodically: Method ended.");
    }

    /**
     * With this random no. factory only one object is created once :)
     */
    public static int getRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min; //r.nextInt(max - min + 1) + min;
    }

    /**
     * @param nonFormattedNumber: Only numbers are allowed (int, double, etc.)
     * @param nachkommaStellen:   only int from 0-n
     */
    public static <N extends Number> String formatCommaNumber(@NonNull N nonFormattedNumber, int nachkommaStellen) {
        if (nachkommaStellen < 0) { //prevent arrayindex errors
            Log.w(TAG, "formatCommaNumber: Did not format number, because nachkommastellen were negative!");
            return nonFormattedNumber.toString();
        }

        String formatStr = "#0" + ((nachkommaStellen > 0) ? "." : ""); //standard formatting now [only adds comma separator if nachkommastelle > 0]
        formatStr += new String(new char[nachkommaStellen]).replace("\0", "0");  //makes array acc. to nachkommastellen and replaces empty chars with 0s
        return (new DecimalFormat(formatStr)).format(nonFormattedNumber);
    }

    public static void setIntervalSpinnerConfigurations(Spinner spinner, int arrayIntervalSpinnerLabels, int defaultPos) {
        //Spinner spinner = (Spinner) findViewById(R.id.notificationIntervalSpinner);
        //Create an arrayadapter using string array from strings.xml and default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(spinner.getContext(),
                arrayIntervalSpinnerLabels, R.layout.spinner_intervall_item);
        //specify layout to use when list of choices appears

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply adapter to spinner
        spinner.setAdapter(adapter);
        try {
            spinner.setSelection(defaultPos);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "setIntervalSpinnerConfigurations: Did not find entry for Spinner: " + defaultPos);
        }
        Log.d(TAG, "setIntervalSpinnerConfigurations: Tried to set spinner properties.");
    }

    //inner class
    public interface ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation {
        //New interface for newest inappPurchaseHelper and maybe other classes
        void success_is_true();
        void failure_is_false();
    }

}
