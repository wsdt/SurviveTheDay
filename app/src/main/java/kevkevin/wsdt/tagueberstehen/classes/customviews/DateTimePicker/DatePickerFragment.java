package kevkevin.wsdt.tagueberstehen.classes.customviews.DateTimePicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Constants;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private TextView resultView;
    private Resources res;
    private static final String TAG = "DatePickerFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        this.setRes(getResources()); //important must NOT be null!

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        GregorianCalendar now = new GregorianCalendar();
        TimePickerFragment tmpTimePicker = new TimePickerFragment(getActivity(), new MyTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                // Do something with the time chosen by the user
                getResultView().setText(String.format(getRes().getString(R.string.dateTimePicker_format_DateTime),getResultView().getText(),String.format(Constants.GLOBAL.LOCALE,"%02d", hourOfDay),String.format(Constants.GLOBAL.LOCALE,"%02d", minute),String.format(Constants.GLOBAL.LOCALE,"%02d", seconds)));
                Log.d(TAG, "onTimeSet: Assigned new time.");
            }
        }, this.getResultView(), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND), true);
        tmpTimePicker.show(); //show timePicker only if dateTime set (so dialog will not show if cancelled)
        //tmpTimePicker.setResultView(this.getResultView());
        //DateTimePicker.thisInstance.getMainTimePicker().show(); //show timePicker only if dateTime set (so dialog will not show if cancelled)

        // Do something with the date chosen by the user
        this.getResultView().setText(String.format(this.getRes().getString(R.string.dateTimePicker_format_date),day,(month + 1),year));
        Log.d(TAG, "onDateSet: Assigned new date.");
    }

    public void setResultView(TextView resultView) {
        this.resultView = resultView;
    }

    public TextView getResultView() {
        return this.resultView;
    }

    public Resources getRes() {
        return res;
    }

    public void setRes(Resources res) {
        this.res = res;
    }
}