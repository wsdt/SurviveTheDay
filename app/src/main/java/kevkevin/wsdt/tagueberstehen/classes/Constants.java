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

    public interface INAPP_PURCHASES {
        boolean USE_STATIC_TEST_INAPP_PRODUCTS = true;

        interface BASE64ENCODED_PUBLICKEY {
            char SEPARATOR = '/';
            String[] substr_arr = new String[] {"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjgvR22VPMnYUNA6WRQIwrgnjLUp1hb+fkHB2nkyJSq9sg9LaY1fs",
                "0TLTPpRpDWHipYfI58671lIuUKL",
                "pOokXDC6RMefoRYhNS3ikl8O3",
                "sOkBB3vFJb+Fgwk+b",
                "FvIp3Xes33s",
                "UJ6ZiC1Eidt1fT3xbPWZd+ss76sp3remAzFcHJ44UeU9jVECowmfnMTuddv62lb1QSsPo3la4bmBIwTaGdsTyemx92wjwHUYkZdIBxHwIiRxM3as72Q6s1PIK+YLC7+kRzsxE0QYLcCGbMt1Y2Ox",
                "asv",
                "8yRbKLE63",
                "9RRpFjjRdyNc5HbtnKBoHVmr6TeKDGUJWgiomVExqQIDAQAB"};
        }

        interface TEST_INAPP_PRODUCTS {
            interface STATIC_TEST {
                //String BUY_PRODUCT_DEFAULT_RESPONSE = GOOGLE_PLAY_STATIC_RESPONSES.PRODUCT_CANCELED.toString(); //let test purchases success (here changeable)
                enum GOOGLE_PLAY_STATIC_RESPONSES { //as enum to iterate through it
                    PRODUCT_PURCHASED("android.test.purchased"),
                    PRODUCT_CANCELED("android.test.canceled"),
                    PRODUCT_REFUND("android.test.refunded"),
                    PRODUCT_ITEM_UNAVAILABLE("android.test.item_unavailable");

                    private final String responseId;
                    GOOGLE_PLAY_STATIC_RESPONSES(final String responseId) {
                        this.responseId = responseId;
                    }
                    @Override
                    public String toString() {
                        return this.responseId;
                    }
                }
            }
        }

        //As enum so we can iterate through it more easily and ensure data types
        enum INAPP_PRODUCTS { //all other informations should be delivered by Google Play (following infos not translateable)
            BUY_EVERYTHING_ID("buy_everything"),
            CHANGE_NOTIFICATION_COLOR("countdownnode_notificationcategory_color_change"),
            CREATE_COUNTDOWNS_LONGER_THAN_24H("create_countdowns_longer_than_24hours"),
            REMOVE_ALL_ADS("remove_ads"),
            USE_MORE_COUNTDOWN_NODES("use_more_countdownnodes");

            private final String productId;
            INAPP_PRODUCTS(final String productId) {
                this.productId = productId;
            }
            @Override
            public String toString() {
                return this.productId;
            }
        }
    }

    public interface ADMANAGER {
        boolean USE_TEST_ADS = true;
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

    public interface COUNTDOWNCOUNTERSERVICE {
        /* 999999950 as base so each countdown can have its own notification (but within the same foreground service be changed
        * notification id for a countdown: (999999950+countdownid) so..: countdown 0 = 999999950, countdown 1 = 999999951 etc.
        * --> High no. because notificationIds of motivational notifications are generated randomly up to this no.!*/
        int NOTIFICATION_ID = 999999950; //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]

        //for put extra, mostly for restarting foreground service
        String STOP_SERVICE_LABEL = "STOP_SERVICE";
        int STOP_SERVICE = (-1); //put as extra
    }

    public interface CUSTOMNOTIFICATION {
        //MUST BE LOWER than NOTIFICATION_ID of COUNTDOWNCOUNTERSERVICE! Below this no. a motivational notification gets its random notification id
        int NOTIFICATION_ID = (COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID-1); //currently 999999949

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
            String NO_INTERNET_CONNECTION_COUNTER = "NO_INTERNET_CONNECTION_COUNTER";
            String REMOVE_ADS_TEMPORARLY_IN_MINUTES = "REMOVE_ADS_TEMPORARLY_IN_MINUTES";
        }
    }
}
