package kevkevin.wsdt.tagueberstehen.classes;


public class Constants {
    public interface ADMANAGER {
        boolean useTestAds = true;

        interface TEST {
            String BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111";
            String INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712";
        }
        interface REAL {
            String BANNER_AD_ID = "ca-app-pub-8160960481527784/8464916252";
            String INTERSTITIAL_AD_ID = "ca-app-pub-8160960481527784/3526438439";
        }
    }

    public interface COUNTDOWNCOUNTERSERVICE {
        /* 99 as base so each countdown can have its own notification (but within the same foreground service be changed
        * notification id for a countdown: (1000+countdownid) so..: countdown 0 = 1000, countdown 1 = 1001 etc. */
        int NOTIFICATION_ID = 1000;
    }
}
