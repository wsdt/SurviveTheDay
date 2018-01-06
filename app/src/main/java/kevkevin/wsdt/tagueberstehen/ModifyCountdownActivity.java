package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Arrays;
import java.util.GregorianCalendar;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.DateTimePicker;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;

public class ModifyCountdownActivity extends AppCompatActivity {
    private Countdown newEditedCountdown;
    private static final String TAG = "ModifyCountdownActivity";
    private int existingCountdownId = (-1); //if edit then this value will be updated and used to overwrite existing countdown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

        //ADS - START
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.wrappingRLForAdsModifyCountdowns));
        //ADS - END

        //Set custom onclick listener so time and datepicker show up
        setCustomOnClickListener(findViewById(R.id.startDateTimeValue));
        setCustomOnClickListener(findViewById(R.id.untilDateTimeValue));

        //Set List for intervalsetter (spinner)
        setIntervalSpinnerConfigurations();


        try {
            this.existingCountdownId = getIntent().getIntExtra("COUNTDOWN_ID",-1);
            //say that we want to edit an existing countdown and not create a new one
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Could not load existing countdown.");
        }

        if (this.existingCountdownId >= 0) {
            setFormValues((new InternalStorageMgr(this).getCountdown(this.existingCountdownId)));
        }

        onMotivateMeToggleClick(findViewById(R.id.isActive)); //simulate click so it is always at its correct state (enabled/disabled)
    }

    public void onSaveClick(View view) {
        //Get values from form
        loadFormValues();
        if (areFormValuesValid()) {
            new InternalStorageMgr(this).setSaveCountdown(this.getNewEditedCountdown(), true);
            Log.d(TAG, "onSaveClick: Tried to save new countdown.");
            finish(); //go back to main
        } else {
            //Toast.makeText(this, "Value(s) not allowed!", Toast.LENGTH_SHORT).show(); --> toasts in areFormValuesValid() more specific!
            Log.w(TAG, "onSaveClick: areFormValuesValid() delivered not true!");
        }
    }

    public void onAbortClick(View view) {
        //Finish current activity and go back to main page
        finish();
    }

    private boolean areFormValuesValid() {
        boolean areFormValuesValid = true;
        //Validate dates
        if (this.getNewEditedCountdown().getStartDateTime().matches(Countdown.DATE_FORMAT_REGEX) && this.getNewEditedCountdown().getUntilDateTime().matches(Countdown.DATE_FORMAT_REGEX)) {
            // Is UntilDateTime AFTER StartDateTime? -------------------
            //getDateTime(getStartDateTime()).compareTo(getCurrentDateTime()) > 0
            if (getNewEditedCountdown().getDateTime(getNewEditedCountdown().getStartDateTime())
                    .compareTo(getNewEditedCountdown().getDateTime(getNewEditedCountdown().getUntilDateTime())) >= 0) {
                //startdatetime is in "future" is bigger than untildatetima (bad)
                Toast.makeText(this, "UntilDateTime needs to be AFTER StartDateTime!", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "areFormValuesValid: UntilDateTime needs to be AFTER StartDateTime.");
                areFormValuesValid = false;
            }
            // Is UntilDateTime AFTER StartDateTime? - END -------------

            //TODO: ADD HERE FURTHER VALIDATIONS
        } else {
            Log.e(TAG, "validateFormValue: Dates not valid: "+this.getNewEditedCountdown().getStartDateTime()+" /// "+this.getNewEditedCountdown().getUntilDateTime());
            Toast.makeText(this,"DateTime is not valid! This might be an internal error. ", Toast.LENGTH_LONG).show();
        }
        return areFormValuesValid;
    }

    private void setFormValues(Countdown countdown) {
        ((TextView) findViewById(R.id.countdownTitleValue)).setText(countdown.getCountdownTitle());
        ((TextView) findViewById(R.id.countdownDescriptionValue)).setText(countdown.getCountdownDescription());
        ((TextView) findViewById(R.id.startDateTimeValue)).setText(countdown.getStartDateTime());
        ((TextView) findViewById(R.id.untilDateTimeValue)).setText(countdown.getUntilDateTime());
        ((TextView) findViewById(R.id.categoryValue)).setText(countdown.getCategory());
        ((ToggleButton) findViewById(R.id.isActive)).setChecked(countdown.isActive());
        //set associated entry of interval seconds to spinner
        ((Spinner) findViewById(R.id.notificationIntervalSpinner)).setSelection(Arrays.asList(getResources().getStringArray(R.array.countdownIntervalSpinner_VALUES)).indexOf(""+countdown.getNotificationInterval())); //reduce about 5 otherwise we would add 5 every time we edited it!
    }

    private void loadFormValues() {
        this.setNewEditedCountdown(new Countdown(this,
                ((TextView) findViewById(R.id.countdownTitleValue)).getText().toString(),
                ((TextView) findViewById(R.id.countdownDescriptionValue)).getText().toString(),
                ((TextView) findViewById(R.id.startDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.untilDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.categoryValue)).getText().toString(),
                ((ToggleButton) findViewById(R.id.isActive)).isChecked(),
                Integer.parseInt(getResources().getStringArray(R.array.countdownIntervalSpinner_VALUES)[((Spinner) findViewById(R.id.notificationIntervalSpinner)).getSelectedItemPosition()]))); //.getProgress()+5 for old seekbar slider +5 seconds by default (because if 0) app crashes
                //line above: gets selected spinner items position and uses this to get the associated array entry with the correct value in seconds.

        //Overwrite countdown id if countdown exists already
        if (this.existingCountdownId >= 0) {
            this.getNewEditedCountdown().setCountdownId(this.existingCountdownId);
            Log.d(TAG, "loadFormValues: Overwrote/Edited countdown.");
        } else {
            Log.d(TAG, "loadFormValues: New countdown created.");
        }
    }

    //Show service specific field if toggle button is ON (isActive)
    public void onMotivateMeToggleClick(View view) {
        //Add/Remove field(s) if countdown should run in background
        boolean tbIsChecked = ((ToggleButton) view).isChecked();

        //disable/enable fields (if toggle button is checked = active then buttons should be enabled. otherwise it is false
        findViewById(R.id.notificationIntervalTextView).setEnabled(tbIsChecked);
        findViewById(R.id.notificationIntervalSpinner).setEnabled(tbIsChecked);
    }

    private void setIntervalSpinnerConfigurations() {
        Spinner spinner = (Spinner) findViewById(R.id.notificationIntervalSpinner);
        //Create an arrayadapter using string array from strings.xml and default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countdownIntervalSpinner_LABELS, R.layout.spinner_intervall_item);
        //specify layout to use when list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply adapter to spinner
        spinner.setAdapter(adapter);
    }

    //GETTER/SETTER -----------------------------------------------------
    public Countdown getNewEditedCountdown() {
        return newEditedCountdown;
    }

    public void setNewEditedCountdown(Countdown newEditedCountdown) {
        this.newEditedCountdown = newEditedCountdown;
    }


    // ################################################################################################################
    // TIMER/DATE PICKER ##############################################################################################
    // ################################################################################################################

    public void setCustomOnClickListener(View v) {
        //GregorianCalendar now = new GregorianCalendar(); //now
        final DateTimePicker DATETIMEPICKER = new DateTimePicker(this, getSupportFragmentManager(),(TextView) v,GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND, true);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATETIMEPICKER.showDateTimePicker();
            }
        });
    }
}
