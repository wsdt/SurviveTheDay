package kevkevin.wsdt.tagueberstehen.classes.manager.interfaces;

import kevkevin.wsdt.tagueberstehen.BuildConfig;

public interface IAdMgr {
    boolean USE_TEST_ADS = BuildConfig.BUILD_TYPE.equals("debug"); //if debug then true, otherwise false
    String ADMOB_USER_ID = "ca-app-pub-8160960481527784~1956004763";
    int NO_INTERNET_CONNECTION_MAX = 5; //after 5 ads which cannot be displayed notify user that this app gets financed by ads
    //Reward for rewarded video must be changed on Admob Console AND the following constant has to be the same Value!!!!

    interface TEST {
        String BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111";
        String INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712";
        String REWARDED_VIDEO_AD_ID = "ca-app-pub-3940256099942544/5224354917";
    }
    interface REAL {
        String BANNER_AD_ID = "ca-app-pub-8160960481527784/8464916252";
        String INTERSTITIAL_AD_ID = "ca-app-pub-8160960481527784/3526438439";
        String REWARDED_VIDEO_AD_ID = "ca-app-pub-8160960481527784/1260589901";
    }
}
