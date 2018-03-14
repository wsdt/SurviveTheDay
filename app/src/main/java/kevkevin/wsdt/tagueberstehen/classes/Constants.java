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
        boolean USE_TEST_ADS = false;
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
        String DEFAULT_NOTIFICATION_CHANNEL = "SURVIVE_THE_DAY"; //now obligatory for android o etc.

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
        //COUNTDOWN Properties (for validation e.g.)
        int COUNTDOWN_TITLE_LENGTH_MIN = 1; //(excluding this nr. and below all values will be rejected!)
        int COUNTDOWN_DESCRIPTION_LENGTH_MIN = 1; //same constraint as above

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

    public interface LANGUAGE_PACK {
        String DEFAULT_LANGUAGE_PACK = STORAGE_MANAGERS.DATABASE_STR_MGR.TABLES.QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0];
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
                    String[] LANGUAGE_PACKS = {"en", "de"}; //first index 0 is DEFAULT ENTRY!! (see interface default langaugepack)
                }
                interface ZWISCHENTABELLE_COU_QLP {
                    String TABLE_NAME = "Zwischentabelle_COU_QLP";
                    String TABLE_PREFIX = "zcq_";
                    //no own attributes (only foreign keys until now)
                    interface ATTRIBUTE_ADDITIONALS { //additional values for attributes
                        String LANGUAGE_ID_LIST_SEPARATOR = ","; //von sqlite vorgegeben so müssen wir diesen nutzen! (einheitlich)
                    }
                }
                interface QUOTES {
                    String TABLE_NAME = "Quotes";
                    String TABLE_PREFIX = "qts_";
                    interface ATTRIBUTES {
                        String ID = TABLE_PREFIX+"id";
                        String QUOTE_TEXT = TABLE_PREFIX+"quotetext";
                        //foreign key quotelanguagepackages id!
                    }
                    String[][] ALL_QUOTES = { //first dimension = ROW / second dimension is text + languagepack (NO ID here, this will be the index)
                            // ENGLISH - DEFAULT-QUOTES ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                            new String[] {"The way get started is to quit talking and begin doing. (Walt Disney)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The pessimist sees difficulty in every opportunity. The optimist sees opportunity in every difficulty. (Winston Churchill)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Don\'t let yesterday take up too much of today. (Will Rogers)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"You learn more from failure than from success. Don\'t let it stop you. Failure build character.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"It\'s not whether you get knocked down, it\'s whether you get up. (Vince Lombardi)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If you are working on something that you really care about, you don\'t have to be pushed. The vision pulls you. (Steve Jobs)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"People who are crazy enough to think they can change the world, are the ones who do. (Rob Siltanen)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Failure will never overtake me if my determination to succeed is strong enough. (Og Mandino)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Entrepreneurs are great at dealing with uncertainty and also very good at minimizing risk. That\'s the classic entrepreneur. (Mohnish Pabrai)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"We may encounter many defeats but we must not be defeated. (Maya Angelou)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Knowing is not enough; We must apply. Wishing is not enough; We must do. (Johann Wolfgang von Goethe)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Imagine your life is perfect in every respect; What would it look like? (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"We generate fears while we sit. We overcome them by action. (Dr. Henry Link)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Whether you think you can or think you can\'t, you\'re right. (Henry Ford)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Security is mostly a superstition. Life is either a daring adventure or nothing. (Helen Keller)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The man who has confidence in himself gains the confidence of others. (Hasidic Proverb)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The only limit to our realization of tomorrow will be our doubts of today. (Franklin D. Roosevelt)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Creativity is intelligence having fun. (Albert Einstein)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"What you lack in talent can be made up with desire, hustle and giving 110 % all the time. (Don Zimmer)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Do what you can with all you have, wherever you are. (Theodore Roosevelt)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Develop an attitude of gratitude. Say thank you to everyone you meet for everything they do for you. (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"You are never too old to set another goal or to dream a new dream. (C.S. Lewis)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"To see what is right and not do it is a lack of courage. (Confucious)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Reading is to the mind, as exercise to the body. (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Fake it until you make it! Act as if you had all the confidence you require until it becomes your reality. (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The future belong to the competent. Get good, get better, be the best! (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"For every reason it\'s not possible, there are hundreds of people who have faced the same circumstances and succeeded. (Jack Canfield)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Things work out best for those who make the best of how things work out. (John Wooden)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"A room without books is like a body without a soul. (Marcus Tullius Cicero)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"I think goals should never be easy, they should force you to work, even if they are uncomfortable at the time. (Michael Phelps)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"One of the lessons that I grew up with was to always stay true to yourself and never let what somebody else days distracts you from your goals. (Michelle Obama)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Today\'s accomplishments were yesterday\'s impossibilities. (Robert H. Schuller)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The only way to do great work is to love what you do. If you haven\'t found it yet, keep looking. Don\'t settle. (Steve Jobs)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"You Don’t Have To Be Great To Start, But You Have To Start To Be Great. (Zig Ziglar)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"A Clear Vision, Backed By Definite Plans, Gives You A Tremendous Feeling Of Confidence And Personal Power. (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"There Are No Limits To What You Can Accomplish, Except The Limits You Place On Your Own Thinking. (Brian Tracy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Integrity is the most valuable and respected quality of leadership. Always keep your word.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leadership is the ability to get extraordinary achievement from ordinary people.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders set high standards. Refuse to tolerate mediocrity or poor performance.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Clarity is the key to effective leadership. What are your goals?",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The best leaders have a high Consideration Factor. They really care about their people.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders think and talk about the solutions. Followers think and talk about the problems.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The key responsibility of leadership is to think about the future. No one else can do it for you.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The effective leader recognizes that they are more dependent on their people than they are on them. Walk softly.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders never use the word failure. They look upon setbacks as learning experiences.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Practice Golden Rule Management in everything you do. Manage others the way you would like to be managed.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Superior leaders are willing to admit a mistake and cut their losses. Be willing to admit that you’ve changed your mind. Don’t persist when the original decision turns out to be a poor one.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders are anticipatory thinkers. They consider all consequences of their behaviors before they act.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The true test of leadership is how well you function in a crisis.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders concentrate single-mindedly on one thing– the most important thing, and they stay at it until it’s complete.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The three ‘C’s’ of leadership are Consideration, Caring, and Courtesy. Be polite to everyone.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Respect is the key determinant of high-performance leadership. How much people respect you determines how well they perform.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leadership is more who you are than what you do.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Entrepreneurial leadership requires the ability to move quickly when opportunity presents itself.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders are innovative, entrepreneurial, and future oriented. They focus on getting the job done.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Leaders are never satisfied; they continually strive to be better.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The best and most beautiful things in the world cannot be seen or even touched - they must be felt with the heart. (Helen Keller)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The best preparation for tomorrow is doing your best today. (H. Jackson Brown, Jr.)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"I can\'t change the direction of the wind, but I can adjust my sails to always reach my destination. (Jimmy Dean)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"We must let go of the life we have planned, so as to accept the one that is waiting for us. (Joseph Campbell)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"You must do the things you think you cannot do. (Eleanor Roosevelt)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Put your heart, mind, and soul into even your smallest acts. This is the secret of success. (Swami Sivananda)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Start by doing what\'s necessary; then do what\'s possible; and suddenly you are doing the impossible. (Francis of Assisi)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The limits of the possible can only be defined by going beyond them into the impossible. (Arthur C. Clarke)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Happiness is not something you postpone for the future; it is something you design for the present. (Jim Rohn)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Try to be a rainbow in someone\'s cloud. (Maya Angelou)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"It is during our darkest moments that we must focus to see the light. (Aristotle)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Health is the greatest gift, contentment the greatest wealth, faithfulness the best relationship. (Buddha)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Change your thoughts and you change the world. (Norman Vincent Peale)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Nothing is impossible, the word itself says \'I\'m possible\'. (Audrey Hepburn)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"My mission in life is not merely to survive, but to thrive; and to do so with some passion, some compassion, some humor, and some style. (Maya Angelou)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Today I choose life. Every morning when I wake up I can choose joy, happiness, negativity, pain... To feel the freedom that comes from being able to continue to make mistakes and choices - today I choose to feel life, not to deny my humanity but embrace it. (Kevyn Aucoin)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Your work is going to fill a large part of your life, and the only way to be truly satisfied is to do what you believe is great work. And the only way to do great work is to love what you do. If you haven\'t found it yet keep looking. Don\'t settle. As with all matters of the heart, you\'ll know when you find it. (Steve Jobs)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Believe you can and you\'re halfway there. (Theodore Roosevelt)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Keep your face always toward the sunshine - and shadows will fall behind you. (Walt Whitman)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Perfection is not attainable, but if we chase perfection we can catch excellence. (Vince Lombardi)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If opportunity doesn\'t knock, build a door. (Milton Berle)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"What we think, we become. (Buddha)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Clouds come floating into my life, no longer to carry rain or usher storm, but to add color to my sunset sky. (Rabindranath Tagore)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"What lies behind you and what lies in front of you, pales in comparison to what lies inside of you. (Ralph Waldo Emerson)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Someone is sitting in the shade today because someone planted a tree a long time ago. (Warren Buffett)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"No act of kindness, no matter how small, is ever wasted. (Aesop)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"I believe that if one always looked at the skies, one would end up with wings. (Gustave Flaubert)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"We know what we are, but know not what we may be. (William Shakespeare)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Let us sacrifice our today so that our children can have a better tomorrow. (A. P. J. Abdul Kalam)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"There are two ways of spreading light: to be the candle or the mirror that reflects it. (Edith Wharton)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Let us remember: One book, one pen, one child, and one teacher can change the world. (Malala Yousafzai)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"All you need is the plan, the road map, and the courage to press on to your destination. (Earl Nightingale)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Your personal life, your professional life, and your creative life are all intertwined. I went through a few very difficult years where I felt like a failure. But it was actually really important for me to go through that. Struggle, for me, is the most inspirational thing in the world at the end of the day - as long as you treat it that way. (Skylar Grey)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Your present circumstances don\'t determine where you can go; they merely determine where you start. (Nido Qubein)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"I will love the light for it shows me that way, yet I will endure the darkness because it shows me the stars. (Og Mandino)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"It is in your moments of decision that your destiny is shaped. (Tony Robbins)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Thousands of candles can be lighted from a single candle, and the life of the candle will not be shortened. Happiness never decreases by being shared. (Buddha)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The only journey is the one within. (Rainer Maria Rilke)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"As we express our gratitude, we must never forget that the highest appreciation is not to utter words, but to live by them. (John F. Kennedy)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The bird is powered by its own life and by its motivation. (A. P. J. Abdul Kalam)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"No matter what people tell you, words and ideas can change the world. (Robin Williams)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Don\'t judge each day by the harvest you reap but by the seeds you plant. (Robert Louis Stevenson)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"I believe in pink. I believe that laughing is the best calorie burner. I believe in kissing, kissing a lot. I believe in being strong when everything seems to be going wrong. I believe that happy girls are the prettiest girls. I believe that tomorrow is another day and I believe in miracles. (Audrey Hepburn)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Shoot for the moon and if you miss you will still be among the stars. (Les Brown)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Let your life lightly dance on the edges of Time like dew on the tip of a leaf. (Rabindranath Tagore)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"I hated every minute of training, but I said, \'Don\'t quit. Suffer now and live the rest of your life as a champion.\' (Muhammad Ali)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"We can\'t help everyone, but everyone can help someone. (Ronald Reagan)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"God always gives His best to those who leave the choice with him. (Jim Elliot)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"When you have a dream, you\'ve got to grab it and never let go. (Carol Burnett)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If you believe in yourself and have dedication and pride - and never quit, you\'ll be a winner. The price of victory is high but so are the rewards. (Paul Bryant)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Let us make our future now, and let us make our dreams tomorrow\'s reality. (Malala Yousafzai)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"A hero is someone who has given his or her life to something bigger than oneself. (Joseph Campbell)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"When the sun is shining I can do anything; no mountain is too high, no trouble too difficult to overcome. (Wilma Rudolph)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Throw your dreams into space like a kite, and you do not know what it will bring back, a new life, a new friend, a new love, a new country. (Anais Nin)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If you always put limit on everything you do, physical or anything else. It will spread into your work and into your life. There are no limits. There are only plateaus, and you must not stay there, you must go beyond them. (Bruce Lee)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"To love means loving the unloveable. To forgive means pardoning the unpardonable. Faith means believing the unbelievable. Hope means hoping when everything seems hopeless. (Gilbert K. Chesterton)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The measure of who we are is what we do with what we have. (Vince Lombardi)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"It is never too late to be what you might have been. (George Eliot)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"There is nothing impossible to him who will try. (Alexander the Great)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Two roads diverged in a wood and I - took the one less traveled by, and that has made all the difference. (Robert Frost)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"From a small seed a mighty trunk may grow. (Aeschylus)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Give light, and the darkess will disappear of itself. (Desiderius Erasmus)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Love is a fruit in season at all times, and within reach of every hand. (Mother Teresa)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Be brave enough to live life creatively. The creative place where no one else has ever been. (Alan Alda)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If I have seen further than others, it is by standing upon the shoulders of giants. (Isaac Newton)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Follow your bliss and the universe will open doors where there were only walls. (Joseph Campbell)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Thinking: the talking of the soul with itself. (Plato)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Happiness resides not in possessions, and not in gold, happiness dwells in the soul. (Democritus)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"How wonderful it is that nobody need wait a single moment before starting to improve the world. (Anne Frank)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"When we seek to discover the best in others, we somehow bring out the best in ourselves. (William Arthur Ward)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"With self-discipline most anything is possible. (Theodore Roosevelt)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"To the mind that is still, the whole universe surrenders. (Lao Tzu)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Today is the only day. Yesterday is gone. (John Wooden)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Your big opportunity may be right where you are now. (Napoleon Hill)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The power of imagination makes us infinite. (John Muir)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Out of difficulties grow miracles. (Jean de la Bruyere)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"What makes the desert beautiful is that somewhere it hides a well. (Antoine de Saint-Exupery)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Tomorrow is the most important thing in life. Comes into us at midnight very clean. It\'s perfect when it arrives and it puts itself in our hands. It hopes we\'ve learning something from yesterday. (John Wayne)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If the world seems cold to you, kindle fires to warm it. (Lucy Larcom)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"How glorious a greeting the sun gives the mountains. (John Muir)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"When you get into a tight place and everything goes against you, till it seems as though you could not hang on a minute longer, never give up then, for that is just the place and time that the tide will turn. (Harriet Beecher Stowe)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Happiness is a butterfly, which when pursued, is always beyond your grasp, but which, if you will sit down quietly, may alight upon you. (Nathaniel Hawthorne)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Whoever is happy will make others happy too. (Anne Frank)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"In a gentle way, you can shake the world. (Mahatma Gandhi)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Don\'t limit yourself. Many people limit themselves to what they think they can do. You can go as far as your mind lets you. What you believe, remember, you can achieve. (Mary Kay Ash)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"We can change our lives. We can do, have, and be exactly what we wish. (Tony Robbings)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Even if I knew that tomorrow the \'would\' would go to pieces, I would still plant my apple tree. (Martin Luther)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Memories of our lives, of our works and our deeds will continue in others. (Rosa Parks)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The things that we love tell us what we are. (Thomas Aquinas)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Somewhere, something incredible is waiting to be known. (Sharon Begley)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"The glow of one warm thought is to me worth more than money. (Thomas Jefferson)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If a man does not keep pace with his companions, perhaps it is because he hears a different drummer. Let him step to the music which he hears, however measured or far away. (Henry David Thoreau)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Accept the things to which fate binds you, and love the people whom fate brings you together, but do so with all your heart. (Marcus Aurelius)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"If we did all the things we are capable of, we would literally astound ourselves. (Thomas A. Edison)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Each day provides its own gifts. (Marcus Aurelius)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Keep your feet on the ground, but let your heart soar as high as it will. Refuse to be average or to surrender to the chill of your spiritual environment. (Arthur Helps)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"Let us dream of tomorrow where we can truly love from the soul, and know love as the ultimate truth at the heart of all creation. (Michael Jackson)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"You change your life by changing your heart. (Max Lucado)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},
                            new String[] {"A champion is someone who gets up when he can\'t. (Jack Dempsey)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]},

                            //GERMAN - DEFAULT QUOTES ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                            new String[] {"Aller Anfang ist schwer.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Des Teufels liebstes Möbelstück ist die lange Bank.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wer rastet, der rostet.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Anfangen ist leicht, Beharren eine Kunst.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Erst denken, dann handeln.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Eile mit Weile.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Kümmere dich nicht um ungelegte Eier.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Krummes Holz gibt auch gerades Feuer.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Man muss die Dinge nehmen, wie sie kommen.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Morgenstund hat Gold im Mund.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Selbst ist der Mann/die Frau.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Taten sagen mehr als Worte.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Übung macht den Meister.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wer zwei Hasen auf einmal jagt bekommt keinen.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wer A sagt, muss auch B sagen.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wenn der Reiter nichts taugt, ist das Pferd schuld.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Dienst ist Dienst und Schnaps ist Schnaps.",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Aufklärung ist der Ausgang des Menschen aus seiner selbst verschuldeten Unmündigkeit. (Immanuel Kant)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Eine Wahrheit kann erst wirken, wenn der Empfänger für sie reif ist. (Christian Morgenstern)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die beste und sicherste Tarnung ist immer noch die blanke und nackte Wahrheit. Die glaubt niemand! (Max Frisch)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die Strafe des Lügners ist nicht, dass ihm niemand mehr glaubt, sondern dass er selbst Niemandem mehr glauben kann. (George Bernard Shaw)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Mancher Mensch hat ein großes Feuer in seiner Seele, und niemand kommt, um sich daran zu wärmen. (Vincent van Gogh)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wer sich den Gesetzen nicht fügen will, muss die Gegen verlassen, wo sie gelten. (Johann Wolfgang von Goethe)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Jeder kann wütend werden, das ist einfach. Aber wütend auf den Richtigen zu sein, im richtigen Maß, zur richtigen Zeit, zum richtigen Zweck und auf die richtige Art, das ist schwer. (Aristoteles)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Der Tod eines Mannes ist eine Tragödie, aber der Tod von Millionen nur eine Statistik. (Josef Stalin)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Der Gescheitere gibt nach! Eine traurige Wahrheit, sie begründet die Weltherrschaft der Dummheit. (Marie von Ebner-Eschenbach)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Fallen ist weder gefährlich noch eine Schande. Liegenbleiben ist beides. (Konrad Adenauer)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Alle Revolutionen haben bisher nur eines bewiesen, nämlich, dass sich vieles ändern lässt, bloß nicht die Menschen. (Karl Marx)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Der große Jammer mit den Menschen ist, dass sie so genau wissen, was man ihnen schuldet, und so wenig Empfindungen dafür haben, was sie anderen schulden. (Franz von Sales)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Dem Kapitalismus wohnt ein Laster inne: Die ungleiche Verteilung der Güter. Dem Sozialismus hingegen wohnt eine Tugend inne: Die gleichmäßige Verteilung des Elends. (Winston Churchill)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Unser größter Ruhm ist nicht, niemals zu fallen, sondern jedes Mal wieder aufzustehen. (Nelson Mandela)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die Freiheit des Menschen liegt nicht darin, dass er tun kann, was er will, sondern, dass er nicht tun muss, was er nicht will. (Jean-Jacques Rousseau)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wahlen allein machen noch keine Demokratie. (Barack Obama)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die Lüge ist wie ein Schneeball: Je länger man ihn wälzt, desto größer wird er. (Martin Luther)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Nachrichtensprecher fangen stets mit \'Guten Abend\' an und brauchen dann 15 Minuten, um zu erklären, dass es kein guter Abend ist. (Rudi Carrell)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Anmut ist ein Ausströmen der inneren Harmonie. (Marie von Ebner-Eschenbach)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Das Ärgerliche am Ärger ist, dass man sich schadet, ohne anderen zu nützen. (Kurt Tucholsky)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Vermutlich hat Gott die Frau erschaffen, um den Mann kleinzukriegen. (Voltaire)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Keinem vernünftigen Menschen wird es einfallen, Tintenflecken mit Tinte, Ölflecken mit Öl wegwaschen zu wollen. Nur Blut soll immer wieder mit Blut abgewaschen werden. (Bertha von Suttner)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Bescheiden können nur die Menschen sein, die genug Selbstbewusstsein haben. (Gabriel Laub)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die Öffentlich-Rechtlichen machen sich in jede Hose, die man ihnen hinhält, und die Privaten senden das, was darin ist. (Dieter Hildebrandt)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wer nicht kann, was er will, muss wollen, was er kann. Denn das zu wollen, was er nicht kann, wäre töricht. (Leonardo da Vinci)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Suche nicht nach Fehlern, suche nach Lösungen. (Henry Ford)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Gott würfelt nicht. (Albert Einstein)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die größten Enttäuschungen haben ihren Ursprung in zu großen Erwartungen. (Ernst Ferstl)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Niemand wird mit dem Hass auf andere Menschen wegen ihrer Hautfarbe, ethnischen Herkunft oder Religion geboren. Hass wird gelernt. Und wenn man Hass lernen kann, kann man auch lernen zu lieben. Denn Liebe ist ein viel natürlicheres Empfinden im Herzen eines Menschen als ihr Gegenteil. (Nelson Mandela)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die Leute hatten die Gewohnheit, mich anzuschauen, als sei ich eine Art Spiegel und keine Person. Sie sahen nicht mich, sondern ihre eigenen lüsternen Gedanken und dann spielten sie selbst die Unschuldigen, indem sie mich als lüstern bezeichneten. (Marilyn Monroe)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Man löst keine Probleme, indem man sie auf Eis legt. (Winston Churchill)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die ganze Mannigfaltigkeit, der ganze Reiz und die ganze Schönheit des Lebens setzten sich aus Licht und Schatten zusammen. (Leo Tolstoi)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Freundschaft ist nicht nur ein köstliches Geschenk, sondern auch eine dauernde Aufgabe. (Ernst Zacharias)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wenn wir alt werden, so beginnen wir zu disputieren, wollen klug sein und doch sind wir die größten Narren. (Martin Luther)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Der große Sport fängt da an, wo er längst aufgehört hat, gesund zu sein. (Bertolt Brecht)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Der Sport ist ein sehr vernünftiger Versuch des modernen Zivilisationsmenschen, sich die Strapaze künstlich zu verschaffen. (Peter Bamm)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Sport: Opium für das Volk. (Percy Clummings)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Es gibt tausend Krankheiten, aber nur eine Gesundheit. (Ludwig Börne)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Seien Sie vorsichtig mit Gesundheitsbüchern - Sie könnten an einem Druckfehler sterben. (Mark Twain)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Wer nicht jeden Tag etwas für seine Gesundheit aufbringt, muss eines Tages sehr viel Zeit für die Krankheit opfern. (Sebastian Kneipp)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Ein leidenschaftlicher Raucher, der immer von der Gefahr des Rauchens für die Gesundheit liest, hört in den meisten Fällen auf zu lesen. (Winston Churchill)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Glück, das ist einfach eine gute Gesundheit und ein schlechtes Gedächtnis. (Ernest Hemmingway)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Ich bin körperlich und physisch topfit. (Thomas Häßler)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Ein ungeübtes Gehirn ist schädlicher für die Gesundheit als ein ungeübter Körper. (George Bernard Shaw)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Die Ehefrau ist das beste Trainingslager. (Otto Rehhagel)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]},
                            new String[] {"Ich messe den Erfolg nicht an meinen Siegen, sondern daran, ob ich jedes Jahr besser werde. (Tiger Woods)",QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]}
                    };
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

                        "CREATE TABLE "+TABLES.QUOTES.TABLE_NAME+" (\n" +
                                TABLES.QUOTES.ATTRIBUTES.ID+" INTEGER PRIMARY KEY,\n" +
                                TABLES.QUOTES.ATTRIBUTES.QUOTE_TEXT+" TEXT NOT NULL,\n" +
                                TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+" TEXT,\n" +
                                "FOREIGN KEY ("+TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+") REFERENCES "+TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME+"("+TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID+")\n" +
                                    "ON UPDATE CASCADE\n" +
                                    "ON DELETE CASCADE\n" +
                                ");",

                        //Default data for foreign keys etc. [Zwischentabelle muss in setSave geupdatet/inserted werden]
                        "INSERT INTO "+TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME+" VALUES ('"+TABLES.QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]+"'),('"+TABLES.QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[1]+"');"}; //create table etc.
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
