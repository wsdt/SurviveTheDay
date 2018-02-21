package kevkevin.wsdt.tagueberstehen.classes;


import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import kevkevin.wsdt.tagueberstehen.R;

public class HelperClass {
    private static final String TAG = "HelperClass";

    public static Spinner setIntervalSpinnerConfigurations(Spinner spinner, int arrayIntervalSpinnerLabels, int defaultPos) {
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
            Log.e(TAG, "setIntervalSpinnerConfigurations: Did not find entry for Spinner: "+defaultPos);
        }
        Log.d(TAG, "setIntervalSpinnerConfigurations: Tried to set spinner properties.");
        return spinner;
    }

    //inner class
    public interface ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation {
        //New interface for newest inappPurchaseHelper and maybe other classes
        void success_is_true();
        void failure_is_false();
    }

}
