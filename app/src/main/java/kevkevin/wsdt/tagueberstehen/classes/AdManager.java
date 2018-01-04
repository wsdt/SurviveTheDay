package kevkevin.wsdt.tagueberstehen.classes;


import android.content.Context;
import android.content.Intent;
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

public class AdManager {
    private Context context;
    private static final String TAG = "AdManager";


    //TODO: für internet permission prüfen und verlangen usw.
    public AdManager(Context context) {
        this.setContext(context);
    }

    public void initializeAdmob() {
        MobileAds.initialize(this.getContext(), "ca-app-pub-8160960481527784~1956004763");
    }


    public void loadFullPageAd(@Nullable AdListener adListener, @Nullable final Intent goToActivityAfterShown) {
        final String FULLPAGE_ID = "ca-app-pub-3940256099942544/1033173712"; //testing purpose
        //TODO: final String FULLPAGE_ID = "ca-app-pub-8160960481527784/3526438439"; //real page id

        final InterstitialAd fullpageAd = new InterstitialAd(this.getContext());
        fullpageAd.setAdUnitId(FULLPAGE_ID);
        fullpageAd.loadAd(new AdRequest.Builder().build());
        fullpageAd.setAdListener((adListener == null) ? new AdListener() {
            @Override
            public void onAdLoaded() {
                //just in case validate whether app is loaded
                if (fullpageAd.isLoaded()) {
                    fullpageAd.show(); //show ad if loaded
                    Log.e(TAG, "onAdLoaded: Tried to show fullpage ad.");
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
                Toast.makeText(getContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onAdFailedToLoad: Could not load interstitial ad. Errorcode: "+errorcode);
                if (goToActivityAfterShown != null) {
                    Log.d(TAG, "onAdClosed: gotoActivity is not null.");
                    getContext().startActivity(goToActivityAfterShown); //does app not prevent from being executed without internet
                }
            }
        } : adListener); //IMPORTANT: add given adListener, if null create default one
    }

    public void loadBannerAd(final RelativeLayout viewGroup) {
        final String BANNER_ID = "ca-app-pub-3940256099942544/6300978111"; //testing purpose
        //TODO: final String BANNER_ID = "ca-app-pub-8160960481527784/8464916252"; //real banner!!!!

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
                Toast.makeText(getContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onAdFailedToLoad (loadBannerAd): Banner could not be loaded.");
            }

            @Override
            public void onAdLoaded() {
                viewGroup.removeView(adView);
                viewGroup.addView(adView); //add to layout if loaded
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
}
