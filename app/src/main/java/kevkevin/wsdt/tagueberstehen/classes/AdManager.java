package kevkevin.wsdt.tagueberstehen.classes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;

import static com.google.android.gms.ads.AdRequest.ERROR_CODE_NETWORK_ERROR;

public class AdManager {
    private Context context;
    private static final String TAG = "AdManager";
    private GlobalAppSettingsMgr globalAppSettingsMgr;
    private InAppPurchaseManager_newUsedHelper inAppPurchaseManager;


    //TODO: für internet permission prüfen und verlangen usw.
    public AdManager(Context context) {
        this.setContext(context);
        this.setGlobalAppSettingsMgr(new GlobalAppSettingsMgr(context));
    }

    public void initializeAdmob() {
        if (!this.getGlobalAppSettingsMgr().isRemoveAdsTemporarlyInMinutesActiveValid()) {
            MobileAds.initialize(this.getContext(), Constants.ADMANAGER.ADMOB_USER_ID);
        } else {
            Log.d(TAG, "initializeAdMob: Did not initialize admob, because app is temporarily ad free. [Important, that this method has lower or at maximum the same constraints as loadAd() methods!!]");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        //On destroy for object
        this.getInAppPurchaseManager().unbindIabHelper(); //remove it when throwing away instance
    }

    public RewardedVideoAd loadRewardedVideoInRewardActivity(@NonNull final Activity activityContext, @Nullable RewardedVideoAdListener adListener, @Nullable final Intent goToActivityAfterShown) {
        final String REWARDED_VIDEO_ID = Constants.ADMANAGER.USE_TEST_ADS ? Constants.ADMANAGER.TEST.REWARDED_VIDEO_AD_ID : Constants.ADMANAGER.REAL.REWARDED_VIDEO_AD_ID;

        final RewardedVideoAd rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activityContext);
        rewardedVideoAd.setRewardedVideoAdListener((adListener == null) ? new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                //just in case validate whether app is loaded
                if (rewardedVideoAd.isLoaded()) {
                    rewardedVideoAd.show(); //show ad if loaded
                    Log.d(TAG, "onRewardedVideoAdLoaded: Tried to show rewarded video ad.");
                } else {
                    Log.e(TAG, "onRewardedVideoAdLoaded: This error should not happen.");
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d(TAG, "onRewardedVideoAdOpened: Opened Rewarded video ad and have informed user.");
                Toast.makeText(activityContext, R.string.adManager_loadRewardedVideoInRewardActivity_reward_introductionMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                //if ad closed go to gotoActivity
                Log.d(TAG, "onRewardedVideoAdClosed: Rewarded video ad got closed and new activity got started.");
                if (goToActivityAfterShown != null) {
                    Log.d(TAG, "onRewardedVideoAdClosed: gotoActivity is not null.");
                    getContext().startActivity(goToActivityAfterShown);
                } else {
                    //IMPORTANT: In this case (because this ad has its own activity) we finish the activity if no target activity is specified.
                    Log.d(TAG, "onRewardedVideoAdClosed: Tried to finish activity.");
                    activityContext.finish();
                }
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                /*if ((rewardItem.getAmount()*60*1000) != Constants.ADMANAGER.REMOVE_ADS_TEMPORARLY_IN_MILLISECONDS) {
                    Toast.makeText(activityContext, R.string.error_contactAdministrator, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onRewarded: Could not reward user, because rewardItem-Amount and Constants-Value is different! Maybe adapt on Google Play console");
                } else {*/
                    getGlobalAppSettingsMgr().setRemoveAdsTemporarlyInMinutes(rewardItem.getAmount());
                    Toast.makeText(activityContext, String.format(getContext().getResources().getString(R.string.adManager_loadRewardedVideoInRewardActivity_reward_successMessage),rewardItem.getAmount()), Toast.LENGTH_LONG).show();
                //}
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorcode) {
                Toast.makeText(getContext(), R.string.error_noInternetConnection, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onAdFailedToLoad: Could not load interstitial ad. Errorcode: "+errorcode);
                if (errorcode == ERROR_CODE_NETWORK_ERROR) {
                    //only error code where user might be the reason so increment counter
                    getGlobalAppSettingsMgr().incrementNoInternetConnectionCounter();
                    Log.d(TAG, "onAdFailedToLoad: Tried to increment noInternetConnectionCounter.");
                }

                if (goToActivityAfterShown != null) {
                    Log.d(TAG, "onAdClosed: gotoActivity is not null.");
                    activityContext.startActivity(goToActivityAfterShown); //does app not prevent from being executed without internet
                } else {
                    //IMPORTANT: In this case (because this ad has its own activity) we finish the activity if no target activity is specified.
                    Log.d(TAG, "onRewardedVideoAdFailedToLoad: Tried to finish activity.");
                    activityContext.finish();
                }
            }
        } : adListener); //add given listener or create default one
        Log.d(TAG, "loadRewardedVideoInRewardActivity: Created reward video instance etc.");

        rewardedVideoAd.loadAd(REWARDED_VIDEO_ID,new AdRequest.Builder().build());
        Log.d(TAG, "loadRewardedVideoInRewardActivity: Tried to load rewarded video.");

        return rewardedVideoAd;
    }


    public void loadFullPageAd(@Nullable AdListener adListener, @Nullable final Intent goToActivityAfterShown) {
        if (getGlobalAppSettingsMgr().isRemoveAdsTemporarlyInMinutesActiveValid()) {
            Log.d(TAG, "loadFullPageAd: Did not show ad, because temporarly ad free! Redirecting to next activity if given.");
            if (goToActivityAfterShown != null) {
                Log.d(TAG, "loadFullPageAd: Hid ad, redirecting to next activity.");
                this.getContext().startActivity(goToActivityAfterShown);
            }
            return;
        }

        //IMPORTANT: ADMOB-GUIDELINE only place interestials between activities with contents and not too much!! Showing Fullpage Ad only allowed if loadingActivity shows BEFORE ad! (see: https://support.google.com/admob/answer/6201362?hl=de&ref_topic=2745287)
        final String FULLPAGE_ID = Constants.ADMANAGER.USE_TEST_ADS ? Constants.ADMANAGER.TEST.INTERSTITIAL_AD_ID : Constants.ADMANAGER.REAL.INTERSTITIAL_AD_ID;

        final InterstitialAd fullpageAd = new InterstitialAd(this.getContext());
        fullpageAd.setAdUnitId(FULLPAGE_ID);
        fullpageAd.loadAd(new AdRequest.Builder().build());
        fullpageAd.setAdListener((adListener == null) ? new AdListener() {
            @Override
            public void onAdLoaded() {
                //just in case validate whether app is loaded
                if (fullpageAd.isLoaded()) {
                    fullpageAd.show(); //show ad if loaded
                    Log.d(TAG, "onAdLoaded: Tried to show fullpage ad.");
                } else {
                    Log.e(TAG, "onAdLoaded: This error should not happen.");
                }
            }

            @Override
            public void onAdClosed() {
                //if ad closed go to gotoActivity
                Log.d(TAG, "onAdClosed: Interstitial ad got closed and new activity got started.");
                if (goToActivityAfterShown != null) {
                    Log.d(TAG, "onAdClosed: gotoActivity is not null.");
                    getContext().startActivity(goToActivityAfterShown);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorcode) {
                Toast.makeText(getContext(), R.string.error_noInternetConnection, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onAdFailedToLoad: Could not load interstitial ad. Errorcode: "+errorcode);
                if (errorcode == ERROR_CODE_NETWORK_ERROR) {
                    //only error code where user might be the reason so increment counter
                    getGlobalAppSettingsMgr().incrementNoInternetConnectionCounter();
                    Log.d(TAG, "onAdFailedToLoad: Tried to increment noInternetConnectionCounter.");
                }

                if (goToActivityAfterShown != null) {
                    Log.d(TAG, "onAdClosed: gotoActivity is not null.");
                    getContext().startActivity(goToActivityAfterShown); //does app not prevent from being executed without internet
                }
            }
        } : adListener); //IMPORTANT: add given adListener, if null create default one
    }

    public void loadBannerAd(final RelativeLayout viewGroup) {
        if (getGlobalAppSettingsMgr().isRemoveAdsTemporarlyInMinutesActiveValid()) {
            Log.d(TAG, "loadBannerAd: Did not show ad, because temporarly ad free!");
            return;
        }

        final String BANNER_ID = Constants.ADMANAGER.USE_TEST_ADS ? Constants.ADMANAGER.TEST.BANNER_AD_ID : Constants.ADMANAGER.REAL.BANNER_AD_ID;

        final AdView adView = new AdView(this.getContext());
        adView.setAdSize(AdSize.SMART_BANNER); //IMPORTANT: adsize and adunit should be added in the same manner! (programmatically | xml)
        adView.setAdUnitId(BANNER_ID);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(lp);

        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorcode) {
                Toast.makeText(getContext(), R.string.error_noInternetConnection, Toast.LENGTH_SHORT).show();
                if (errorcode == ERROR_CODE_NETWORK_ERROR) {
                    //only error code where user might be the reason so increment counter
                    getGlobalAppSettingsMgr().incrementNoInternetConnectionCounter();
                    Log.d(TAG, "onAdFailedToLoad: Tried to increment noInternetConnectionCounter.");
                }
                Log.e(TAG, "onAdFailedToLoad (loadBannerAd): Banner could not be loaded.");
            }

            @Override
            public void onAdLoaded() {
                viewGroup.removeView(adView);
                viewGroup.addView(adView); //add to layout if loaded
                Log.d(TAG, "onAdLoaded (loadBannerAd): Banner successfully loaded.");
            }
        });
    }


    // GETTER / SETTER ---------------------------
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public GlobalAppSettingsMgr getGlobalAppSettingsMgr() {
        return globalAppSettingsMgr;
    }

    public void setGlobalAppSettingsMgr(GlobalAppSettingsMgr globalAppSettingsMgr) {
        this.globalAppSettingsMgr = globalAppSettingsMgr;
    }

    public InAppPurchaseManager_newUsedHelper getInAppPurchaseManager() {
        return inAppPurchaseManager;
    }

    public void setInAppPurchaseManager(InAppPurchaseManager_newUsedHelper inAppPurchaseManager) {
        this.inAppPurchaseManager = inAppPurchaseManager;
    }
}
