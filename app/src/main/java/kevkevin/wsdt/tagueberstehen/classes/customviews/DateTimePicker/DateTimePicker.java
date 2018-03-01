package kevkevin.wsdt.tagueberstehen.classes.customviews.DateTimePicker;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

//Thanks to http://www.truiton.com/2013/03/android-pick-date-time-from-edittext-onclick-event/
public class DateTimePicker {
    private FragmentManager fragmentManager;
    private DatePickerFragment mainDatePicker;
    private TimePickerFragment mainTimePicker;
    private TextView mainResultView;
    private static final String TAG = "DateTimePicker";

    public DateTimePicker (Context context, FragmentManager fragmentManager, @NonNull TextView mainResultView, int hourOfDay, int minute, int seconds, boolean is24hourView) {
        Log.d(TAG, "Created instance.");
        //this.setMainResultView(mainResultView);
        this.setFragmentManager(fragmentManager);
        Log.d(TAG, "Tried to assign and create DatePickerFragment. ");

        //Create datepicker and assign it to outer class
        DatePickerFragment tmpDatePicker = new DatePickerFragment();
        tmpDatePicker.setResultView(mainResultView);
        this.setMainDatePicker(tmpDatePicker);
    }

    public void showDateTimePicker() {
        try {
            //this.getMainTimePicker().show(); //call timepicker firstly so it is behind datepicker
            this.getMainDatePicker().show(this.getFragmentManager(), "datePicker");
        } catch (NullPointerException e) {
            Log.e(TAG, "showDateTimePicker: NullpointerException! Define both fragments firstly!");
        }
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public FragmentManager getFragmentManager() {
        return this.fragmentManager;
    }

    public DatePickerFragment getMainDatePicker() {
        return mainDatePicker;
    }

    private void setMainDatePicker(DatePickerFragment mainDatePicker) {
        this.mainDatePicker = mainDatePicker;
    }
}