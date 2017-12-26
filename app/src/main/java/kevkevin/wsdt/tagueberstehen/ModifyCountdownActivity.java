package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;

public class ModifyCountdownActivity extends AppCompatActivity {
    private String countdownTitle;
    private String countdownDescription;
    private String startDateTime;
    private String untilDateTime;
    private String category;
    private boolean isActive;
    private static final String TAG = "ModifyCountdownActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

    }

    public void onSaveClick(View view) {
        //Get values from form
        loadFormValues();
        InternalStorageMgr storageMgr = new InternalStorageMgr(this);
        storageMgr.setSaveCountdown(new Countdown(this,this.getCountdownTitle(),this.getCountdownDescription(),this.getStartDateTime(),this.getUntilDateTime(),this.getCategory(),this.isActive()),true);
        Log.d(TAG, "onSaveClick: Tried to save new countdown.");
    }

    public void onAbortClick(View view) {
        //Finish current activity and go back to main page
        finish();
    }

    private void validateFormValues() {
        //Validate dates
        if (this.getStartDateTime().matches(Countdown.DATE_FORMAT_REGEX) && this.getUntilDateTime().matches(Countdown.DATE_FORMAT_REGEX)) {
            //TODO: (new Countdown(this,)).savePersistently();
            //TODO: new constructor in countdown
        } else {
            Log.e(TAG, "validateFormValue: Dates not valid: "+this.getStartDateTime()+" /// "+this.getUntilDateTime());
        }
    }

    private void loadFormValues() {
        this.setCountdownTitle(((TextView) findViewById(R.id.countdownTitleValue)).getText().toString());
        this.setCountdownDescription(((TextView) findViewById(R.id.countdownDescriptionValue)).getText().toString());
        this.setStartDateTime(((TextView) findViewById(R.id.startDateTimeValue)).getText().toString());
        this.setUntilDateTime(((TextView) findViewById(R.id.untilDateTimeValue)).getText().toString());
        this.setCategory(((TextView) findViewById(R.id.categoryValue)).getText().toString());
    }

    //GETTER/SETTER -----------------------------------------------------
    public String getCountdownTitle() {
        return countdownTitle;
    }

    public void setCountdownTitle(String countdownTitle) {
        this.countdownTitle = countdownTitle;
    }

    public String getCountdownDescription() {
        return countdownDescription;
    }

    public void setCountdownDescription(String countdownDescription) {
        this.countdownDescription = countdownDescription;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getUntilDateTime() {
        return untilDateTime;
    }

    public void setUntilDateTime(String untilDateTime) {
        this.untilDateTime = untilDateTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
