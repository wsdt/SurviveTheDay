package kevkevin.wsdt.tagueberstehen.classes.customviews.DateTimePicker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import java.util.Calendar;
import java.util.GregorianCalendar;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Constants;


public class TimePickerFragment extends MyTimePickerDialog implements MyTimePickerDialog.OnTimeSetListener {
    private TextView resultView;
    private static final String TAG = "TimePickerFragment";
    private Resources res;
    private MyTimePickerDialog.OnTimeSetListener onTimeSetListener; //use this explicitely created listener to give them as callback

    public TimePickerFragment(@NonNull Context context, OnTimeSetListener callback, final TextView resultView, int hourOfDay, int minute, int seconds, boolean is24HourView) {
        super(context, callback, hourOfDay, minute, seconds, is24HourView);
        this.setResultView(resultView);

        //Overrides assigned null-Listener from superclass! --> ONCANCEL-BUTTON
        setButton2(context.getText(com.ikovac.timepickerwithseconds.R.string.cancel), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //if cancelled set current time
                GregorianCalendar now = new GregorianCalendar();
                getResultView().setText(String.format(getRes().getString(R.string.dateTimePicker_format_DateTime),getResultView().getText(),String.format(Constants.GLOBAL.LOCALE,"%02d", now.get(Calendar.HOUR_OF_DAY)),String.format(Constants.GLOBAL.LOCALE,"%02d", now.get(Calendar.MINUTE)),String.format(Constants.GLOBAL.LOCALE,"%02d", now.get(Calendar.SECOND))));
                Log.d(TAG, "onCancelListener: Time cancelled, set current time with previously selected date.");
                Toast.makeText(getContext(), "Set current date.",Toast.LENGTH_SHORT).show();
            }
        });

        this.res = context.getResources(); //important, must not be null!
    }

    @Override
    public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
        // Do something with the time chosen by the user
        this.getResultView().setText(String.format(this.getRes().getString(R.string.dateTimePicker_format_DateTime),getResultView().getText(),String.format(Constants.GLOBAL.LOCALE,"%02d", hourOfDay),String.format(Constants.GLOBAL.LOCALE,"%02d", minute),String.format(Constants.GLOBAL.LOCALE,"%02d", seconds)));
        Log.d(TAG, "onTimeSet: Assigned new time.");
    }


    public TextView getResultView() {
        return this.resultView;
    }

    public void setResultView(TextView resultView) {
        this.resultView = resultView;
    }

    public Resources getRes() {
        return res;
    }

    public void setRes(Resources res) {
        this.res = res;
    }
}