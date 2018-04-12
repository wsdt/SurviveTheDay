package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import java.util.HashMap;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.interfaces.IConstants_Countdown;
import kevkevin.wsdt.tagueberstehen.classes.manager.NotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.UserLibrary_depr;
import kevkevin.wsdt.tagueberstehen.classes.UserLibrarySaying_depr;
import kevkevin.wsdt.tagueberstehen.classes.services.LiveCountdown_ForegroundService;
import kevkevin.wsdt.tagueberstehen.classes.services.Kickstarter_BootAndGeneralReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;
import static kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_DatabaseMgr.*;
import static kevkevin.wsdt.tagueberstehen.classes.services.interfaces.IConstants_Kickstart_BootAndGeneralReceiver.BROADCASTRECEIVER_ACTION_RESTART_ALL_SERVICES;

//Maybe more efficient in future: https://developer.android.com/training/data-storage/room/index.html (for mapping)
public class DatabaseMgr {
    /**
     * database Helper class for mostly internal DatabaseMgr use!
     * Nested class SHOULD NOT be static, because we want to make it only accessable via instance of outer class which is a Singleton
     */
    private class DatabaseHelper extends SQLiteOpenHelper { //should be private
        private static final String TAG = "DatabaseHelper";

        //private constructor for singleton, but nested class no singleton because helper outside is static and the whole nested class is private!
        private DatabaseHelper(Context context) {
            //use applicationcontext so longer in memory
            super(context.getApplicationContext(), DATABASE_HELPER.DATABASE_NAME, null, DATABASE_VERSION); //seemingly there should be always 0
            Log.d(TAG, "DatabaseHelper: Tried to create helper instance.");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String sqlStatement : DATABASE_HELPER.DATABASE_CREATE_SQL) {
                Log.d(TAG, "onCreate: Executed statement-> " + sqlStatement);
                db.execSQL(sqlStatement); //ONLY one statement per method!
            }
            //By default no default data (so users have to download desired firebase libs)

            Log.d(TAG, "onCreate: Tried to create sql tables. ");
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            //IMPORTANT: If new version, then do this procedure NOT on mainthread (needs longer!)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.w(TAG, "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");
                    for (String sqlStatement : DATABASE_HELPER.DATABASE_UPGRADE_RESETTABLES) {
                        db.execSQL(sqlStatement); //for each statement a separate method call (necessary)
                    }
                    onCreate(db); //recreate database!
                }
            }).start();
        }
        //Also onDowngrade exists!
    }

    // DatabaseMgr methods ###########################################################
    //Members of superior class (outside helper class)
    private static final String TAG = "DatabaseMgr";
    private static DatabaseMgr singletonInstance;
    private static DatabaseHelper dbHelper; //SHOULD be static, because the same instance (to make singleton of it without a real singleton of helper class!)
    private static SQLiteDatabase db; //MUST be a member var
    //do not put cursor as member (because simultaneous operations would maybe destroy object allocations)
    private static SparseArray<Countdown> allCountdowns;
    private static SparseArray<UserLibrarySaying_depr> allQuotes;
    private static HashMap<String, UserLibrary_depr> allLanguagePacks;

    //Also singleton, so only one database instance at a time open
    private DatabaseMgr(@NonNull Context context) { //do not set context as class member, otherwise we could not make a singleton! (give context via methods!)
        dbHelper = new DatabaseHelper(context);
        setDb(dbHelper.getWritableDatabase()); //TODO: long running, should be maybe also in background thread
        Log.d(TAG, "Constructor: Created new DatabaseMgr instance.");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (db != null) {
            Log.d(TAG, "finalize: Tried to close db connection.");
            db.close(); //close db
        }
        /*Since getWritableDatabase() and getReadableDatabase() are expensive to call when the database is closed, you should leave your database connection open for as long as you possibly need to access it. Typically, it is optimal to close the database in the onDestroy() of the calling Activity.*/
    }

    //LANGUAGEPACK RELATED METHODS ++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * This method will map a queried row to a quoteObj.
     * ATTENTION: @param cursorRow should be closed() in parent-method in a finally block!
     */
    private UserLibrary_depr mapCursorRowToUserLibrary(@NonNull Context context, Cursor cursorRow) {
        return new UserLibrary_depr(
                context,
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID)));
    }

    public boolean saveUserLibrary(@NonNull Context context, @NonNull UserLibrary_depr userLibrary) {
        ContentValues values = new ContentValues();
        //values.put(TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES);

        long insertResult = getDb(context).insert(TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME,null,values);
        return false;
    }

    //todo: Fürs Erste nur queryMethod für Languagepacks, (delete/replace, etc. erst später WENN eigene Quotes hinzufügbar bzw. generell Quotes verwaltbar
    public HashMap<String, UserLibrary_depr> getAllUserLibraries(@NonNull Context context, boolean forceReload) {
        //SparseArray for setting the same rows/ids as in db, but string[] for setting row values (quote text, etc.)
        Log.d(TAG, "getAllUserLibraries: Trying to load all quotes.");
        if (allQuotes == null || forceReload) { //only do this if not already extracted in this session! (performance enhancement :)) --> so also not extra sql query necessary when getting single countdown
            if (forceReload) {
                Log.d(TAG, "getAllUserLibraries: Forcefully reloaded two-dimensional array.");
            } else {
                Log.d(TAG, "getAllUserLibraries: Found no already extracted two-dimensional array for quotes. Doing it now.");
            }
            Cursor dbCursor = null;
            HashMap<String, UserLibrary_depr> queriedLanguagePacks = new HashMap<>(); //sparse array instead of hashmap because better performance for pimitives

            try {
                /* Following query gets executed: ---------------
                 * SELECT * FROM Quote_languagepackages;*/

                dbCursor = getDb(context).rawQuery("SELECT * FROM " + TABLES.QUOTELANGUAGEPACKAGES.TABLE_NAME + ";", null); //query all

                if (dbCursor != null) {
                    Log.d(TAG, "getAllUserLibraries: Cursor is not null :)");

                    while (dbCursor.moveToNext()) {
                        UserLibrary_depr tmp = mapCursorRowToUserLibrary(context, dbCursor); //temporary cache-saving, to get countdownId for Index!
                        Log.d(TAG, "getAllUserLibraries: Found userpack-> " + tmp.toString());
                        queriedLanguagePacks.put(tmp.getUserLibraryId(), tmp);
                    }
                    setAllLanguagePacks(queriedLanguagePacks); //save all queried countdowns so we do not have to do this procedure again for runtime :)
                } else {
                    Log.w(TAG, "getAllUserLibraries: Cursor is null!");
                }
            } finally { //always finally for closing cursor! (also in error case)
                Log.d(TAG, "getAllUserLibraries: Trying to close cursor (finally).");
                if (dbCursor != null) {
                    Log.d(TAG, "getAllUserLibraries: Trying to close cursor now! It's not null.");
                    dbCursor.close();
                }
            }
        } //no else necessary, because allCountdowns already set
        Log.d(TAG, "getAllUserLibraries: Length of returned two-dimensional array: " + allLanguagePacks.size());
        return allLanguagePacks;
    }


    //QUOTE RELATED METHODS +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * This method will map a queried row to a quoteObj.
     * ATTENTION: @param cursorRow should be closed() in parent-method in a finally block!
     */
    private UserLibrarySaying_depr mapCursorRowToQuote(@NonNull Context context, Cursor cursorRow) {
        return new UserLibrarySaying_depr(
                context,
                cursorRow.getInt(cursorRow.getColumnIndex(TABLES.QUOTES.ATTRIBUTES.ID)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.QUOTES.ATTRIBUTES.QUOTE_TEXT)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID)));
    }

    //todo: Fürs Erste nur queryMethod für Quotes, (delete/replace, etc. erst später WENN eigene Quotes hinzufügbar bzw. generell Quotes verwaltbar
    public SparseArray<UserLibrarySaying_depr> getAllQuotes(@NonNull Context context, boolean forceReload) {
        //SparseArray for setting the same rows/ids as in db, but string[] for setting row values (quote text, etc.)
        Log.d(TAG, "getAllQuotes: Trying to load all quotes.");
        if (allQuotes == null || forceReload) { //only do this if not already extracted in this session! (performance enhancement :)) --> so also not extra sql query necessary when getting single countdown
            if (forceReload) {
                Log.d(TAG, "getAllQuotes: Forcefully reloaded two-dimensional array.");
            } else {
                Log.d(TAG, "getAllQuotes: Found no already extracted two-dimensional array for quotes. Doing it now.");
            }
            Cursor dbCursor = null;
            SparseArray<UserLibrarySaying_depr> queriedQuotes = new SparseArray<>(); //sparse array instead of hashmap because better performance for pimitives

            try {
                /* Following query gets executed: ---------------
                 * SELECT * FROM Quotes;*/

                dbCursor = getDb(context).rawQuery("SELECT * FROM " + TABLES.QUOTES.TABLE_NAME + ";", null); //query all

                if (dbCursor != null) {
                    Log.d(TAG, "getAllQuotes: Cursor is not null :)");

                    while (dbCursor.moveToNext()) {
                        UserLibrarySaying_depr tmp = mapCursorRowToQuote(context, dbCursor); //temporary cache-saving, to get countdownId for Index!
                        Log.d(TAG, "getAllQuotes: Found quote-> " + tmp.toString());
                        queriedQuotes.put(tmp.getSayingId(), tmp);
                    }
                    setAllQuotes(queriedQuotes); //save all queried countdowns so we do not have to do this procedure again for runtime :)
                } else {
                    Log.w(TAG, "getAllQuotes: Cursor is null!");
                }
            } finally { //always finally for closing cursor! (also in error case)
                if (dbCursor != null) {
                    dbCursor.close();
                }
            }
        } //no else necessary, because allCountdowns already set
        Log.d(TAG, "getAllQuotes: Length of returned two-dimensional array: " + allQuotes.size());
        return allQuotes;
    }


    //COUNTDOWN RELATED METHODS +++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * deleteCountdown: Deletes countdown with id and returns whether it was successful or not!
     */
    public boolean deleteCountdown(@NonNull Context context, int countdownId) {
        Log.d(TAG, "deleteCountdown: Trying to delete countdown with id: " + countdownId);
        boolean deletionSuccessful = getDb(context).delete(TABLES.COUNTDOWN.TABLE_NAME,
                TABLES.COUNTDOWN.ATTRIBUTES.ID + "=?", //do this for preventing sql injections!
                new String[]{String.valueOf(countdownId)}) > 0;

        //No foreach languagePack necessary, because we just delete all rows simultaneously where countdownId is
        deletionSuccessful &= getDb(context).delete(TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME,
                TABLES.COUNTDOWN.ATTRIBUTES.ID + "=?", new String[]{String.valueOf(countdownId)}) > 0;

        if (deletionSuccessful) {
            getAllCountdowns(context, false).delete(countdownId);
        } //also delete from object to keep it uptodate

        //Restart service because countown got removed
        restartNotificationService(context); //TODO: not here in storage mgr (own service mgr for all of them maybe merge them or similar i do not know)
        return deletionSuccessful;
    }

    /**
     * Deletes ALL countdowns and returns number of deleted rows
     * (not whether it was successful or not)
     */
    public int deleteAllCountdowns(@NonNull Context context) {
        Log.d(TAG, "deleteAllCountdowns: Trying to delete all countdowns.");

        //Deletes all countdowns
        int amountRowsDeleted = getDb(context).delete(TABLES.COUNTDOWN.TABLE_NAME, "1", null); //no. 1 says, that we want to return not whether deletion was successful, but how many rows we deleted
        amountRowsDeleted += getDb(context).delete(TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME, "1", null); //also delete auflösungstabelle (but not languagepacks itself)
        setAllCountdowns(null); //also delete saved object!

        //Restart service (because new/less services etc. / changed settings) [must be AFTER DELETION and BEFORE return (logically)!]
        restartNotificationService(context);

        return amountRowsDeleted;
    }

    /**
     * This method will map a queried row to a countdownObj.
     * ATTENTION: @param cursorRow should be closed() in parent-method in a finally block!
     */
    private Countdown mapCursorRowToCountdown(@NonNull Context context, Cursor cursorRow) {
        //TRUE = 1 | FALSE = 0 --> if == 1 then just give true otherwise false
        return new Countdown(context,
                cursorRow.getInt(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.ID)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.TITLE)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.DESCRIPTION)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.STARTDATETIME)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.UNTILDATETIME)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.CREATEDDATETIME)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.LASTEDITDATETIME)),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.CATEGORYCOLOR)),
                (cursorRow.getInt(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.RANDOMNOTIFICATIONMOTIVATION)) == 1),
                cursorRow.getInt(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.NOTIFICATIONINTERVAL)),
                (cursorRow.getInt(cursorRow.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.LIVECOUNTDOWN)) == 1),
                cursorRow.getString(cursorRow.getColumnIndex(TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.LANGUAGE_ID_LIST)).split(TABLES.ZWISCHENTABELLE_COU_QLP.ATTRIBUTE_ADDITIONALS.LANGUAGE_ID_LIST_SEPARATOR)); //, as concat is determined by Sqlite !!
    }

    /**
     * Get current countdown from database and map it onto countdownObj
     */
    public Countdown getCountdown(@NonNull Context context, boolean forceReload, int countdownId) { //do not use in loops! (use getAllCountdowns, because there is only ONE sql statement executed)
        //If allCountdowns not already downloaded they will, if they are then we just get returned the sparseArray
        return getAllCountdowns(context, forceReload).get(countdownId);
        //IMPORTANT: always use ValueAt() better performance than get()!!!! --> BUT: ValueAt = Index, Get = Key (=CountdownId)
    }

    /**
     * Get all countdowns from database and map them onto countdownObj
     *
     * @param forceReload: We are keeping the sparseArray uptodate, BUT if we modify our db and have a service running (external process in our case)
     *                     then we have a separate object! This means we would have outdated countdown objects!
     */
    public SparseArray<Countdown> getAllCountdowns(@NonNull Context context, boolean forceReload) { //do not use in loops! (use getAllCountdowns, because there is only ONE sql statement executed)
        Log.d(TAG, "getAllCountdowns: Trying to get all countdowns.");
        if (allCountdowns == null || forceReload) { //only do this if not already extracted in this session! (performance enhancement :)) --> so also not extra sql query necessary when getting single countdown
            if (forceReload) {
                Log.d(TAG, "getAllCountdowns: Forcefully reloaded sparseArray.");
            } else {
                Log.d(TAG, "getAllCountdowns: Found no already extracted sparseArray for countdowns. Doing it now.");
            }
            Cursor dbCursor = null;
            SparseArray<Countdown> queriedCountdowns = new SparseArray<>(); //sparse array instead of hashmap because better performance for pimitives

            try {
                /* Following query gets executed: ---------------
                 * SELECT cou.*,zcq.qlp_idList FROM Countdown as cou
                 INNER JOIN (
                 SELECT cou_id, GROUP_CONCAT(qlp_id) as qlp_idList FROM Zwischentabelle_COU_QLP
                 GROUP BY cou_id
                 ) as zcq ON zcq.cou_id=cou.cou_id;*/

                dbCursor = getDb(context).rawQuery("SELECT " + TABLES.COUNTDOWN.TABLE_PREFIX + ".*," + TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_PREFIX + "." + TABLES.QUOTELANGUAGEPACKAGES.TABLE_PREFIX + "idList FROM " + TABLES.COUNTDOWN.TABLE_NAME + " as " + TABLES.COUNTDOWN.TABLE_PREFIX +
                                " INNER JOIN (" +
                                " SELECT " + TABLES.COUNTDOWN.ATTRIBUTES.ID + ", GROUP_CONCAT(" + TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID + ") as " + TABLES.QUOTELANGUAGEPACKAGES.TABLE_PREFIX + "idList FROM " + TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME +
                                " GROUP BY " + TABLES.COUNTDOWN.ATTRIBUTES.ID +
                                " ) as " + TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_PREFIX + " ON " + TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_PREFIX + "." + TABLES.COUNTDOWN.ATTRIBUTES.ID + "=" + TABLES.COUNTDOWN.TABLE_PREFIX + "." + TABLES.COUNTDOWN.ATTRIBUTES.ID + ";"
                        , null);

                if (dbCursor != null) {
                    Log.d(TAG, "getAllCountdowns: Cursor is not null :)");

                    while (dbCursor.moveToNext()) {
                        Countdown tmp = mapCursorRowToCountdown(context, dbCursor); //temporary cache-saving, to get countdownId for Index!
                        Log.d(TAG, "getAllCountdowns: Found countdown-> " + tmp.toString());
                        queriedCountdowns.put(tmp.getCountdownId(), tmp);
                    }
                    setAllCountdowns(queriedCountdowns); //save all queried countdowns so we do not have to do this procedure again for runtime :)
                } else {
                    Log.w(TAG, "getAllCountdowns: Cursor is null!");
                }
            } finally { //always finally for closing cursor! (also in error case)
                if (dbCursor != null) {
                    dbCursor.close();
                }
            }
        } //no else necessary, because allCountdowns already set
        Log.d(TAG, "getAllCountdowns: Length of returned sparseArray: " + allCountdowns.size());
        return allCountdowns;
    }

    public SparseArray<Countdown> getAllCountdowns(@NonNull Context context, boolean forceReload, boolean onlyActiveCountdowns, boolean onlyShowLiveCountdowns) {
        if (!onlyActiveCountdowns && !onlyShowLiveCountdowns) {
            Log.w(TAG, "getAllCountdowns[filtered]: Both parameters are false. You won't get any results! Use getAllCountdowns(Context context)");
        }
        SparseArray<Countdown> queriedCountdowns = getAllCountdowns(context, forceReload); //only context parameter (otherwise recursive!!) --> so we get all countdowns
        SparseArray<Countdown> filteredCountdowns = new SparseArray<>(); //temporary (outside if so we can return empty list)
        int amountAllCountdowns = queriedCountdowns.size();

        if (amountAllCountdowns > 0) { //if list size smaller or equal zero we do not filter list (performance enhancement)
            Log.d(TAG, "getAllCountdowns[filtered]: Found " + amountAllCountdowns + " entries, trying now to filter them!");
            try {
                for (int i = 0, nsize = queriedCountdowns.size(); i < nsize; i++) {
                    Countdown tmp = queriedCountdowns.valueAt(i);
                    Log.d(TAG, "getAllCountdowns[filtered]: Saved entry: " + i + " / Value: " + tmp.toString());

                    if (onlyActiveCountdowns || onlyShowLiveCountdowns) {
                        Log.d(TAG, "getAllCountdowns[filtered]: Trying to filter onlyActive or onlyShowLiveCountdowns!");
                        if (tmp.isStartDateInThePast() && tmp.isUntilDateInTheFuture()) { //only add to activeCountdowns|onlyShowLiveCountdowns if startDate is in the past and untilDate is in Future (because otherwise service would run if motivateMe is on but startdate in the past
                            Log.d(TAG, "getAllCountdowns[filtered]: Startdate is in the past and until Date is in the future.");
                            if (onlyActiveCountdowns) {
                                Log.d(TAG, "getAllCountdowns[filtered]: Trying to filter for active countdowns.");
                                if (tmp.isActive()) { //is this specific countdown active? only then add it, because we only want active countdowns
                                    filteredCountdowns.put(tmp.getCountdownId(), tmp);
                                }
                            } else { //else because with simple if countdown could be added twice (true, true) --> not really because we have PUT now which would override existing countdown at its index (index=countdownId)
                                Log.d(TAG, "getAllCountdowns[filtered]: Trying to filter for live countdowns.");
                                if (tmp.isShowLiveCountdown()) { //is this specific countdown active? only then add it, because we only want active countdowns
                                    Log.d(TAG, "getAllCountdowns[filtered]: FOUND entry for live countdown! Adding it to list.");
                                    filteredCountdowns.put(tmp.getCountdownId(), tmp);
                                }
                            }
                        } else {
                            //both services are counting down, so we only want to add countdowns where until/startDate constraints are true
                            Log.d(TAG, "getAllCountdowns[filtered]: Ignoring countdown, because startDate in future or untilDate in past!");
                        }
                    } //do not evaluate whether both false and then return all of them!! (better use normal getAllCountdowns!)
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "getAllCountdowns[filtered]: Entry or a splitted index of array is null!");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "getAllCountdowns[filtered]: Unknown error!");
                e.printStackTrace();
            }
            Log.d(TAG, "getAllCountdowns[filtered]: Length of filtered arraylist is " + filteredCountdowns.size());
        } //no else

        //Important: Only return filtered sparseArray and DO NOT make evaluation here whether size==0 and then return allCountdowns (because this could return sth we do not expect, when e.g. no countdown active)
        return filteredCountdowns; //IMPORTANT: It is extremely important that the arraylist is ordered (for that we assign objects with index = countdown id
    }

    public void setSaveCountdown(@NonNull Context context, Countdown countdown) {
        Log.d(TAG, "setSaveCountdown: Entry might be replaced if it exists already.");

        /* Following query gets executed: (INSERT OR REPLACE INTO == REPLACE INTO [Abbr.]) ---------------
         * REPLACE INTO Countdown VALUES ({countdownDataRow});*/

        //Countdown to contentvalues to be inserted!
        ContentValues insertCountdownValues = new ContentValues();
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.ID, countdown.getCountdownId()); //escaping makes only sense for strings!
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.TITLE, escapeString(countdown.getCountdownTitle()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.DESCRIPTION, escapeString(countdown.getCountdownDescription()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.STARTDATETIME, escapeString(countdown.getStartDateTime()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.UNTILDATETIME, escapeString(countdown.getUntilDateTime()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.CREATEDDATETIME, escapeString(countdown.getCreatedDateTime()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.LASTEDITDATETIME, escapeString(countdown.getLastEditDateTime()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.CATEGORYCOLOR, escapeString(countdown.getCategory()));
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.RANDOMNOTIFICATIONMOTIVATION, countdown.isActive());
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.NOTIFICATIONINTERVAL, countdown.getNotificationInterval());
        insertCountdownValues.put(TABLES.COUNTDOWN.ATTRIBUTES.LIVECOUNTDOWN, countdown.isShowLiveCountdown());

        //If countdown with id exists already, it gets overwritten!
        long rowIdCountdown = getDb(context).replace(TABLES.COUNTDOWN.TABLE_NAME, null, insertCountdownValues);

        //Delete auflösungstabelle für countdown, because what is when countdown has now less languagepacks (it would remain in zwischentabelle)
        if (getDb(context).delete(TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME,
                TABLES.COUNTDOWN.ATTRIBUTES.ID + "=?", new String[]{String.valueOf(countdown.getCountdownId())}) > 0) {
            Log.d(TAG, "setSaveCountdown: Deletion of entries of updated/new countdown in zwischentabelle successful.");
        }

        //Now also insertValues for zwischentabelle (AFTER countdown is inserted)
        long[] rowIdsZwischentabelle = new long[countdown.getQuotesLanguagePacksObj().size()];
        int iteration = 0;
        for (UserLibrary_depr languagePack : countdown.getQuotesLanguagePacksObj().values()) {
            ContentValues insertZwischentabelleValues = new ContentValues();
            //for every row a separate insert!! (ergo for every languagepack of countdown a separate insert)
            insertZwischentabelleValues.put(TABLES.COUNTDOWN.ATTRIBUTES.ID, countdown.getCountdownId());
            insertZwischentabelleValues.put(TABLES.QUOTELANGUAGEPACKAGES.ATTRIBUTES.ID, languagePack.getUserLibraryId());
            rowIdsZwischentabelle[iteration++] = getDb(context).replace(TABLES.ZWISCHENTABELLE_COU_QLP.TABLE_NAME, null, insertZwischentabelleValues);
        }

        //at least evaluate one row id of the zwischentablle (if those succeeded we assume others did also)
        if (rowIdCountdown < 0 || rowIdsZwischentabelle[0] < 0) { //replace() returns -1 if failure otherwise it returns the rowid which must be equal or higher 0
            Log.e(TAG, "setSaveCountdown: Could not save/update countdown!");
            Toast.makeText(context, R.string.databaseMgr_error_saveCountdown_unsuccessful, Toast.LENGTH_SHORT).show();
        } else {
            //if successfully saved, then update also locally downloaded/extracted sparseArray (if already in RAM)
            if (allCountdowns != null) { //so we do not reload all countdowns with getAllCountdowns(); :)
                allCountdowns.put(countdown.getCountdownId(), countdown);
            } else {
                Log.w(TAG, "setSaveCountdown: AllCountdowns SparseArray is NULL! Not updating sparseArray."); //if we call getAllCountdowns() next time there should be also the current countdown :)
            }

            Log.d(TAG, "setSaveCountdown: Tried to save countdown not only to sparseArray, but also to Database [RowId: " + rowIdCountdown + "]: " + countdown.toString());
        }

        //TODO: Maybe this block should be in future sth else (separation of concerns), so we might be able to ensure that this is also checked on device start etc.
        //When saved then validate whether startDate is in future and if so, then schedule broadcast receiver for restarting all services so countdown gets started without opening app
        if (!countdown.isStartDateInThePast()) {
            Log.d(TAG, "setSaveCountdown: New saved countdown's StartDate is in the future! Scheduling broadcast receiver for restarting services.");
            AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
            if (alarmManager != null) {
                Intent intent = new Intent(context, Kickstarter_BootAndGeneralReceiver.class);
                intent.setAction(BROADCASTRECEIVER_ACTION_RESTART_ALL_SERVICES);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP, countdown.getDateTime(countdown.getStartDateTime()).getTimeInMillis(), pendingIntent);
                Log.d(TAG, "setSaveCountdown: Tried setting alarm for restarting all services when startdate gets into past.");
            } else {
                Log.e(TAG, "setSaveCountdown: Could not set alarm for future start date. Alarmmanager is null!");
            }
        }

        //Restart service (because new/less services etc. / changed settings)
        restartNotificationService(context);
    } //no setSaveAllCountdowns() necessary!


    public void restartNotificationService(@NonNull Context context) { //also called by Kickstarter_BootAndGeneralReceiver
        //TODO: BEST: ONLY EXECUTE this method, IF especially a relevant setting got changed (but hard to implement, because shared prefs get overwritten) --> would solve comment below with only st
        Log.d(TAG, "restartNofificationService: Did not restart service (not necessary). Tried broadcast receiver.");
        //would not be necessary because on broadcastreceiver the current countdown gets automatically loaded!
        //except if countdown was created, then we have to reload it! (only changes/deletes do not require a reload) [but for bgservice mode it is necessary]

        if (getAllCountdowns(context, false, true, false).size() > 0) {
            //Only start broadcast receivers or service when at least one countdown acc. to criteria found
            //TODO: only do this when not already active (otherwise intervals will get restarted)
            (new NotificationMgr(context, CountdownActivity.class, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications();
            Log.d(TAG, "restartNotificationService: Rescheduled all broadcast receivers.");
        }

        //ALSO RESTART FOREGROUND SERVICE
        Intent foregroundServiceIntent = new Intent(context, LiveCountdown_ForegroundService.class);
        try {
            Log.d(TAG, "restartNotificationService: Trying to restart foregroundService.");
            LiveCountdown_ForegroundService.stopRefreshAll();
            context.stopService(foregroundServiceIntent);
            context.startService(foregroundServiceIntent);
        } catch (NullPointerException e) {
            Log.e(TAG, "restartNotificationService: foregroundServiceIntent equals null! Could not restart foregroundService.");
        }
        Log.d(TAG, "restartNotficationService: Tried to restart foregroundService.");
    }

    public int getNextCountdownId(@NonNull Context context) {
        /* Returns Integer value of not existing countown id e.g.:
        * --> 1,2,3,4 [next: 5]
        * --> 1,2,4,5 [next: 3]
        * By filling the gaps we do not need to resave all countdowns when deleting a countdown
        * */
        int newCountdownId = (-1);
        //Load saved countdowns
        SparseArray<Countdown> countdowns = this.getAllCountdowns(context, false);
        //int i = 0; //counter
        for (int i = 0; i < countdowns.size(); i++) {
            if (countdowns.valueAt(i) == null) { //if nothing at index
                Log.d(TAG, "getNextCountdownId: Next countdown id at: " + i);
                newCountdownId = i;
                break;
            }
        }
        if (newCountdownId < countdowns.size()) {
            newCountdownId = countdowns.size();
            Log.d(TAG, "getNextCountdownId: Found next countdown id after loop (just incremented/used from size): " + newCountdownId);
        } else {
            Log.d(TAG, "getNextCountdownId: Found next countdown id within loop (filled gap): " + newCountdownId);
        }
        return newCountdownId;
    }

    /**
     * Used for sqlite escaping (used in countdown obj itself (setter/getter) and here in DatabaseMgr when inserting e.g.
     */
    public static String escapeString(@NonNull String string) {
        //return DatabaseUtils.sqlEscapeString(string); --> surrounds string with ' (destroys queries etc.) use following below:
        return string.replaceAll(IConstants_Countdown.ESCAPE.escapeSQL_illegalCharacter, IConstants_Countdown.ESCAPE.escapeSQL_legalCharacter);
    }

    //GETTER/SETTER +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static DatabaseMgr getSingletonInstance(@NonNull Context context) { //no setter bc. instance should be managed locally
        if (singletonInstance == null) {
            singletonInstance = new DatabaseMgr(context);
            Log.d(TAG, "getSingletonInstance: Instance is null, needed to create new one.");
        }
        return singletonInstance;
    }

    //Getter not static (althoug members are static), so we can assume result won't be null that easily if we forgot instance creation

    public static void setAllCountdowns(SparseArray<Countdown> allCountdowns) {
        DatabaseMgr.allCountdowns = allCountdowns;
    }

    public static SQLiteDatabase getDb(@NonNull Context context) {
        if (db == null) {
            if (context.getDatabasePath(DATABASE_HELPER.DATABASE_NAME).exists()) {
                Log.d(TAG, "getDb: Database exists already. ");
            } else {
                Log.w(TAG, "getDb: Database does not exist!");
            }
            setDb(dbHelper.getWritableDatabase());
        }
        return db;
    }

    public static void setDb(SQLiteDatabase db) {
        DatabaseMgr.db = db;
    }

    public static void setAllQuotes(SparseArray<UserLibrarySaying_depr> allQuotes) {
        DatabaseMgr.allQuotes = allQuotes;
    }

    public static void setAllLanguagePacks(HashMap<String, UserLibrary_depr> allLanguagePacks) {
        DatabaseMgr.allLanguagePacks = allLanguagePacks;
    }
}
