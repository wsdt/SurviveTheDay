package kevkevin.wsdt.tagueberstehen;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.content.Context;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

//Thanks to http://www.truiton.com/2013/03/android-pick-date-time-from-edittext-onclick-event/
public class DateTimePicker extends FragmentActivity {
    static EditText DateEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_picker);
        DateEdit = (EditText) findViewById(R.id.dateTimePicker);
        setCustomOnClickListener(DateEdit);
    }

    public void setCustomOnClickListener(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
                showDatePickerDialog(v);
            }
        });
    }


    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

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

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            DateEdit.setText(day + "." + (month + 1) + "." + year);
        }
    }

    public void showTimePickerDialog(View v) {
        /* OLD works, but no support for seconds
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");*/
        GregorianCalendar now = new GregorianCalendar();

        final TimePickerFragment mTimePicker = new TimePickerFragment(this, new TimePickerFragment.OnTimeSetListener() {
            @Override
            public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
                TimePickerFragment.onTimeSet(view, hourOfDay, minute, seconds);
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND), true);
        mTimePicker.show();
    }

    public static class TimePickerFragment extends MyTimePickerDialog {

        public TimePickerFragment(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, int seconds, boolean is24HourView) {
            super(context, callBack, hourOfDay, minute, seconds, is24HourView);
        }

        public TimePickerFragment(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, int seconds, boolean is24HourView) {
            super(context, theme, callBack, hourOfDay, minute, seconds, is24HourView);
        }

        public static void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
            // Do something with the time chosen by the user
            DateEdit.setText(DateEdit.getText() + " " + String.format("%02d", hourOfDay) + ":"	+ String.format("%02d", minute)+":"+String.format("%02d", seconds));
        }
    }



    /* WORKS but no support for SECONDS!*/

    /*public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            DateEdit.setText(DateEdit.getText() + " " + hourOfDay + ":"	+ minute+":00"); //because seconds cannot be selected until now
        }
    }*/

}