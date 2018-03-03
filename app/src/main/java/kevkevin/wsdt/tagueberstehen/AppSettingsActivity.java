package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Arrays;

import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppNotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.GlobalAppSettingsMgr;

public class AppSettingsActivity extends AppCompatActivity {
    private GlobalAppSettingsMgr globalAppSettingsMgr;
    private static final String TAG = "AppSettingsActivity";
    private Switch saveBattery;
    private Spinner inappNotificationShowDuration;
    private InAppNotificationMgr inAppNotificationMgr = new InAppNotificationMgr(); //must be a member! (to prevent influencing iapnotifications of other activities)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        //Ads ---------------------------------------------
        AdMgr adMgr = new AdMgr(this);
        adMgr.initializeAdmob();
        adMgr.loadBannerAd((RelativeLayout) findViewById(R.id.settingsRLforAd));

        //declare storagemgr
        this.globalAppSettingsMgr = new GlobalAppSettingsMgr(this);
        this.saveBattery = findViewById(R.id.saveBattery);
        this.inappNotificationShowDuration = findViewById(R.id.inappNotificationHowLongToShowValue);

        //Set spinner properties (inappnotification_template duration show)
        HelperClass.setIntervalSpinnerConfigurations(this.inappNotificationShowDuration, R.array.inAppNotificationShowSpinner_LABELS_VALUES,0);

        //Load current settings before attaching listeners etc.
        loadCurrentSettings();

        setCustomListeners();
    }

    private void setCustomListeners() {
        this.saveBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                globalAppSettingsMgr.setSaveBattery(b);
                Log.d(TAG, "setCustomListeners: Set saveBattery.");
            }
        });

        this.inappNotificationShowDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                globalAppSettingsMgr.setInAppNotificationShowDurationInS(Integer.parseInt(adapterView.getItemAtPosition(pos).toString()));
                Log.d(TAG, "onItemSelected: Saved selected inappsetting.");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: inAppNotificationShowDuration nothing selected.");
            }
        });
    }

    private void loadCurrentSettings() {
        this.saveBattery.setChecked(this.globalAppSettingsMgr.saveBattery());
        this.inappNotificationShowDuration.setSelection(Arrays.asList(getResources().getStringArray(R.array.inAppNotificationShowSpinner_LABELS_VALUES)).indexOf(""+(this.globalAppSettingsMgr.getInAppNotificationShowDurationInMs()/1000)));

        Log.d(TAG, "loadCurrentSettings: Loaded current settings.");
    }

    public void onHelpClick(View view) {
        this.getInAppNotificationMgr().showQuestionMarkHelpText(this, view, (ViewGroup) findViewById(R.id.settingsRLforAd));
    }

    //GETTER/SETTER -----------------------
    public InAppNotificationMgr getInAppNotificationMgr() {
        return inAppNotificationMgr;
    }
}
