package kevkevin.wsdt.tagueberstehen;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.services.CountdownCounterService;

public class LoadingScreenActivity extends AppCompatActivity {
    private static final String TAG = "LoadingScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreate: Nullpointer on Actionbar!");
        }

        //Create blinking effect
        final ImageView appIcon = ((ImageView) findViewById(R.id.appIcon));
        ObjectAnimator animator = ObjectAnimator.ofFloat(appIcon, "alpha", 0.25f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();


        //TODO:dismiss Progressbar from XML after leaving

        Class loadThatActivityAfterAdShown = MainActivity.class;
        try {
            if (getIntent().getIntExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID, -1) >= 0) {
                loadThatActivityAfterAdShown = CountdownActivity.class; //show countdown activity because from notification or similar called
                Log.d(TAG, "onCreate: Will call CountdownActivity instead of MainActivity.");
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Presumably LoadingScreenActivity not called from a notification. So intent is null or similar.");
            e.printStackTrace();
        }
        //Intent tmp = getIntent();
        //tmp.get
        Intent followingActivityIntent = new Intent(this, loadThatActivityAfterAdShown);
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            Bundle receivedExtras = receivedIntent.getExtras();
            if (receivedExtras != null) {
                //Put all extras (e.g. from notification) to new intent so we can pass it to countdownactivity e.g.
                followingActivityIntent.putExtras(receivedExtras);
                Log.d(TAG, "onCreate: Passed all received extras to new intent.");
            }
        }

        //######################### Remove from code! ################################
        //(new InAppPurchaseManager(this)).resetAllPurchasedItems();
        //(new GlobalAppSettingsMgr(this)).setRemoveAdsTemporarlyInMinutes(0); //to reset

        //FullPageAd etc.
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadFullPageAd(null, followingActivityIntent); //after ad is closed or failure happens the maiActivity starts.
    }

}
