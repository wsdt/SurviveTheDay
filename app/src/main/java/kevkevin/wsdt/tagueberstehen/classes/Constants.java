package kevkevin.wsdt.tagueberstehen.classes;


import java.util.Locale;

public class Constants {
    public interface GLOBAL {
        //FORMATTING PERCENTAGE and BIG NUMBERS or DATES
        Locale LOCALE = Locale.ENGLISH;
        Character THOUSAND_GROUPING_SEPERATOR = ',';
        String DATETIME_FORMAT = "dd.MM.yyyy hh:mm:ss";
        String DATETIME_FORMAT_REGEX = "\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}";
    }

    public interface INAPP_PURCHASES {
        boolean USE_STATIC_TEST_INAPP_PRODUCTS = false;

        interface BASE64ENCODED_PUBLICKEY {
            char SEPARATOR = '/';
            String[] substr_arr = new String[] {
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjgvR22VPMnYUNA6WRQIwrgnjLUp1hb+fkHB2nkyJSq9sg9LaY1fs",
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
            CHANGE_NOTIFICATION_COLOR("countdownnode_notificationcategory_color_change"), //implemented
            REMOVE_ALL_ADS("remove_ads"), //implemented
            USE_MORE_COUNTDOWN_NODES("use_more_countdownnodes"); //implemented

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
        int COUNTDOWN_TITLE_LENGTH_MAX = 15; //including this nr. and above all values will be rejected
        int COUNTDOWN_DESCRIPTION_LENGTH_MIN = 0; //same constraint as above
        int COUNTDOWN_DESCRIPTION_LENGTH_MAX = 18; //(old before multiline: 29) same constraint as above

        //Escape methods
        interface ESCAPE {
            String escapeSQL_illegalCharacter = "'";
            String escapeSQL_legalCharacter = "\'";
            String[] escapeEnter_illegalCharacters = new String[]{"\n", "\r", System.getProperty("line.separator")}; //illegalCharacters (enter) get replaced with legalCharacter below (only called for title and description because they use CustomEdittext)
            String escapeEnter_legalCharacter = " "; //when enter found, place space
        }
    }

    public interface COUNTDOWN_COUNTER {
        //was in early versions the asynctask (now outsourced in own class with threads etc.)
        int REFRESH_UI_EVERY_X_MS = 400;
        //TODO: maybe make multiplikator configurable (but displayed for user in seconds!)
        int REFRESH_RANDOM_QUOTE_MULTIPLIKATOR = 35; //quote gets updated every REFRESH_UI_EVERY_X_MS * times to skip updating quote -> MULTIPLIKATOR
        int PROGRESS_ZERO_VALUE = 100; //so finished
        String TOTAL_TIMEUNIT_ZERO_VALUE = "0.00";
        String BIG_COUNTDOWN_ZERO_VALUE = "0:0:0:0:0:0:0";
    }

    public interface COUNTDOWN_ACTIVITY {
        int INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS = 1500;
        int INAPP_NOTIFICATION_CLOSE_ANIMATION_DURATION_IN_MS = 500;
    }

    public interface MAIN_ACTIVITY {
        String COUNTDOWN_VIEW_TAG_PREFIX = "COUNTDOWN_";
    }

    public interface STORAGE_MANAGERS {
        interface DATABASE_STR_MGR {
            int DATABASE_VERSION = 1; //MUST NOT be smaller than 1, and if we change db schema we should increment this so onUpgrade gets called!
            interface TABLES {
                interface COUNTDOWN { //Sql names etc.
                    String TABLE_NAME = "Countdown";
                    String TABLE_PREFIX = "cou_"; //table prefixes should be unique (added before every attributname)
                    interface ATTRIBUTES {
                        String ID = TABLE_PREFIX+"id";
                        String TITLE = TABLE_PREFIX+"title";
                        String DESCRIPTION = TABLE_PREFIX+"description";
                        String STARTDATETIME = TABLE_PREFIX+"startdatetime";
                        String UNTILDATETIME = TABLE_PREFIX+"untildatetime";
                        String CREATEDDATETIME = TABLE_PREFIX+"createddatetime";
                        String LASTEDITDATETIME = TABLE_PREFIX+"lasteditdatetime";
                        String CATEGORYCOLOR = TABLE_PREFIX+"categorycolor";
                        String NOTIFICATIONINTERVAL = TABLE_PREFIX+"notificationinterval";
                        String RANDOMNOTIFICATIONMOTIVATION = TABLE_PREFIX+"randomnotificationmotivation";
                        String LIVECOUNTDOWN = TABLE_PREFIX+"livecountdown";
                    }
                }
                interface QUOTELANGUAGEPACKAGES {
                    String TABLE_NAME = "Quote_languagepackages";
                    String TABLE_PREFIX = "qlp_";
                    interface ATTRIBUTES {
                        String ID = TABLE_PREFIX+"id";
                        String LANGUAGE_ID_LIST = TABLE_PREFIX+"idList"; //not a real member of any table, but gets created during some sql queries where n:m relationship gets merged/joined to list
                    }
                }
                interface ZWISCHENTABELLE_COU_QLP {
                    String TABLE_NAME = "Zwischentabelle_COU_QLP";
                    String TABLE_PREFIX = "zcq_";
                    //no own attributes (only foreign keys until now)
                    interface ATTRIBUTE_ADDITIONALS { //additional values for attributes
                        String LANGUAGE_ID_LIST_SEPARATOR = ","; //von sqlite vorgegeben so müssen wir diesen nutzen! (einheitlich)
                    }
                }
            }

            interface DATABASE_HELPER {
                String DATABASE_NAME = "SURVIVE_THE_DAY";

                //IMPORTANT: Booleans are saved as integers! (0=FALSE | 1=TRUE)
                String[] DATABASE_CREATE_SQL = new String[] { //MUST be a string array for each statement! (because we cannot execute multiple statements at once!
                        "PRAGMA foreign_keys = ON;",
                        "CREATE TABLE "+TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME+" (\n" +
                        TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+" TEXT PRIMARY KEY );",
                        "CREATE TABLE "+TABLES.COUNTDOWN.TABLE_NAME+" (\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.ID+" INTEGER PRIMARY KEY,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.TITLE+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.DESCRIPTION+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.STARTDATETIME+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.UNTILDATETIME+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.CREATEDDATETIME+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.LASTEDITDATETIME+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.CATEGORYCOLOR+" TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.NOTIFICATIONINTERVAL+" INTEGER,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.RANDOMNOTIFICATIONMOTIVATION+" BOOLEAN,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.LIVECOUNTDOWN+" BOOLEAN);",

                        "CREATE TABLE "+TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME+" (\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.ID+" INTEGER,\n" +
                        TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+" TEXT,\n" +
                        "PRIMARY KEY("+ TABLES.COUNTDOWN.ATTRIBUTES.ID+","+TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+"),\n" +
                        " FOREIGN KEY ("+TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+") REFERENCES "+TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME+"("+TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+")\n" +
                        " ON UPDATE CASCADE\n" +
                        " ON DELETE CASCADE,\n" +
                        " FOREIGN KEY ("+ TABLES.COUNTDOWN.ATTRIBUTES.ID+") REFERENCES "+TABLES.COUNTDOWN.TABLE_NAME+"("+ TABLES.COUNTDOWN.ATTRIBUTES.ID+")\n" +
                        " ON UPDATE CASCADE\n" +
                        " ON DELETE CASCADE);",

                        //Default data for foreign keys etc. [Zwischentabelle muss in setSave geupdatet/inserted werden]
                        "INSERT INTO "+TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME+" VALUES ('en'),('de');"}; //create table etc.
                String[] DATABASE_UPGRADE_RESETTABLES = new String[] {"DROP TABLE IF EXISTS "+TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME+";",
                        "DROP TABLE IF EXISTS "+TABLES.COUNTDOWN.TABLE_NAME+";",
                        "DROP TABLE IF EXISTS "+TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME+";"}; //drop table if exists name;
            }
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

    public interface KICKSTARTER_BOOTANDGENERALRECEIVER {
        String BROADCASTRECEIVER_ACTION_RESTART_ALL_SERVICES = "kevkevin.wsdt.tagueberstehen.classes.services.RESTART_ALL_SERVICES";
    }
}
