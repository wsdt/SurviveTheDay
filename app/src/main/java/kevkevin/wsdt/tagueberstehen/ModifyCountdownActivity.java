package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;

public class ModifyCountdownActivity extends AppCompatActivity {
    private String countdownTitle;
    private String countdownDescription;
    private String startDateTime;
    private String untilDateTime;
    private String category;
    private static final String TAG = "ModifyCountdownActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

        //TODO: createCountdownAct.putExtra("CRUD","C"); --> get extra each letter (except read)
    }

    public void onSaveClick(View view) {
        //Get values from form
        loadFormValues();


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
}
