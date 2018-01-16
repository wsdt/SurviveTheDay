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

    private void setCustomListeners() {
        this.useForwardCompatibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                globalAppSettingsMgr.setUseForwardCompatibility(b);
                Log.d(TAG, "setCustomListeners: Set useForwardcompatibility.");

                if (!b) {
                    //if forward compatibility of then hide save battery setting
                    for (int i = 0; i<saveBatteryRow.getChildCount(); i++) {
                        saveBatteryRow.getChildAt(i).setEnabled(false);
                    }
                    findViewById(R.id.saveBatteryDescription).setEnabled(false);
                } else {
                    for (int i = 0; i<saveBatteryRow.getChildCount(); i++) {
                        saveBatteryRow.getChildAt(i).setEnabled(true);
                    }
                    findViewById(R.id.saveBatteryDescription).setEnabled(true);
                }
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
        Log.d(TAG, "loadCurrentSettings: Loaded current settings.");
    }
}
