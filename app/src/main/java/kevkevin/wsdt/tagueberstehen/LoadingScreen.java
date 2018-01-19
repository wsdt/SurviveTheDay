package kevkevin.wsdt.tagueberstehen;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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

        //Create blinking effect
        final ImageView appIcon = ((ImageView) findViewById(R.id.appIcon));
        ObjectAnimator animator = ObjectAnimator.ofFloat(appIcon, "alpha", 0.25f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();

        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadFullPageAd(null, new Intent(this, MainActivity.class)); //after ad is closed or failure happens the maiActivity starts.
    }
}
