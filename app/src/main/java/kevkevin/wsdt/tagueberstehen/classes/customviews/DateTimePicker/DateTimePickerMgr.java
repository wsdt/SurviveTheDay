package kevkevin.wsdt.tagueberstehen.classes.customviews.DateTimePicker;

import android.app.Dialog;
import android.app.DialogFragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.ikovac.timepickerwithseconds.TimePicker;

import java.util.GregorianCalendar;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.manager.DialogMgr;

/** For each dateTimePicker Field we need a separate instance of this manager! */
@Deprecated
public class DateTimePickerMgr {
    private static final String TAG = "DateTimePickerMgr";
    private FragmentManager fragmentManager;
    private DateTimePickerDialog dateTimePickerDialog;

    public DateTimePickerMgr(@NonNull FragmentManager fragmentManager, @NonNull TextView targetTextView) {
        this.setFragmentManager(fragmentManager);
        this.setDateTimePickerDialog(new DateTimePickerDialog());
        this.getDateTimePickerDialog().setTargetTextView(targetTextView);
    }

    public void showDialog() {
        this.getDateTimePickerDialog().show(this.getFragmentManager(),"dateTimePicker");
    }


    public DateTimePickerDialog getDateTimePickerDialog() {
        return dateTimePickerDialog;
    }

    public void setDateTimePickerDialog(DateTimePickerDialog dateTimePickerDialog) {
        this.dateTimePickerDialog = dateTimePickerDialog;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    //GETTER/SETTER for members inside static nested class for outer classes
    public int getSelectedYear() {
        return this.getDateTimePickerDialog().getSelectedYear();
    }

    public void setSelectedYear(int selectedYear) {
        this.getDateTimePickerDialog().setSelectedYear(selectedYear);
    }

    public int getSelectedMonth() {
        return this.getDateTimePickerDialog().getSelectedMonth();
    }

    public void setSelectedMonth(int selectedMonth) {
        this.getDateTimePickerDialog().setSelectedMonth(selectedMonth);
    }

    public int getSelectedDay() {
        return this.getDateTimePickerDialog().getSelectedDay();
    }

    public void setSelectedDay(int selectedDay) {
        this.getDateTimePickerDialog().setSelectedDay(selectedDay);
    }

    public int getSelectedHour() {
        return this.getDateTimePickerDialog().getSelectedHour();
    }

    public void setSelectedHour(int selectedHour) {
        this.getDateTimePickerDialog().setSelectedHour(selectedHour);
    }

    public int getSelectedMinute() {
        return this.getDateTimePickerDialog().getSelectedMinute();
    }

    public void setSelectedMinute(int selectedMinute) {
        this.getDateTimePickerDialog().setSelectedMinute(selectedMinute);
    }

    public int getSelectedSeconds() {
        return this.getDateTimePickerDialog().getSelectedSeconds();
    }

    public void setSelectedSeconds(int selectedSeconds) {
        this.getDateTimePickerDialog().setSelectedSeconds(selectedSeconds);
    }

    /**
     * Tries to combine date and time view into one alert dialog
     * Important: Only DateTimePickerMgr should work with this subclass!
     */
    public static class DateTimePickerDialog extends DialogFragment {
        private GregorianCalendar now = new GregorianCalendar();
        private TextView targetTextView;
        private DatePicker datePicker;
        private TimePicker timePicker;

        /*Selected values (with getter/setter so we can modify them from outside)*/
        private int selectedYear = this.now.get(GregorianCalendar.YEAR);
        private int selectedMonth = this.now.get(GregorianCalendar.MONTH);
        private int selectedDay = this.now.get(GregorianCalendar.DAY_OF_MONTH);
        private int selectedHour = this.now.get(GregorianCalendar.HOUR_OF_DAY);
        private int selectedMinute = this.now.get(GregorianCalendar.MINUTE);
        private int selectedSeconds = this.now.get(GregorianCalendar.SECOND);


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View dateTimePickerDialog = getActivity().getLayoutInflater().inflate(R.layout.datetimepicker_templatedialog, null); //todo does this work despite missing layoutparams?
            this.setDatePicker((DatePicker) dateTimePickerDialog.findViewById(R.id.datePicker));
            this.setTimePicker((TimePicker) dateTimePickerDialog.findViewById(R.id.timePicker));

            //initiate datetime picker (set also current date)
            this.getDatePicker().init(
                    this.getSelectedYear(),
                    this.getSelectedMonth(),
                    this.getSelectedDay(),
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            setSelectedYear(year);
                            setSelectedMonth(monthOfYear);
                            setSelectedDay(dayOfMonth);
                            updateDateTime();
                        }
                    }
            );

            //Timepicker initiating (set also current time)
            this.getTimePicker().setCurrentHour(this.now.get(GregorianCalendar.HOUR_OF_DAY));
            this.getTimePicker().setCurrentMinute(this.now.get(GregorianCalendar.MINUTE));
            this.getTimePicker().setCurrentSecond(this.now.get(GregorianCalendar.SECOND));
            this.getTimePicker().setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute, int seconds) {
                    setSelectedHour(hourOfDay);
                    setSelectedMinute(minute);
                    setSelectedSeconds(seconds);
                    updateDateTime();
                }
            });

            //Initially set current member time date stamps
            updateDateTime();

            return (new DialogMgr(getActivity())).createDialog(
                    dateTimePickerDialog,
                    getResources().getString(R.string.dateTimePicker_dialog_title),
                    "", null, null, R.drawable.coloraccent_datetimepicker_icon, //do not provide null as msg, otherwise the default error msg will be shown, we only want the view as content
                    null
            );
        }

        /**
         * Because this method is now called in onChangedListeners we don't have to take care whether dialog gets closed or multiple tapped on ok etc. :)
         */
        private void updateDateTime() {
            String updatedDateTime = String.format(getActivity().getResources().getString(R.string.dateTimePicker_textView_format_dateTime),
                    String.format(Constants.GLOBAL.LOCALE, "%02d", this.getSelectedDay()),
                    String.format(Constants.GLOBAL.LOCALE, "%02d", this.getSelectedMonth()),
                    String.format(Constants.GLOBAL.LOCALE, "%02d", this.getSelectedYear()),
                    String.format(Constants.GLOBAL.LOCALE, "%02d", this.getSelectedHour()),
                    String.format(Constants.GLOBAL.LOCALE, "%02d", this.getSelectedMinute()),
                    String.format(Constants.GLOBAL.LOCALE, "%02d", this.getSelectedSeconds()));
            this.getTargetTextView().setText(updatedDateTime);
            Log.d(TAG, "updateDateTime: Tried to update TextView bc. date or time picker got changed: "+updatedDateTime);
        }


        //GETTER/SETTER ------------------------
        public DatePicker getDatePicker() {
            return datePicker;
        }

        public void setDatePicker(DatePicker datePicker) {
            this.datePicker = datePicker;
        }

        public TimePicker getTimePicker() {
            return timePicker;
        }

        public void setTimePicker(TimePicker timePicker) {
            this.timePicker = timePicker;
        }

        public TextView getTargetTextView() {
            return targetTextView;
        }

        public void setTargetTextView(TextView targetTextView) {
            this.targetTextView = targetTextView;
        }

        public int getSelectedYear() {
            return selectedYear;
        }

        public void setSelectedYear(int selectedYear) {
            this.selectedYear = selectedYear;
        }

        public int getSelectedMonth() {
            return selectedMonth;
        }

        public void setSelectedMonth(int selectedMonth) {
            this.selectedMonth = selectedMonth;
        }

        public int getSelectedDay() {
            return selectedDay;
        }

        public void setSelectedDay(int selectedDay) {
            this.selectedDay = selectedDay;
        }

        public int getSelectedHour() {
            return selectedHour;
        }

        public void setSelectedHour(int selectedHour) {
            this.selectedHour = selectedHour;
        }

        public int getSelectedMinute() {
            return selectedMinute;
        }

        public void setSelectedMinute(int selectedMinute) {
            this.selectedMinute = selectedMinute;
        }

        public int getSelectedSeconds() {
            return selectedSeconds;
        }

        public void setSelectedSeconds(int selectedSeconds) {
            this.selectedSeconds = selectedSeconds;
        }
    }
}
