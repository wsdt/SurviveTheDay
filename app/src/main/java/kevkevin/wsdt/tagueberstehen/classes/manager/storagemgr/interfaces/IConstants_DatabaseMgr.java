package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces;

public interface IConstants_DatabaseMgr {
    int DATABASE_VERSION = 2; //MUST NOT be smaller than 1, and if we change db schema we should increment this so onUpgrade gets called!

    interface TABLES {
        interface COUNTDOWN { //Sql names etc.
            String TABLE_NAME = "Countdown";
            String TABLE_PREFIX = "cou_"; //table prefixes should be unique (added before every attributname)

            interface ATTRIBUTES {
                String ID = TABLE_PREFIX + "id";
                String TITLE = TABLE_PREFIX + "title";
                String DESCRIPTION = TABLE_PREFIX + "description";
                String STARTDATETIME = TABLE_PREFIX + "startdatetime";
                String UNTILDATETIME = TABLE_PREFIX + "untildatetime";
                String CREATEDDATETIME = TABLE_PREFIX + "createddatetime";
                String LASTEDITDATETIME = TABLE_PREFIX + "lasteditdatetime";
                String CATEGORYCOLOR = TABLE_PREFIX + "categorycolor";
                String NOTIFICATIONINTERVAL = TABLE_PREFIX + "notificationinterval";
                String RANDOMNOTIFICATIONMOTIVATION = TABLE_PREFIX + "randomnotificationmotivation";
                String LIVECOUNTDOWN = TABLE_PREFIX + "livecountdown";
            }
        }

        interface USERLIBRARY {
            String TABLE_NAME = "UserLibrary";
            String TABLE_PREFIX = "ulb_";

            interface ATTRIBUTES {
                String LIB_ID = TABLE_PREFIX + "libId";
                String LIB_NAME = TABLE_PREFIX + "libName";
                String LIB_LANGUAGE_CODE = TABLE_PREFIX + "libLanguageCode";
                String CREATED_BY = TABLE_PREFIX + "createdBy";
                String CREATED_ON = TABLE_PREFIX + "createdOn";
                String LAST_EDIT_ON = TABLE_PREFIX + "lastEditOn";
                /* 1:n relationship to USERLIBRARY_LINE */
            }
        }

        interface USERLIBRARY_LINE {
            String TABLE_NAME = "UserLibraryLine";
            String TABLE_PREFIX = "ull_";

            interface ATTRIBUTES {
                String LINE_ID = TABLE_PREFIX + "lineId";
                String LINE_TEXT = TABLE_PREFIX + "lineText";
                /* n:1 relationship to USERLIBRARY, so foreign key in this table */
            }
        }

        interface ZWISCHENTABELLE_COU_ULB {
            String TABLE_NAME = "Zwischentabelle_COU_ULB";
            String TABLE_PREFIX = "zcu_";

            //no own attributes (only foreign keys until now)
        }
    }

    interface DATABASE_HELPER {
        String DATABASE_NAME = "SURVIVE_THE_DAY";

        //IMPORTANT: Booleans are saved as integers! (0=FALSE | 1=TRUE)
        String[] DATABASE_CREATE_SQL = new String[]{ //MUST be a string array for each statement! (because we cannot execute multiple statements at once!
                "PRAGMA foreign_keys = ON;",
                "CREATE TABLE " + TABLES.USERLIBRARY.TABLE_NAME + " (\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + " INTEGER PRIMARY KEY,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.LIB_NAME + " TEXT,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.LIB_LANGUAGE_CODE + " TEXT,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.CREATED_BY + " TEXT,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.CREATED_ON + " TEXT,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.LAST_EDIT_ON + " TEXT);",
                "CREATE TABLE " + TABLES.COUNTDOWN.TABLE_NAME + " (\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.ID + " INTEGER PRIMARY KEY,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.TITLE + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.DESCRIPTION + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.STARTDATETIME + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.UNTILDATETIME + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.CREATEDDATETIME + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.LASTEDITDATETIME + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.CATEGORYCOLOR + " TEXT,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.NOTIFICATIONINTERVAL + " INTEGER,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.RANDOMNOTIFICATIONMOTIVATION + " BOOLEAN,\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.LIVECOUNTDOWN + " BOOLEAN);",

                "CREATE TABLE " + TABLES.ZWISCHENTABELLE_COU_ULB.TABLE_NAME + " (\n" +
                        TABLES.COUNTDOWN.ATTRIBUTES.ID + " INTEGER,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + " TEXT,\n" +
                        "PRIMARY KEY(" + TABLES.COUNTDOWN.ATTRIBUTES.ID + "," + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + "),\n" +
                        " FOREIGN KEY (" + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + ") REFERENCES " + TABLES.USERLIBRARY.TABLE_NAME + "(" + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + ")\n" +
                        " ON UPDATE CASCADE\n" +
                        " ON DELETE CASCADE,\n" +
                        " FOREIGN KEY (" + TABLES.COUNTDOWN.ATTRIBUTES.ID + ") REFERENCES " + TABLES.COUNTDOWN.TABLE_NAME + "(" + TABLES.COUNTDOWN.ATTRIBUTES.ID + ")\n" +
                        " ON UPDATE CASCADE\n" +
                        " ON DELETE CASCADE);",

                "CREATE TABLE " + TABLES.USERLIBRARY_LINE.TABLE_NAME + " (\n" +
                        TABLES.USERLIBRARY_LINE.ATTRIBUTES.LINE_ID + " INTEGER PRIMARY KEY,\n" +
                        TABLES.USERLIBRARY_LINE.ATTRIBUTES.LINE_TEXT + " TEXT NOT NULL,\n" +
                        TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + " INTEGER,\n" +
                        "FOREIGN KEY (" + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + ") REFERENCES " + TABLES.USERLIBRARY.TABLE_NAME + "(" + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + ")\n" +
                        "ON UPDATE CASCADE\n" +
                        "ON DELETE CASCADE\n" +
                        ");"};

                //+++++++++++++++ RESETTING FROM DB VERSION 1->2 ++++++++++++++++++++++++++++++++
                String[] DATABASE_UPGRADE_RESETTABLES = new String[]{"DROP TABLE IF EXISTS Zwischentabelle_COU_QLP;",
                        "DROP TABLE IF EXISTS Quotes;",
                        "DROP TABLE IF EXISTS Quote_languagepackages;"}; //drop table if exists name;
    }
}

