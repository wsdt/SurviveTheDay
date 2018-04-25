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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.UserLibrary;
import kevkevin.wsdt.tagueberstehen.classes.interfaces.IConstants_Countdown;
import kevkevin.wsdt.tagueberstehen.classes.manager.NotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_FirebaseStorageMgr;
import kevkevin.wsdt.tagueberstehen.classes.services.Kickstarter_BootAndGeneralReceiver;
import kevkevin.wsdt.tagueberstehen.classes.services.LiveCountdown_ForegroundService;

import static android.content.Context.NOTIFICATION_SERVICE;
import static kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_DatabaseMgr.DATABASE_HELPER;
import static kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_DatabaseMgr.DATABASE_VERSION;
import static kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_DatabaseMgr.TABLES;
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

            Log.d(TAG, "onCreate: Tried to create sql tables. ");
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            //IMPORTANT: If new version, then do this procedure NOT on mainthread (needs longer!)
            //BUT we do it on mainthread for now, because otherwise when upgrading the new version is not available and the app might crash for the first launch after the update.

            Log.w(TAG, "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");
            for (String sqlStatement : DATABASE_HELPER.DATABASE_UPGRADE_RESETTABLES) {
                db.execSQL(sqlStatement); //for each statement a separate method call (necessary)
            }
            onCreate(db); //recreate database!
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

    //Also singleton, so only one database instance at a time open
    private DatabaseMgr(@NonNull Context context) { //do not set context as class member, otherwise we could not make a singleton! (give context via methods!)
        dbHelper = new DatabaseHelper(context);
        setDb(dbHelper.getWritableDatabase()); //TODO: long running, should be maybe also in background thread
        Log.d(TAG, "Constructor: Created new DatabaseMgr instance.");

        //Download default userlibs (e.g.) if not downloaded already
        FirebaseStorageMgr.downloadDefaultData(context);
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

    //USERLIBRARY RELATED METHODS ++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    /* ##########################################################################################################################################
     * ###########################################################################################################################################
     * ------- USERLIBRARY - CRUD Operations -----------------------------------------------------------------------------------------------------
     * ###########################################################################################################################################
     * ########################################################################################################################################### */
    private Map<String, UserLibrary> global_AllUserLibraries; //no setter, because only this class should modify it (getter is getAllCountdowns(force etc.))

    //CREATE & UPDATE ---------------------------------------------------------------------------------------------------------------------------
    public void saveUserLibrary(@NonNull Context context, @NonNull UserLibrary userLibrary) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID, userLibrary.getLibId());
        contentValues.put(TABLES.USERLIBRARY.ATTRIBUTES.LIB_NAME, escapeString(userLibrary.getLibName()));
        contentValues.put(TABLES.USERLIBRARY.ATTRIBUTES.LIB_LANGUAGE_CODE, escapeString(userLibrary.getLibLanguageCode()));
        contentValues.put(TABLES.USERLIBRARY.ATTRIBUTES.CREATED_BY, escapeString(userLibrary.getCreatedBy()));
        contentValues.put(TABLES.USERLIBRARY.ATTRIBUTES.CREATED_ON, escapeString(userLibrary.getCreatedOn()));
        contentValues.put(TABLES.USERLIBRARY.ATTRIBUTES.LAST_EDIT_ON, escapeString(userLibrary.getLastEditOn()));

        getDb(context).replace(TABLES.USERLIBRARY.TABLE_NAME, null, contentValues);

        //Now also save lines!
        int rowId = 0;
        for (String line : userLibrary.getLines()) {
            ContentValues cvLine = new ContentValues();
            cvLine.put(TABLES.USERLIBRARY_LINE.ATTRIBUTES.LINE_ID,rowId++); //explicitely add id instead of auto_increment to avoid saving the same rows more often into the same userlib! (when downloaded multiple times)
            cvLine.put(TABLES.USERLIBRARY_LINE.ATTRIBUTES.LINE_TEXT,line);
            cvLine.put(TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID,userLibrary.getLibId());
            getDb(context).replace(TABLES.USERLIBRARY_LINE.TABLE_NAME, null, cvLine);
        }

        //no need to restart, because userLib just get's active if selected in countdown (and if countdown saved services will be restarted)
        //also no need to update countdown table, because countdowns with this lib will only exist after this operations if users selected this lib.
    }

    //READ --------------------------------------------------------------------------------------------------------------------------------------
    public Map<String, UserLibrary> getAllUserLibraries(@NonNull Context context, boolean forceReload) {
        if (forceReload || this.global_AllUserLibraries == null) {

            Cursor cursorAllUserLibraries = getDb(context).rawQuery("SELECT * FROM " + TABLES.USERLIBRARY.TABLE_NAME + ";", null);
            Map<String, UserLibrary> allUserLibraries = new HashMap<>();

            while (cursorAllUserLibraries.moveToNext()) {
                UserLibrary userLibrary = getUserLibraryFromCursor(context, cursorAllUserLibraries);
                Log.d(TAG, "UserLibsize: "+userLibrary.getLines().size());
                allUserLibraries.put(userLibrary.getLibId() + "", userLibrary);
            }

            closeCursor(cursorAllUserLibraries);
            this.global_AllUserLibraries = allUserLibraries;
        }
        return this.global_AllUserLibraries;
    }
    //don't extract one specific userLib directly from db (just use the hashmap in UserLibrary)

    /**
     * Extracts library lines of one specific userLibrary.
     */
    private List<String> getUserLibraryLines(@NonNull Context context, String userLibraryId) {
        Cursor cursorUserLibraryLines = getDb(context).rawQuery("SELECT * FROM " + TABLES.USERLIBRARY_LINE.TABLE_NAME + " WHERE " +
                TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + "= '" + userLibraryId + "';", null);

        List<String> userLibraryLines = new ArrayList<>();
        while (cursorUserLibraryLines.moveToNext()) {
            userLibraryLines.add(cursorUserLibraryLines.getString(cursorUserLibraryLines.getColumnIndex(TABLES.USERLIBRARY_LINE.ATTRIBUTES.LINE_TEXT)));
        }

        closeCursor(cursorUserLibraryLines);
        return userLibraryLines;
    }

    private UserLibrary getUserLibraryFromCursor(@NonNull Context context, @NonNull Cursor userLibraryCursor) {
        String userLibraryId = userLibraryCursor.getString(userLibraryCursor.getColumnIndex(TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID));
        return new UserLibrary(
                userLibraryId,
                userLibraryCursor.getString(userLibraryCursor.getColumnIndex(TABLES.USERLIBRARY.ATTRIBUTES.LIB_NAME)),
                userLibraryCursor.getString(userLibraryCursor.getColumnIndex(TABLES.USERLIBRARY.ATTRIBUTES.LIB_LANGUAGE_CODE)),
                userLibraryCursor.getString(userLibraryCursor.getColumnIndex(TABLES.USERLIBRARY.ATTRIBUTES.CREATED_BY)),
                userLibraryCursor.getString(userLibraryCursor.getColumnIndex(TABLES.USERLIBRARY.ATTRIBUTES.CREATED_ON)),
                userLibraryCursor.getString(userLibraryCursor.getColumnIndex(TABLES.USERLIBRARY.ATTRIBUTES.LAST_EDIT_ON)),
                getUserLibraryLines(context, userLibraryId)
        );
    }

    //DELETE ------------------------------------------------------------------------------------------------------------------------------------
    public boolean deleteUserLibrary(@NonNull Context context, int userLibraryId) {
        Log.d(TAG, "deleteUserLibrary: Trying to delete userLib with id: " + userLibraryId);
        boolean deletionSuccessful = getDb(context).delete(TABLES.USERLIBRARY.TABLE_NAME,
                TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + "=" + userLibraryId, null) > 0; //relationsships updated by cascade

        if (deletionSuccessful) {
            this.global_AllUserLibraries.remove(userLibraryId + ""); //remove from static hashmap
        } //also delete to keep uptodate

        restartNotificationService(context); //restart because userlibs might change output
        return deletionSuccessful;
    }

    /* ###########################################################################################################################################
     * ###########################################################################################################################################
     * ------- COUNTDOWN - CRUD Operations -----------------------------------------------------------------------------------------------------
     * ###########################################################################################################################################
     * ########################################################################################################################################### */
    private SparseArray<Countdown> global_AllCountdowns; //no setter, because only this class should modify it (getter is getAllCountdowns(force etc.))


    // CREATE & UPDATE ---------------------------------------------------------------------------------------------------------------------------
    public void saveCountdown(@NonNull Context context, Countdown countdown) {
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

        //Delete auflösungstabelle für countdown, because what is when countdown has now less userlibs (it would remain in zwischentabelle)
        if (getDb(context).delete(TABLES.ZWISCHENTABELLE_COU_ULB.TABLE_NAME,
                TABLES.COUNTDOWN.ATTRIBUTES.ID + "=?", new String[]{String.valueOf(countdown.getCountdownId())}) > 0) {
            Log.d(TAG, "setSaveCountdown: Deletion of entries of updated/new countdown in zwischentabelle successful.");
        }

        //Now also insertValues for zwischentabelle (AFTER countdown is inserted)
        long[] rowIdsZwischentabelle = new long[countdown.getUserSelectedUserLibraries().size()];
        int iteration = 0;
        for (UserLibrary userLibrary : countdown.getUserSelectedUserLibraries().values()) {
            ContentValues insertZwischentabelleValues = new ContentValues();
            //for every row a separate insert!! (ergo for every languagepack of countdown a separate insert)
            insertZwischentabelleValues.put(TABLES.COUNTDOWN.ATTRIBUTES.ID, countdown.getCountdownId());
            insertZwischentabelleValues.put(TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID, userLibrary.getLibId());
            rowIdsZwischentabelle[iteration++] = getDb(context).replace(TABLES.ZWISCHENTABELLE_COU_ULB.TABLE_NAME, null, insertZwischentabelleValues);
        }

        //at least evaluate one row id of the zwischentablle (if those succeeded we assume others did also)
        if (rowIdCountdown < 0 || rowIdsZwischentabelle[0] < 0) { //replace() returns -1 if failure otherwise it returns the rowid which must be equal or higher 0
            Log.e(TAG, "setSaveCountdown: Could not save/update countdown!");
            Toast.makeText(context, R.string.databaseMgr_error_saveCountdown_unsuccessful, Toast.LENGTH_SHORT).show();
        } else {
            //if successfully saved, then update also locally downloaded/extracted sparseArray (if already in RAM)
            if (this.global_AllCountdowns != null) { //so we do not reload all countdowns with getAllCountdowns(); :)
                this.global_AllCountdowns.put(countdown.getCountdownId(), countdown);
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

    // READ ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Get all countdowns from database and map them onto countdownObj
     *
     * @param forceReload: We are keeping the sparseArray uptodate, BUT if we modify our db and have a service running (external process in our case)
     *                     then we have a separate object! This means we would have outdated countdown objects!
     */
    public SparseArray<Countdown> getAllCountdowns(@NonNull Context context, boolean forceReload) { //do not use in loops! (use getAllCountdowns, because there is only ONE sql statement executed)
        Log.d(TAG, "getAllCountdowns: Trying to get all countdowns.");
        if (this.global_AllCountdowns == null || forceReload) { //only do this if not already extracted in this session! (performance enhancement :)) --> so also not extra sql query necessary when getting single countdown
            if (forceReload) {
                Log.d(TAG, "getAllCountdowns: Forcefully reloaded sparseArray.");
            } else {
                Log.d(TAG, "getAllCountdowns: Found no already extracted sparseArray for countdowns. Doing it now.");
            }
            Cursor cursorCountdown = null;
            SparseArray<Countdown> queriedCountdowns = new SparseArray<>(); //sparse array instead of hashmap because better performance for pimitives

            try {
                Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        Log.d(TAG, "Table Name=> " + c.getString(0));
                        c.moveToNext();
                    }
                }
                closeCursor(c);

                /* Following query gets executed: ---------------
                 SELECT * FROM Countdown as C
                 INNER JOIN Zwischentabelle ON countdownId = countdownID
                 INNER JOIN UserLibrary ON libId = libId
                 */
                cursorCountdown = getDb(context).rawQuery("SELECT * FROM " +
                        TABLES.COUNTDOWN.TABLE_NAME + " as C" +
                        " INNER JOIN " + TABLES.ZWISCHENTABELLE_COU_ULB.TABLE_NAME + " AS ZT ON C." + TABLES.COUNTDOWN.ATTRIBUTES.ID + "=ZT." + TABLES.COUNTDOWN.ATTRIBUTES.ID +
                        " INNER JOIN " + TABLES.USERLIBRARY.TABLE_NAME + " AS U ON ZT." + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + "=U." + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + ";", null);

                while (cursorCountdown.moveToNext()) {
                    Countdown countdown = getCountdownFromCursor(context, cursorCountdown); //temporary cache-saving, to get countdownId for Index!
                    Log.d(TAG, "getAllCountdowns: Found countdown-> " + countdown.toString());
                    queriedCountdowns.put(countdown.getCountdownId(), countdown);
                }
                this.global_AllCountdowns = (queriedCountdowns); //save all queried countdowns so we do not have to do this procedure again for runtime :)
            } finally { //always finally for closing cursor! (also in error case)
                closeCursor(cursorCountdown);
            }
        } //no else necessary, because allCountdowns already set
        Log.d(TAG, "getAllCountdowns: Length of returned sparseArray: " + this.global_AllCountdowns.size());
        return this.global_AllCountdowns;
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


    /**
     * This method will map a queried row to a countdownObj.
     * ATTENTION: @param cursorRow should be closed() in parent-method in a finally block!
     */
    private Countdown getCountdownFromCursor(@NonNull Context context, @NonNull Cursor countdownCursor) {
        //TRUE = 1 | FALSE = 0 --> if == 1 then just give true otherwise false
        return new Countdown(context,
                countdownCursor.getInt(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.ID)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.TITLE)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.DESCRIPTION)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.STARTDATETIME)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.UNTILDATETIME)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.CREATEDDATETIME)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.LASTEDITDATETIME)),
                countdownCursor.getString(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.CATEGORYCOLOR)),
                (countdownCursor.getInt(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.RANDOMNOTIFICATIONMOTIVATION)) == 1),
                countdownCursor.getInt(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.NOTIFICATIONINTERVAL)),
                (countdownCursor.getInt(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.LIVECOUNTDOWN)) == 1),
                getUserLibrariesOfCountdownId(context, countdownCursor.getInt(countdownCursor.getColumnIndex(TABLES.COUNTDOWN.ATTRIBUTES.ID))));
    }

    private HashMap<String, UserLibrary> getUserLibrariesOfCountdownId(@NonNull Context context, int countdownId) {
        HashMap<String, UserLibrary> countdownUserLibraries = new HashMap<>();

        Cursor selectedUserLibraries = getDb(context).rawQuery("SELECT * FROM " +
                TABLES.COUNTDOWN.TABLE_NAME + " as C" +
                " INNER JOIN " + TABLES.ZWISCHENTABELLE_COU_ULB.TABLE_NAME + " AS ZT ON C." + TABLES.COUNTDOWN.ATTRIBUTES.ID + "=ZT." + TABLES.COUNTDOWN.ATTRIBUTES.ID +
                " INNER JOIN " + TABLES.USERLIBRARY.TABLE_NAME + " AS U ON ZT." + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID + "=U." + TABLES.USERLIBRARY.ATTRIBUTES.LIB_ID +
                " WHERE C." + TABLES.COUNTDOWN.ATTRIBUTES.ID + "=" + countdownId+";",null);

        while (selectedUserLibraries.moveToNext()) {
            UserLibrary userLibrary = getUserLibraryFromCursor(context, selectedUserLibraries);
            countdownUserLibraries.put(userLibrary.getLibId() + "", userLibrary);
        }
        closeCursor(selectedUserLibraries);
        return countdownUserLibraries;
    }

    //DELETE ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Deletes ALL countdowns and returns number of deleted rows
     * (not whether it was successful or not)
     */
    public int deleteAllCountdowns(@NonNull Context context) {
        Log.d(TAG, "deleteAllCountdowns: Trying to delete all countdowns.");

        //Deletes all countdowns
        int amountRowsDeleted = getDb(context).delete(TABLES.COUNTDOWN.TABLE_NAME, "1", null); //no. 1 says, that we want to return not whether deletion was successful, but how many rows we deleted
        amountRowsDeleted += getDb(context).delete(TABLES.ZWISCHENTABELLE_COU_ULB.TABLE_NAME, "1", null); //also delete auflösungstabelle (but not languagepacks itself)
        this.global_AllCountdowns = (null); //also delete saved object!

        //Restart service (because new/less services etc. / changed settings) [must be AFTER DELETION and BEFORE return (logically)!]
        restartNotificationService(context);

        return amountRowsDeleted;
    }

    public boolean deleteCountdown(@NonNull Context context, int countdownId) {
        Log.d(TAG, "deleteCountdown: Trying to delete countdown with id: " + countdownId);
        boolean deletionSuccessful = getDb(context).delete(TABLES.COUNTDOWN.TABLE_NAME,
                TABLES.COUNTDOWN.ATTRIBUTES.ID + "=?", //do this for preventing sql injections!
                new String[]{String.valueOf(countdownId)}) > 0;

        if (deletionSuccessful) {
            getAllCountdowns(context, false).delete(countdownId);
        } //also delete from object to keep it uptodate

        //Restart service because countown got removed
        restartNotificationService(context); //TODO: not here in storage mgr (own service mgr for all of them maybe merge them or similar i do not know)
        return deletionSuccessful;
    }


    /* ################################################################################################################
     * ########### Other methods #######################################################################################
     * ################################################################################################################# */


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

}
