package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;

public class ModifyCountdownActivity extends AppCompatActivity {
    private Countdown newEditedCountdown;
    private static final String TAG = "ModifyCountdownActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

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

    private void loadFormValues() {

        this.setNewEditedCountdown(new Countdown(this,
                ((TextView) findViewById(R.id.countdownTitleValue)).getText().toString(),
                ((TextView) findViewById(R.id.countdownDescriptionValue)).getText().toString(),
                ((TextView) findViewById(R.id.startDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.untilDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.categoryValue)).getText().toString(),
                ((ToggleButton) findViewById(R.id.isActive)).isChecked(),
                5)); //TODO: load interval from form (create field)
    }

    //GETTER/SETTER -----------------------------------------------------
    public Countdown getNewEditedCountdown() {
        return newEditedCountdown;
    }

    public void setNewEditedCountdown(Countdown newEditedCountdown) {
        this.newEditedCountdown = newEditedCountdown;
    }
}
