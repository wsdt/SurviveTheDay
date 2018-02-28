package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Arrays;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;

public class AppSettingsActivity extends AppCompatActivity {
    private GlobalAppSettingsMgr globalAppSettingsMgr;
    private static final String TAG = "AppSettingsActivity";
    private Switch saveBattery;
    private Spinner inappNotificationShowDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        //Ads ---------------------------------------------
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.settingsRLforAd));

        //declare storagemgr
        this.globalAppSettingsMgr = new GlobalAppSettingsMgr(this);
        this.saveBattery = findViewById(R.id.saveBattery);
        this.inappNotificationShowDuration = findViewById(R.id.inappNotificationHowLongToShowValue);

        //Set spinner properties (inappnotification duration show)
        HelperClass.setIntervalSpinnerConfigurations(this.inappNotificationShowDuration, R.array.inAppNotificationShowSpinner_LABELS_VALUES,0);

        //Load current settings before attaching listeners etc.
        loadCurrentSettings();

        setCustomListeners();
    }

    private void enableDisableBatteryFields(boolean enabled) {
        //Hide battery field if forward compatibility off or reverse (because batterysaving does not do anything if deactivated)
        int viewVisibility = (enabled) ? View.VISIBLE : View.GONE; //use gone to make space
        findViewById(R.id.saveBattery).setVisibility(viewVisibility);
        findViewById(R.id.saveBatteryDescription).setVisibility(viewVisibility);
        findViewById(R.id.saveBatteryLbl).setVisibility(viewVisibility);
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
}
