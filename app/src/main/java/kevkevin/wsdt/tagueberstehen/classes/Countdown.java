package kevkevin.wsdt.tagueberstehen.classes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;


public class Countdown {
    private Context context;
    private int countdownId; //unique id of countdown
    private String countdownTitle; //name of countdown
    private String countdownDescription; //description for countdown
    private String startDateTime; //when countdown needs to start counting, could be now
    private String untilDateTime; //when is countdown 0
    private String createdDateTime; //when was countdown firstly created
    private String lastEditDateTime; //last edit of countdown
    private String category; //hexadecimal color etc.
    private boolean isActive;
    private int notificationInterval; //in seconds!
    private boolean showLiveCountdown; //show Foreground service live countdown if countdown until start date constraints true
    private HashMap<String, Languagepack> quotesLanguagePacksObj; //by default local language will be chosen
    private String[] quotesLanguagePacksStr; //for random e.g.
    private static final String TAG = "Countdown";

    //Members for service (do not save them explicitely [unneccesary])
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private Handler handler = new Handler();


    //Constructor for lastEdit/createdDateTime automatically
    public Countdown(Context context, String countdownTitle, String countdownDescription, String startDateTime, String untilDateTime, String category, boolean isActive, int notificationInterval, boolean showLiveCountdown, String[] quotesLanguagePacks) {
        this.setContext(context);
        this.setCountdownId(DatabaseMgr.getSingletonInstance(context).getNextCountdownId(context)); //get next countdown id (fill gap from deleted countdown or just increment)
        this.setCountdownTitle(countdownTitle);
        this.setCountdownDescription(countdownDescription);
        this.setStartDateTime(startDateTime);
        this.setUntilDateTime(untilDateTime);
        this.setCreatedDateTime(getCurrentDateTimeStr());
        this.setLastEditDateTime(getCurrentDateTimeStr());
        this.setCategory(category);
        this.setActive(isActive);
        this.setNotificationInterval(notificationInterval);
        this.setShowLiveCountdown(showLiveCountdown);
        this.setQuotesLanguagePacksStr(quotesLanguagePacks);
    }

    //Constructor for all fields
    public Countdown(Context context, int countdownId, String countdownTitle, String countdownDescription, String startDateTime, String untilDateTime, String createdDateTime, String lastEditDateTime, String category, boolean isActive, int notificationInterval, boolean showLiveCountdown, String[] quotesLanguagePacks) {
        this.setContext(context);
        this.setCountdownId(countdownId);
        this.setCountdownTitle(countdownTitle);
        this.setCountdownDescription(countdownDescription);
        this.setStartDateTime(startDateTime);
        this.setUntilDateTime(untilDateTime);
        this.setCreatedDateTime(createdDateTime);
        this.setLastEditDateTime(lastEditDateTime);
        this.setCategory(category);
        this.setActive(isActive);
        this.setNotificationInterval(notificationInterval);
        this.setShowLiveCountdown(showLiveCountdown);
        this.setQuotesLanguagePacksStr(quotesLanguagePacks);
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
            eventMsgStr = String.format(this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownNotStartedYet), this.getStartDateTime());
            textColor = R.color.colorBlue;
            eventIcon = R.drawable.colorblue_eventmsg_startdateinfuture;
        } else if (this.isUntilDateInTheFuture()) {
            //because of else if startdate in past and untildateinfuture (= running)
            eventMsgStr = String.format(this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownRunning),
                    (this.isActive() || this.isShowLiveCountdown()) ? this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownRunning_MotivationStatus_active) : this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownRunning_MotivationStatus_inactive));
            textColor = R.color.colorPrimaryDark;
            eventIcon = R.drawable.primarygreen_eventmsg_motivationon;
        } else {
            //because of else if startdate in past, untildate in past --> expired
            eventMsgStr = String.format(this.getContext().getResources().getString(R.string.node_countdownEventMsg_countdownExpired), this.getUntilDateTime());
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
        DatabaseMgr.getSingletonInstance(this.getContext()).setSaveCountdown(this.getContext(),this);
    }


    /** Method calculates Remaining or passedPercentage (this method does not format percentage --> use helper method */
    public double getRemainingPercentage(boolean getRemainingOtherwisePassedPercentage) { //min is 1, if 0 then it will be still min 1 nachkommastelle (but always 0!) because of double format itself
        try {
            double all100percentSeconds = Long.valueOf((getDateTime(getUntilDateTime()).getTimeInMillis() - getDateTime(getStartDateTime()).getTimeInMillis()) / 1000).doubleValue();
            double leftXpercentSeconds = Long.valueOf((getDateTime(getUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000).doubleValue();


            double percentageValueUnformatted;
            if (getRemainingOtherwisePassedPercentage) {
                percentageValueUnformatted = (leftXpercentSeconds / all100percentSeconds) * 100;
            } else {
                percentageValueUnformatted = 100 - ((leftXpercentSeconds / all100percentSeconds) * 100); //get passed percentage if false
            }

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
        decimalFormatSymbols.setGroupingSeparator(Constants.GLOBAL.THOUSAND_GROUPING_SEPERATOR); //set separator
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        return decimalFormat.format(this.getTotalSeconds());
    }

    public boolean isStartDateInThePast() {
        return (getDateTime(getStartDateTime()).compareTo(getCurrentDateTime()) <= 0); //only if in the past or NOW
    }

    public boolean isUntilDateInTheFuture() {
        return (getDateTime(getUntilDateTime()).compareTo(getCurrentDateTime()) > 0); //only if in the future
    }

    public Double getTotalSeconds() {
        Resources res = this.getContext().getResources();
        Double totalSeconds = 0D;
        try {
            if (getDateTime(getStartDateTime()).compareTo(getCurrentDateTime()) > 0) {
                //date is in the future
                Toast.makeText(this.getContext(), String.format(res.getString(R.string.countdown_info_startDateInFuture), this.getStartDateTime()), Toast.LENGTH_SHORT).show();
                totalSeconds = 0D; //prevent from counting to infinity (because negative)
            } else {
                //date is in the past and countdown started already
                totalSeconds = Long.valueOf((getDateTime(getUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000).doubleValue();
            }
        } catch (NullPointerException e) {
            Log.e("getTotalSeconds", "totalSeconds could not be calculated. Nullpointerexception!");
            e.printStackTrace();
        }
        if (totalSeconds < 0) {
            Toast.makeText(this.getContext(), String.format(res.getString(R.string.countdown_info_untilDateInPast), this.getUntilDateTime()), Toast.LENGTH_SHORT).show();
            totalSeconds = 0D; //prevent from counting to infinity (because negative)
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
        DateFormat df = new SimpleDateFormat(Constants.GLOBAL.DATETIME_FORMAT, Locale.GERMAN);
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
        for (String illegalCharacter : Constants.COUNTDOWN.ESCAPE.escapeEnter_illegalCharacters) {
            string = string.replaceAll(illegalCharacter, Constants.COUNTDOWN.ESCAPE.escapeEnter_legalCharacter);
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

    public int getCountdownId() {
        return countdownId;
    }

    public void setCountdownId(int countdownId) {
        this.countdownId = countdownId;
    }

    public String getCountdownTitle() {
        return countdownTitle;
    }

    public void setCountdownTitle(String countdownTitle) { //also escape enter here, because we have a customedittext, which blocks enter (do not call this escapeEnter-method on other values [error preventing])
        this.countdownTitle = escapeEnter(DatabaseMgr.escapeString(countdownTitle));
    }

    public String getCountdownDescription() {
        return countdownDescription;
    }

    public void setCountdownDescription(String countdownDescription) { //also escape enter here, because we have a customedittext, which blocks enter
        this.countdownDescription = escapeEnter(DatabaseMgr.escapeString(countdownDescription));
    }

    public String getUntilDateTime() {
        return untilDateTime;
    }

    public void setUntilDateTime(String untilDateTime) {
        this.untilDateTime = DatabaseMgr.escapeString(untilDateTime);
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = DatabaseMgr.escapeString(createdDateTime);
    }

    public String getLastEditDateTime() {
        return lastEditDateTime;
    }

    public void setLastEditDateTime(String lastEditDateTime) {
        this.lastEditDateTime = DatabaseMgr.escapeString(lastEditDateTime);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) { //#000000
        this.category = DatabaseMgr.escapeString(category);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = DatabaseMgr.escapeString(startDateTime);
    }

    public int getNotificationInterval() {
        return notificationInterval;
    }

    public void setNotificationInterval(int notificationInterval) {
        this.notificationInterval = notificationInterval;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }

    public void setTimerTask(TimerTask timerTask) {
        this.timerTask = timerTask;
    }

    public Handler getHandler() {
        return handler;
    }

    public boolean isShowLiveCountdown() {
        return showLiveCountdown;
    }

    public void setShowLiveCountdown(boolean showLiveCountdown) {
        this.showLiveCountdown = showLiveCountdown;
    }
    //set handler not required because directly assigned on top

    @Override
    public String toString() {
        // untilDateTime, String category, boolean isActive, int notificationInterval, boolean showLiveCountdown) {
        String separator = ";"; //do not use character or number for separator (because of + polymorphism)
        return getCountdownId()+separator+
                getCountdownTitle()+separator +
                getCountdownDescription()+separator +
                getStartDateTime()+separator +
                getUntilDateTime()+separator +
                getCreatedDateTime()+separator +
                getLastEditDateTime()+separator +
                getCategory()+separator +
                isActive()+separator +
                getNotificationInterval()+separator +
                isShowLiveCountdown()+separator +
                getQuotesLanguagePacksObj();
    }

    /** Necessary to determine which languagepacks are used for this countdown. (MIGHT RETURN NULL!)*/
    public Quote getRandomQuoteSuitableForCountdown() {
        Quote fallbackQuoteErrorCase = new Quote(this.getContext(),-1,this.getContext().getResources().getString(R.string.error_contactAdministrator),Constants.STORAGE_MANAGERS.DATABASE_STR_MGR.TABLES.QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0]); //no matter which language pack we return
        HashMap<String, Languagepack> languagepacks = this.getQuotesLanguagePacksObj();

        if (languagepacks.size() <= 0) { //no languagepacks defined!
            Log.w(TAG, "getRandomQuoteSuitableForCountdown: No languagepack for countdown defined! Returned fallbackNotification.");
            return fallbackQuoteErrorCase;
        } else if (languagepacks.size() != this.getQuotesLanguagePacksStr().length) {
            Log.w(TAG, "getRandomQuoteSuitableForCountdown: Hashmap and string array of language packs does not have the same size. Might cause arrayoutofbounds!");
            return fallbackQuoteErrorCase;
        }

        //this way every language is shown the same probability (only drawback: languagepacks with less quotes might show more probably the same quotes again)
        //IMPORTANT: String array of language packs and hashmap need the same size so keep them uptodate! (we are doing above a if to prevent such error cases, but user gets notified about it)
        SparseArray<Quote> languageQuotes = languagepacks.get(this.getQuotesLanguagePacksStr()[HelperClass.getRandomInt(0,languagepacks.size()-1)]).getLanguagePackQuotes(this.getContext());
        if (languageQuotes.size() <= 0) {
            Log.w(TAG, "getRandomQuoteSuitableForCountdown: No quote for languagepack for countdown defined! Returned fallbackNotification.");
            return fallbackQuoteErrorCase;
        }
        //IMPORTANT: Hier ausnahmsweise valueAt(), WEIL wir hier den index selbst suchen und nicht den Key!
        return languageQuotes.valueAt(HelperClass.getRandomInt(0,languageQuotes.size()-1));
    }

    public HashMap<String, Languagepack> getQuotesLanguagePacksObj() {
        return quotesLanguagePacksObj;
    }

    private void setQuotesLanguagePacksObj(HashMap<String, Languagepack> quotesLanguagePacksObj) {
        //should only be called by setQuotesLStr(), because this method does not update hashmap
        this.quotesLanguagePacksObj = quotesLanguagePacksObj;
    }

    public void setQuotesLanguagePacksStr(String[] quotesLanguagePacks) { //additional setter (easier for constructor etc.) because no extra object creation necessary
        this.quotesLanguagePacksStr = quotesLanguagePacks; //IMPORTANT: That string array and hashmap are uptodate otherwise we will get errors!
        //now also refresh hashmap
        HashMap<String,Languagepack> usedLanguagePacks = new HashMap<>();
        for (String langPack : quotesLanguagePacks) {
            Log.d(TAG, "getQuotesLanguagePacks_Quotes: Trying to evaluate language pack->"+langPack);
            Languagepack languagepack = Languagepack.getAllLanguagePacks(this.getContext()).get(langPack);
            if (languagepack != null) {
                usedLanguagePacks.put(languagepack.getLangPackId(),languagepack);
            }
        }

        //If NO valid language found then just report fallback language (english)
        if (usedLanguagePacks.size() <= 0) {
            Log.d(TAG, "getQuotesLanguagePacks_Quotes: Languages not found. Used fallback language.");
            String fallBackLanguagePack = Constants.STORAGE_MANAGERS.DATABASE_STR_MGR.TABLES.QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0];
            usedLanguagePacks.put(fallBackLanguagePack,Languagepack.getAllLanguagePacks(this.getContext()).get(fallBackLanguagePack));
        }
        this.setQuotesLanguagePacksObj(usedLanguagePacks); //now use other setter
    }

    public String[] getQuotesLanguagePacksStr() {
        return quotesLanguagePacksStr;
    }
}
