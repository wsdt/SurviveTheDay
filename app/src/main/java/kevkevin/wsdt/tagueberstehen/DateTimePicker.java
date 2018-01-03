package kevkevin.wsdt.tagueberstehen;
import java.util.Calendar;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

//Thanks to http://www.truiton.com/2013/03/android-pick-date-time-from-edittext-onclick-event/
public class DateTimePicker {
    private FragmentManager fragmentManager;
    private DatePickerFragment mainDatePicker;
    private TimePickerFragment mainTimePicker;
    private TextView mainResultView;
    private static final String TAG = "DateTimePicker";

    public DateTimePicker (Context context, FragmentManager fragmentManager, @NonNull TextView mainResultView, int hourOfDay, int minute, int seconds, boolean is24hourView) {
        this.setMainResultView(mainResultView);
        this.setFragmentManager(fragmentManager);

        //Create datepicker and assign it to outer class
        DatePickerFragment tmpDatePicker = new DatePickerFragment();
        tmpDatePicker.setResultView(mainResultView);
        this.setMainDatePicker(tmpDatePicker);

        //create time picker and assign it to outer class
        TimePickerFragment tmpTimePicker = new TimePickerFragment(context, hourOfDay, minute, seconds, is24hourView);
        tmpTimePicker.setResultView(mainResultView);
        this.setMainTimePicker(tmpTimePicker);
    }

    public void showDateTimePicker() {
        try {
            this.getMainTimePicker().show(); //call timepicker firstly so it is behind datepicker
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

    public TimePickerFragment getMainTimePicker() {
        return mainTimePicker;
    }

    private void setMainTimePicker(TimePickerFragment mainTimePicker) {
        this.mainTimePicker = mainTimePicker;
    }

    public TextView getMainResultView() {
        return mainResultView;
    }

    public void setMainResultView(TextView mainResultView) {
        this.mainResultView = mainResultView;
    }


    // STATIC CLASSES #########################################################

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private TextView resultView;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }


        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            getResultView().setText(day + "." + (month + 1) + "." + year);
        }

        public void setResultView(TextView resultView) {
            this.resultView = resultView;
        }

        public TextView getResultView() {
            return this.resultView;
        }
    }

    //########################## TIME PICKER ####################################
    public static class TimePickerFragment extends MyTimePickerDialog implements MyTimePickerDialog.OnTimeSetListener {
        private TextView resultView;

        public TimePickerFragment(Context context, int hourOfDay, int minute, int seconds, boolean is24HourView) {
            super(context, null, hourOfDay, minute, seconds, is24HourView);
            /* callback = new DateTimePicker.TimePickerFragment.OnTimeSetListener() {
                @Override
                public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
                    DateTimePicker.TimePickerFragment.onTimeSet(view, hourOfDay, minute, seconds);
                }
            }*/
        }

        public TimePickerFragment(Context context, int theme, int hourOfDay, int minute, int seconds, boolean is24HourView) {
            super(context, theme, null, hourOfDay, minute, seconds, is24HourView);
        }

        @Override
        public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
            // Do something with the time chosen by the user
            getResultView().setText(getResultView().getText() + " " + String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", seconds));
        }

        public TextView getResultView() {
            return resultView;
        }

        public void setResultView(TextView resultView) {
            this.resultView = resultView;
        }
    }
}