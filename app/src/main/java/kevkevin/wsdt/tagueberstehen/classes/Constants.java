package kevkevin.wsdt.tagueberstehen.classes;


import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;

public class Constants {
    public interface GLOBAL {
        //FORMATTING PERCENTAGE and BIG NUMBERS or DATES
        Character THOUSAND_GROUPING_SEPERATOR = ',';
        String DATETIME_FORMAT = "dd.MM.yyyy hh:mm:ss";
        String DATETIME_FORMAT_REGEX = "\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}";
    }

    public interface ADMANAGER {
        boolean useTestAds = true;
        String ADMOB_USER_ID = "ca-app-pub-8160960481527784~1956004763";

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

    public interface CUSTOMNOTIFICATION {
        //LED light of notification (how long to blink on/off)
        int NOTIFICATION_BLINK_OFF_TIME_IN_MS = 1000;
        int NOTIFICATION_BLINK_ON_TIME_IN_MS = 1000;

        //CREATE NOTIFICATION ITSELF (IDENTIFIERS for PUTEXTRA in intents etc.)
        String NOTIFICATION_TICKER = "SurviveTheDay - Motivation";
        String IDENTIFIER_COUNTDOWN_ID = "COUNTDOWN_ID";
        String IDENTIFIER_CONTENT_TITLE = "CONTENT_TITLE";
        String IDENTIFIER_CONTENT_TEXT = "CONTENT_TEXT";
        String IDENTIFIER_SMALL_ICON = "SMALL_ICON";
    }

    public interface COUNTDOWN {
        //Maximum nachkommastellen (e.g. rounding percentage)
        int MAXIMUM_FRACTION_DIGITS = 2;

        //COUNTDOWN Properties (for validation e.g.)
        int COUNTDOWN_TITLE_LENGTH_MIN = 0; //(including this nr. and below all values will be rejected!)
        int COUNTDOWN_TITLE_LENGTH_MAX = 14; //including this nr. and above all values will be rejected
        int COUNTDOWN_DESCRIPTION_LENGTH_MIN = 0; //same constraint as above
        int COUNTDOWN_DESCRIPTION_LENGTH_MAX = 29; //same constraint as above
    }

    public interface COUNTDOWN_ACTIVITY {
        int INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS = 1500;
    }

    public interface CREDITS_ACTIVITY {
        //TODO: Normal strings (translateable should be generally converted to strings.xml and no reference to constants.java)
        String CONTACT_APP_CREATOR_EMAIL = "kevin.riedl.privat@gmail.com";
        String CONTACT_APP_CREATOR_DEFAULT_SUBJECT = "SurviveTheDay: Review";
        String CONTACT_APP_CREATOR_DEFAULT_BODY = "Please only German or English messages. :)";
        String CONTACT_APP_CREATOR_INTENT_TITLE = "Send e-mail..";
    }
}
