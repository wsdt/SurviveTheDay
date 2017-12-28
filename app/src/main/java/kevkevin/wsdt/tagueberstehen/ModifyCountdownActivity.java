package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;

public class ModifyCountdownActivity extends AppCompatActivity {
    private Countdown newEditedCountdown;
    private static final String TAG = "ModifyCountdownActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

        int countdownId = (-2);
        try {
            countdownId = getIntent().getIntExtra("COUNTDOWN_ID",-1);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Could not load existing countdown.");
        }

        if (countdownId >= 0) {
            setFormValues((new InternalStorageMgr(this).getCountdown(countdownId)));
        }
    }

    public void onSaveClick(View view) {
        //Get values from form
        loadFormValues();
        validateFormValues();
        new InternalStorageMgr(this).setSaveCountdown(this.getNewEditedCountdown(),true);
        Log.d(TAG, "onSaveClick: Tried to save new countdown.");
        finish(); //go back to main
    }

    public void onAbortClick(View view) {
        //Finish current activity and go back to main page
        finish();
    }

    private void validateFormValues() {
        //Validate dates
        if (this.getNewEditedCountdown().getStartDateTime().matches(Countdown.DATE_FORMAT_REGEX) && this.getNewEditedCountdown().getUntilDateTime().matches(Countdown.DATE_FORMAT_REGEX)) {
            //TODO: (new Countdown(this,)).savePersistently();
            //TODO: new constructor in countdown
        } else {
            Log.e(TAG, "validateFormValue: Dates not valid: "+this.getNewEditedCountdown().getStartDateTime()+" /// "+this.getNewEditedCountdown().getUntilDateTime());
        }
    }

    private void setFormValues(Countdown countdown) {
        ((TextView) findViewById(R.id.countdownTitleValue)).setText(countdown.getCountdownTitle());
        ((TextView) findViewById(R.id.countdownDescriptionValue)).setText(countdown.getCountdownDescription());
        ((TextView) findViewById(R.id.startDateTimeValue)).setText(countdown.getStartDateTime());
        ((TextView) findViewById(R.id.untilDateTimeValue)).setText(countdown.getUntilDateTime());
        ((TextView) findViewById(R.id.categoryValue)).setText(countdown.getCategory());
        ((ToggleButton) findViewById(R.id.isActive)).setChecked(countdown.isActive());
        ((SeekBar) findViewById(R.id.notificationIntervalSeekBar)).setProgress(countdown.getNotificationInterval()-5); //reduce about 5 otherwise we would add 5 every time we edited it!
    }

    private void loadFormValues() {
        this.setNewEditedCountdown(new Countdown(this,
                ((TextView) findViewById(R.id.countdownTitleValue)).getText().toString(),
                ((TextView) findViewById(R.id.countdownDescriptionValue)).getText().toString(),
                ((TextView) findViewById(R.id.startDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.untilDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.categoryValue)).getText().toString(),
                ((ToggleButton) findViewById(R.id.isActive)).isChecked(),
                ((SeekBar) findViewById(R.id.notificationIntervalSeekBar)).getProgress()+5)); //+5 seconds by default (because if 0) app crashes
    }

    //Show service specific field if toggle button is ON (isActive)
    public void onMotivateMeToggleClick(View view) {
        //Add/Remove field(s) if countdown should run in background
        boolean tbIsChecked = ((ToggleButton) view).isChecked();

        //disable/enable fields (if toggle button is checked = active then buttons should be enabled. otherwise it is false
        findViewById(R.id.notificationIntervalTextView).setEnabled(tbIsChecked);
        findViewById(R.id.notificationIntervalSeekBar).setEnabled(tbIsChecked);

    }

    //GETTER/SETTER -----------------------------------------------------
    public Countdown getNewEditedCountdown() {
        return newEditedCountdown;
    }

    public void setNewEditedCountdown(Countdown newEditedCountdown) {
        this.newEditedCountdown = newEditedCountdown;
    }
}
