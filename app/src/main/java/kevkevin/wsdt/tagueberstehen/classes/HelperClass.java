package kevkevin.wsdt.tagueberstehen.classes;


import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.text.DecimalFormat;
import java.util.Random;

import kevkevin.wsdt.tagueberstehen.R;

public class HelperClass {
    private static final String TAG = "HelperClass";
    private static final Random random = new Random();

    /** With this random no. factory only one object is created once :) */
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
                arrayIntervalSpinnerLabels/*R.array.countdownIntervalSpinner_LABELS*/, R.layout.spinner_intervall_item);
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
