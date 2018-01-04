package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;

public class LoadingScreen extends AppCompatActivity {
    private static final String TAG = "LoadingScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreate: Nullpointer on Actionbar!");
        }

        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadFullPageAd(null, new Intent(this, MainActivity.class)); //after ad is closed or failure happens the maiActivity starts.
    }
}
