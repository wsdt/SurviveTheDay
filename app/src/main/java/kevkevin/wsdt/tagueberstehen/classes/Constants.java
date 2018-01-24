package kevkevin.wsdt.tagueberstehen.classes;


import java.util.Locale;

import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;

public class Constants {
    public interface GLOBAL {
        //FORMATTING PERCENTAGE and BIG NUMBERS or DATES
        Character THOUSAND_GROUPING_SEPERATOR = ',';
        String DATETIME_FORMAT = "dd.MM.yyyy hh:mm:ss";
        String DATETIME_FORMAT_REGEX = "\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}";
        Locale LOCALE = Locale.GERMAN;
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
        /* 999999950 as base so each countdown can have its own notification (but within the same foreground service be changed
        * notification id for a countdown: (999999950+countdownid) so..: countdown 0 = 999999950, countdown 1 = 999999951 etc.
        * --> High no. because notificationIds of motivational notifications are generated randomly up to this no.!*/
        int NOTIFICATION_ID = 999999950; //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]
    }

    public interface CUSTOMNOTIFICATION {
        //MUST BE LOWER than NOTIFICATION_ID of COUNTDOWNCOUNTERSERVICE! Below this no. a motivational notification gets its random notification id
        int NOTIFICATION_ID = 999999949;

        //LED light of notification (how long to blink on/off)
        int NOTIFICATION_BLINK_OFF_TIME_IN_MS = 1000;
        int NOTIFICATION_BLINK_ON_TIME_IN_MS = 1000;

        //CREATE NOTIFICATION ITSELF (IDENTIFIERS for PUTEXTRA in intents etc.)
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

        //Countdown UNITS (e.g. for HashMap identification)
        Integer COUNTDOWN_SECOND_IDENTIFIER = 0;
        Integer COUNTDOWN_MINUTE_IDENTIFIER = 1;
        Integer COUNTDOWN_HOUR_IDENTIFIER = 2;
        Integer COUNTDOWN_DAY_IDENTIFIER = 3;
        Integer COUNTDOWN_WEEK_IDENTIFIER = 4;
        Integer COUNTDOWN_MONTH_IDENTIFIER = 5;
        Integer COUNTDOWN_YEAR_IDENTIFIER = 6;
    }

    public interface COUNTDOWN_ACTIVITY {
        int INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS = 1500;
    }

    public interface MAIN_ACTIVITY {
        String COUNTDOWN_VIEW_TAG_PREFIX = "COUNTDOWN_";
    }

    public interface STORAGE_MANAGERS {
        interface INTERNAL_COUNTDOWN_STR_MGR {
            String SHAREDPREFERENCES_DBNAME = "COUNTDOWNS";
        }
        interface GLOBAL_APPSETTINGS_STR_MGR {
            String SHAREDPREFERENCES_DBNAME = "APP_SETTINGS";
            String SPIDENTIFIER_BG_SERVICE_PID = "BG_SERVICE_PID";
            String SPIDENTIFIER_USE_FORWARD_COMPATIBILITY = "USE_FORWARD_COMPATIBILITY";
            String SPIDENTIFIER_SAVE_BATTERY = "SAVE_BATTERY";
        }
    }
}
