package kevkevin.wsdt.tagueberstehen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.reward.RewardedVideoAd;

import kevkevin.wsdt.tagueberstehen.annotations.Test;
import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.interfaces.IGlobal;

@Test(message = "Worked, but I am not sure if it still works correctly [please test with/without internet connection]",
priority = Test.Priority.LOW,byDeveloper = IGlobal.DEVELOPERS.WSDT)
public class RewardedVideoAdActivity extends AppCompatActivity {
    //This Activity is only used to show rewarded video ads!
    private RewardedVideoAd mRewardedVideoAd;
    private AdMgr adMgr;
    private static final String TAG = "RewardedVideoAdActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewarded_video_ad);

        this.setAdMgr(new AdMgr(this));
        //redirect to mainActivity after ad
        this.setmRewardedVideoAd(this.getAdMgr().loadRewardedVideoInRewardActivity(null,null));
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

    public AdMgr getAdMgr() {
        return adMgr;
    }

    public void setAdMgr(AdMgr adMgr) {
        this.adMgr = adMgr;
    }
}
