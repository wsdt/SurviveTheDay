package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.reward.RewardedVideoAd;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;

public class RewardedVideoAdActivity extends AppCompatActivity {
    //This Activity is only used to show rewarded video ads!
    private RewardedVideoAd mRewardedVideoAd;
    private AdManager adManager;
    private static final String TAG = "RewardedVideoAdActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewarded_video_ad);

        this.setAdManager(new AdManager(this));
        this.getAdManager().initializeAdmob();
        //redirect to mainActivity after ad
        this.setmRewardedVideoAd(this.getAdManager().loadRewardedVideoInRewardActivity(null,null));
    }


    //Prevent skipping of ad (reacting to activity lifecycle etc.)
    @Override
    public void onResume() {
        this.getmRewardedVideoAd().resume(this);
        super.onResume();
        Log.d(TAG, "onResume: Continued rewarded video.");
    }

    @Override
    public void onPause() {
        this.getmRewardedVideoAd().pause(this);
        super.onPause();
        Log.d(TAG, "onPause: Paused rewarded video.");
    }

    @Override
    public void onDestroy() {
        this.getmRewardedVideoAd().destroy(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy: Destroyed rewarded video.");
    }

    //Getter/Setter
    public RewardedVideoAd getmRewardedVideoAd() {
        return mRewardedVideoAd;
    }

    public void setmRewardedVideoAd(RewardedVideoAd mRewardedVideoAd) {
        this.mRewardedVideoAd = mRewardedVideoAd;
    }

    public AdManager getAdManager() {
        return adManager;
    }

    public void setAdManager(AdManager adManager) {
        this.adManager = adManager;
    }
}
