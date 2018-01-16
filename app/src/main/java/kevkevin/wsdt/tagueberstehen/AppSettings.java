package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;

public class AppSettings extends AppCompatActivity {
    private GlobalAppSettingsMgr globalAppSettingsMgr;
    private static final String TAG = "AppSettings";
    private Switch useForwardCompatibility;
    private Switch saveBattery;
    private TableRow saveBatteryRow;

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
        this.useForwardCompatibility = (Switch) findViewById(R.id.useForwardCompatibility);
        this.saveBattery = (Switch) findViewById(R.id.saveBattery);
        this.saveBatteryRow = (TableRow) findViewById(R.id.saveBattery_ROW);

        //Load current settings before attaching listeners etc.
        loadCurrentSettings();

        setCustomListeners();
    }

    private void enableDisableBatteryFields(boolean enabled) {
        for (int i = 0; i<saveBatteryRow.getChildCount(); i++) {
            saveBatteryRow.getChildAt(i).setEnabled(enabled);
        }
        findViewById(R.id.saveBatteryDescription).setEnabled(enabled);
    }

    private void setCustomListeners() {
        this.useForwardCompatibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                globalAppSettingsMgr.setUseForwardCompatibility(b);
                Log.d(TAG, "setCustomListeners: Set useForwardcompatibility.");

                enableDisableBatteryFields(b); //if enabled, then enable battery fields
            }
        });

        this.saveBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                globalAppSettingsMgr.setSaveBattery(b);
                Log.d(TAG, "setCustomListeners: Set saveBattery.");
            }
        });
    }

    private void loadCurrentSettings() {
        this.useForwardCompatibility.setChecked(this.globalAppSettingsMgr.useForwardCompatibility());
        this.saveBattery.setChecked(this.globalAppSettingsMgr.saveBattery());

        enableDisableBatteryFields(this.useForwardCompatibility.isChecked());

        Log.d(TAG, "loadCurrentSettings: Loaded current settings.");
    }
}
