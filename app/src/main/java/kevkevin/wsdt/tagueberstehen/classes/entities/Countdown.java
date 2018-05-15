package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import kevkevin.wsdt.tagueberstehen.annotations.Bug;
import kevkevin.wsdt.tagueberstehen.annotations.Test;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.interfaces.IConstants_Global;
import kevkevin.wsdt.tagueberstehen.R;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;
import static kevkevin.wsdt.tagueberstehen.classes.interfaces.IConstants_Countdown.*;
import org.greenrobot.greendao.DaoException;

@Entity
public class Countdown {
    @Transient
    private static final String TAG = "Countdown";
    @Transient
    private Context context;
    @Id (autoincrement = true)
    private long couId; //unique id of countdown
    @NotNull //bc. user has to put in at least one char
    private String couTitle; //name of countdown
    private String couDescription; //description for countdown
    private String couStartDateTime; //when countdown needs to start counting, could be now
    private String couUntilDateTime; //when is countdown 0
    private String couCreatedDateTime; //when was countdown firstly created
    private String couLastEditDateTime; //last edit of countdown
    private String couCategoryColor; //hexadecimal color etc.
    private boolean couIsMotivationOn;
    private int couMotivationIntervalSeconds; //in seconds!
    private boolean couIsLiveCountdownOn; //show Foreground service live countdown if countdown until start date constraints true
    @ToMany
    @JoinEntity(entity = ZT_CountdownUserlibrary.class,
        sourceProperty = "couId",targetProperty = "libId") //for N:M
    private List<UserLibrary> couSelectedUserLibraries; //by default local language will be chosen
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 2105327365)
    private transient CountdownDao myDao;


    //Constructor for lastEdit/couCreatedDateTime automatically
    public Countdown(Context context, String couTitle, String couDescription, String couStartDateTime, String couUntilDateTime, String couCategoryColor, boolean couIsMotivationOn, int couMotivationIntervalSeconds, boolean couIsLiveCountdownOn, List<UserLibrary> couSelectedUserLibraries) {
        this.setContext(context);
        //this.setCouId(DatabaseMgr.getSingletonInstance(context).getNextCountdownId(context)); //get next countdown id (fill gap from deleted countdown or just increment)
        this.setCouTitle(couTitle);
        this.setCouDescription(couDescription);
        this.setCouStartDateTime(couStartDateTime);
        this.setCouUntilDateTime(couUntilDateTime);
        this.setCouCreatedDateTime(getCurrentDateTimeStr());
        this.setCouLastEditDateTime(getCurrentDateTimeStr());
        this.setCouCategoryColor(couCategoryColor);
        this.setCouIsMotivationOn(couIsMotivationOn);
        this.setCouMotivationIntervalSeconds(couMotivationIntervalSeconds);
        this.setCouIsLiveCountdownOn(couIsLiveCountdownOn);
        this.setCouSelectedUserLibraries(couSelectedUserLibraries);
    }

    //Constructor for all fields
    public Countdown(Context context, int couId, String couTitle, String couDescription, String couStartDateTime, String couUntilDateTime, String couCreatedDateTime, String couLastEditDateTime, String couCategoryColor, boolean couIsMotivationOn, int couMotivationIntervalSeconds, boolean couIsLiveCountdownOn, List<UserLibrary> couSelectedUserLibraries) {
        this.setContext(context);
        this.setCouId(couId);
        this.setCouTitle(couTitle);
        this.setCouDescription(couDescription);
        this.setCouStartDateTime(couStartDateTime);
        this.setCouUntilDateTime(couUntilDateTime);
        this.setCouCreatedDateTime(couCreatedDateTime);
        this.setCouLastEditDateTime(couLastEditDateTime);
        this.setCouCategoryColor(couCategoryColor);
        this.setCouIsMotivationOn(couIsMotivationOn);
        this.setCouMotivationIntervalSeconds(couMotivationIntervalSeconds);
        this.setCouIsLiveCountdownOn(couIsLiveCountdownOn);
        this.setCouSelectedUserLibraries(couSelectedUserLibraries);
    }

    @Generated(hash = 684191569)
    public Countdown(long couId, @NotNull String couTitle, String couDescription, String couStartDateTime, String couUntilDateTime, String couCreatedDateTime, String couLastEditDateTime, String couCategoryColor, boolean couIsMotivationOn, int couMotivationIntervalSeconds, boolean couIsLiveCountdownOn) {
        this.couId = couId;
        this.couTitle = couTitle;
        this.couDescription = couDescription;
        this.couStartDateTime = couStartDateTime;
        this.couUntilDateTime = couUntilDateTime;
        this.couCreatedDateTime = couCreatedDateTime;
        this.couLastEditDateTime = couLastEditDateTime;
        this.couCategoryColor = couCategoryColor;
        this.couIsMotivationOn = couIsMotivationOn;
        this.couMotivationIntervalSeconds = couMotivationIntervalSeconds;
        this.couIsLiveCountdownOn = couIsLiveCountdownOn;
    }

    @Generated(hash = 1901071775)
    public Countdown() {
    }

    /**
     * getEventMsg:
     *
     * @param eventMessageLinearLayout: If linearlayout is null, then method returns eventMsg as string, otherwise it sets the text of the view etc.
     */
    public String getEventMsgOrAndSetView(@Nullable LinearLayout eventMessageLinearLayout) { //Only one msg (most relevant one) e.g. Expired, Starts on x, Expires on ..
        //Craft event messages for countdown nodes (e.g.)
        String eventMsgStr;
        int textColor; //is only used when textView is not null
        int eventIcon; //only used when textView not null

        if (!this.isStartDateInThePast()) {
            //startdate in future (starts on x)
            eventMsgStr = String.format(this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownNotStartedYet), this.getCouStartDateTime());
            textColor = R.color.colorPrimaryDark;
            eventIcon = R.drawable.colorblue_eventmsg_startdateinfuture;
        } else if (this.isUntilDateInTheFuture()) {
            //because of else if startdate in past and untildateinfuture (= running)
            eventMsgStr = String.format(this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownRunning),
                    (this.isCouIsMotivationOn() || this.isCouIsLiveCountdownOn()) ? this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownRunning_MotivationStatus_active) : this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownRunning_MotivationStatus_inactive));
            textColor = R.color.colorGreen;
            eventIcon = R.drawable.primarygreen_eventmsg_motivationon;
        } else {
            //because of else if startdate in past, untildate in past --> expired
            eventMsgStr = String.format(this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownExpired), this.getCouUntilDateTime());
            textColor = R.color.colorRed;
            eventIcon = R.drawable.colorred_eventmsg_countdownexpired;
        }

        if (eventMessageLinearLayout != null) {
            TextView eventMsgTextView = eventMessageLinearLayout.findViewById(R.id.countdownEventMsg);
            ImageView eventMsgImageView = eventMessageLinearLayout.findViewById(R.id.countdownEventMsgIcon);
            if (eventMsgTextView != null) {
                eventMsgTextView.setText(eventMsgStr);
                eventMsgTextView.setTextColor(this.getContext().getResources().getColor(textColor));
            } //not else if!
            if (eventMsgImageView != null) {
                eventMsgImageView.setImageBitmap(BitmapFactory.decodeResource(this.getContext().getResources(), eventIcon)); // when textview found there must be also imageview
            }
            Log.d(TAG, "getEventMsg: Tried to set message and color etc. to view. Color: " + textColor);
        }

        return eventMsgStr;
    }

    public void savePersistently() {
        DatabaseMgr.getSingletonInstance(this.getContext()).saveCountdown(this.getContext(),this);
    }


    @Test (message = "Not sure if method returns it correctly. When receiving different booleans.")
    @Bug (message = "possible bug here. IF YES: Then I did it on all places wrong, so just rename boolean and method :P")
    /** Method calculates Remaining or passedPercentage (this method does not format percentage --> use helper method */
    public double getRemainingPercentage(boolean getRemainingOtherwisePassedPercentage) { //min is 1, if 0 then it will be still min 1 nachkommastelle (but always 0!) because of double format itself
        try {
            double all100percentSeconds = Long.valueOf((getDateTime(getCouUntilDateTime()).getTimeInMillis() - getDateTime(getCouStartDateTime()).getTimeInMillis()) / 1000).doubleValue();
            double leftXpercentSeconds = Long.valueOf((getDateTime(getCouUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000).doubleValue();
            Log.d(TAG, "getRemainingPercentage: "+all100percentSeconds+" // "+leftXpercentSeconds);

            double percentageValueUnformatted;
            if (getRemainingOtherwisePassedPercentage) {
                percentageValueUnformatted = (leftXpercentSeconds / all100percentSeconds) * 100;
            } else {
                percentageValueUnformatted = 100 - ((leftXpercentSeconds / all100percentSeconds) * 100); //get passed percentage if false
            }

            Log.d(TAG, "getRemainingPercentage:Unformatted: "+percentageValueUnformatted);

            //Double result = Double.parseDouble((new DecimalFormat("##,"+nachkommaStellen)).format((leftXpercentSeconds / all100percentSeconds) * 100)); //formatting percentage to 2 nachkommastellen
            return (percentageValueUnformatted >= 0) ? ((percentageValueUnformatted <= 100) ? percentageValueUnformatted : 100) : 0; //always return 0-100
        } catch (NullPointerException | NumberFormatException e) {
            Log.e(TAG, "getRemainingPercentage: Could not calculate remaining percentage.");
            e.printStackTrace();
        }
        return (-1); //to show error
    }

    public String getTotalSecondsNoScientificNotation() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(IConstants_Global.GLOBAL.THOUSAND_GROUPING_SEPERATOR); //set separator
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        return decimalFormat.format(this.getTotalSeconds());
    }

    public boolean isStartDateInThePast() {
        return (getDateTime(getCouStartDateTime()).before(getCurrentDateTime()));
        //Works but time comparison does not work properly: return (getDateTime(getCurrentDateTime()).compareTo(getCouStartDateTime()) > 0); //only if in the past or NOW
    }

    public boolean isUntilDateInTheFuture() {
        return (getCurrentDateTime().before(getDateTime(getCouUntilDateTime())));
        //Works but time comparison does not work properly: return (getDateTime(getCouUntilDateTime()).compareTo(getCurrentDateTime()) > 0); //only if in the future
    }

    public Double getTotalSeconds() {
        Resources res = this.getContext().getResources();
        Double totalSeconds = 0D;
        try {
            if (isUntilDateInTheFuture()) {
                if (isStartDateInThePast()) {
                    //untildateinfuture and startdate in past (running)
                    totalSeconds = Long.valueOf((getDateTime(getCouUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000).doubleValue();
                    Log.d(TAG, "getTotalSeconds: Countdown running->Seconds: "+totalSeconds);
                    if (totalSeconds < 0D) {
                        Log.e(TAG, "getTotalSeconds: TotalSeconds negative. This should not be possible.");
                    }
                } else {
                    //untildateinfuture but startdate also in future (not started yet)
                    Toast.makeText(this.getContext(), String.format(res.getString(R.string.countdown_info_startDateInFuture), this.getCouStartDateTime()), Toast.LENGTH_SHORT).show();
                    totalSeconds = 0D;
                    Log.d(TAG, "getTotalSeconds: Countdown not started yet.");
                }
            } else {
                //untildateinpast (expired
                totalSeconds = 0D; //prevent from counting to infinity (because negative)
                Log.d(TAG, "getTotalSeconds: Countdown has expired.");
            }
        } catch (NullPointerException e) {
            Log.e("getTotalSeconds", "totalSeconds could not be calculated. Nullpointerexception!");
            e.printStackTrace();
        }
        return totalSeconds;
    }

    // DATE FUNCTIONS --------------------------------------------------------------------
    private String getCurrentDateTimeStr() {
        return getDateTime(new GregorianCalendar());
    }

    private GregorianCalendar getCurrentDateTime() {
        return new GregorianCalendar();
    }

    private String getDateTime(int day, int month, int year, int hour, int minute, int second) {
        return getDateTime(new GregorianCalendar(year, month, day, hour, minute, second));
    }

    //also needed in ModifyCountdownActivity
    public GregorianCalendar getDateTime(String formattedDate) {
        DateFormat df = new SimpleDateFormat(IConstants_Global.GLOBAL.DATETIME_FORMAT, Locale.getDefault());
        GregorianCalendar result;
        try {
            Date date = df.parse(formattedDate);
            result = new GregorianCalendar();
            result.setTime(date);
        } catch (ParseException e) {
            Log.e("getDateTime", "Could not parse Stringdate to GregorianCalender Date.");
            e.printStackTrace();
            return null; //abort further execution of method
        }
        return result;
    }

    private String getDateTime(GregorianCalendar c) { //IMPORTANT: Separation of dot must be the same for all attributes
        return c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR) + " " + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
    }


    // ESCAPE METHODS FOR SHARED PREFERENCES ------------------------------------

    private String escapeEnter(@NonNull String string) { //so nicer for UI (node showing)
        Log.d(TAG, "escapeEnter: Trying to escape string for enter!");
        /* Used for CustomEdittext e.g. where no enter is allowed (so do not call this function on all countdown values (because we do not know whether new errors occur)*/
        for (String illegalCharacter : ESCAPE.escapeEnter_illegalCharacters) {
            string = string.replaceAll(illegalCharacter, ESCAPE.escapeEnter_legalCharacter);
        }
        return string;
    }


    // GETTER / SETTER ++++++++++++++++++++++++++++++++++++++++++++
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public long getCouId() {
        return couId;
    }

    public void setCouId(long couId) {
        this.couId = couId;
    }

    public String getCouTitle() {
        return couTitle;
    }

    public void setCouTitle(String couTitle) { //also escape enter here, because we have a customedittext, which blocks enter (do not call this escapeEnter-method on other values [error preventing])
        this.couTitle = escapeEnter(DatabaseMgr.escapeString(couTitle));
    }

    public String getCouDescription() {
        return couDescription;
    }

    public void setCouDescription(String couDescription) { //also escape enter here, because we have a customedittext, which blocks enter
        this.couDescription = escapeEnter(DatabaseMgr.escapeString(couDescription));
    }

    public String getCouUntilDateTime() {
        return couUntilDateTime;
    }

    public void setCouUntilDateTime(String couUntilDateTime) {
        this.couUntilDateTime = DatabaseMgr.escapeString(couUntilDateTime);
    }

    public String getCouCreatedDateTime() {
        return couCreatedDateTime;
    }

    public void setCouCreatedDateTime(String couCreatedDateTime) {
        this.couCreatedDateTime = DatabaseMgr.escapeString(couCreatedDateTime);
    }

    public String getCouLastEditDateTime() {
        return couLastEditDateTime;
    }

    public void setCouLastEditDateTime(String couLastEditDateTime) {
        this.couLastEditDateTime = DatabaseMgr.escapeString(couLastEditDateTime);
    }

    public String getCouCategoryColor() {
        return couCategoryColor;
    }

    public void setCouCategoryColor(String couCategoryColor) { //#000000
        this.couCategoryColor = DatabaseMgr.escapeString(couCategoryColor);
    }

    public boolean isCouIsMotivationOn() {
        return couIsMotivationOn;
    }

    public void setCouIsMotivationOn(boolean couIsMotivationOn) {
        this.couIsMotivationOn = couIsMotivationOn;
    }

    public String getCouStartDateTime() {
        return couStartDateTime;
    }

    public void setCouStartDateTime(String couStartDateTime) {
        this.couStartDateTime = DatabaseMgr.escapeString(couStartDateTime);
    }

    public int getCouMotivationIntervalSeconds() {
        return couMotivationIntervalSeconds;
    }

    public void setCouMotivationIntervalSeconds(int couMotivationIntervalSeconds) {
        this.couMotivationIntervalSeconds = couMotivationIntervalSeconds;
    }

    public boolean isCouIsLiveCountdownOn() {
        return couIsLiveCountdownOn;
    }

    public void setCouIsLiveCountdownOn(boolean couIsLiveCountdownOn) {
        this.couIsLiveCountdownOn = couIsLiveCountdownOn;
    }


    @Override
    public String toString() {
        // couUntilDateTime, String couCategoryColor, boolean couIsMotivationOn, int couMotivationIntervalSeconds, boolean couIsLiveCountdownOn) {
        String separator = ";"; //do not use character or number for separator (because of + polymorphism)
        return getCouId()+separator+
                getCouTitle()+separator +
                getCouDescription()+separator +
                getCouStartDateTime()+separator +
                getCouUntilDateTime()+separator +
                getCouCreatedDateTime()+separator +
                getCouLastEditDateTime()+separator +
                getCouCategoryColor()+separator +
                isCouIsMotivationOn()+separator +
                getCouMotivationIntervalSeconds()+separator +
                isCouIsLiveCountdownOn()+separator +
                getCouSelectedUserLibraries();
    }

    /** Necessary to determine which languagepacks are used for this countdown. (MIGHT RETURN NULL!)*/
    public String getRandomQuoteSuitableForCountdown() {
        List<UserLibrary> userLibraries = this.getCouSelectedUserLibraries();

        if (userLibraries.size() <= 0) { //no userlibs defined!
            Log.w(TAG, "getRandomQuoteSuitableForCountdown: No userLibs for countdown defined! Returned fallbackNotification.");
            return this.getContext().getResources().getString(R.string.error_contactAdministrator);
        }

        //Get random userLibrary of countdownUserLibs (transform to arraylist before, because libId might not be inkrementally!! (rather arbitrary)
        UserLibrary randomUserLibrary = this.getCouSelectedUserLibraries().get(HelperClass.getRandomInt(0,this.getCouSelectedUserLibraries().size()-1));

        //get random userLibraryLine of userLib
        return randomUserLibrary.getLines().get(HelperClass.getRandomInt(0,randomUserLibrary.getLines().size()-1));
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Keep //Keep bc. long here not possible and greendao is too stupid for this
    public List<UserLibrary> getCouSelectedUserLibraries() {
        if (couSelectedUserLibraries == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserLibraryDao targetDao = daoSession.getUserLibraryDao();
            List<UserLibrary> couSelectedUserLibrariesNew = targetDao._queryCountdown_CouSelectedUserLibraries((int) this.getCouId());
            synchronized (this) {
                if (couSelectedUserLibraries == null) {
                    couSelectedUserLibraries = couSelectedUserLibrariesNew;
                }
            }
        }
        return couSelectedUserLibraries;
    }

    private void setCouSelectedUserLibraries(List<UserLibrary> couSelectedUserLibraries) {
        //should only be called by setQuotesLStr(), because this method does not update hashmap
        this.couSelectedUserLibraries = couSelectedUserLibraries;
    }

    public boolean getCouIsMotivationOn() {
        return this.couIsMotivationOn;
    }

    public boolean getCouIsLiveCountdownOn() {
        return this.couIsLiveCountdownOn;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1601497361)
    public synchronized void resetCouSelectedUserLibraries() {
        couSelectedUserLibraries = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1723812481)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCountdownDao() : null;
    }
}
