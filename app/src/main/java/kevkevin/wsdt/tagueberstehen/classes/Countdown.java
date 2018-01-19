package kevkevin.wsdt.tagueberstehen.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.os.Handler;
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
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;


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
    private static final String TAG = "Countdown";
    private static final String DATE_FORMAT = "dd.MM.yyyy hh:mm:ss";
    public static final String DATE_FORMAT_REGEX = "\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}"; //mainly for other classes

    //Members for service (do not save them explicitely [unneccesary])
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private Handler handler = new Handler();


    //Constructor for lastEdit/createdDateTime automatically
    public Countdown(Context context, String countdownTitle, String countdownDescription, String startDateTime, String untilDateTime, String category, boolean isActive, int notificationInterval) {
        this.setContext(context);
        this.setCountdownId((new InternalCountdownStorageMgr(context)).getNextCountdownId()); //get next countdown id (fill gap from deleted countdown or just increment)
        this.setCountdownTitle(countdownTitle);
        this.setCountdownDescription(countdownDescription);
        this.setStartDateTime(startDateTime);
        this.setUntilDateTime(untilDateTime);
        this.setCreatedDateTime(getCurrentDateTimeStr());
        this.setLastEditDateTime(getCurrentDateTimeStr());
        this.setCategory(category);
        this.setActive(isActive);
        this.setNotificationInterval(notificationInterval);
    }

    //Constructor for all fields
    public Countdown(Context context, int countdownId, String countdownTitle, String countdownDescription, String startDateTime, String untilDateTime, String createdDateTime, String lastEditDateTime, String category, boolean isActive, int notificationInterval) {
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
    }

    public void savePersistently() {
        InternalCountdownStorageMgr storageMgr = new InternalCountdownStorageMgr(this.getContext());
        storageMgr.setSaveCountdown(this,true);
    }

    public double getRemainingPercentage(int anzahlNachkomma, boolean getRemainingOtherwisePassedPercentage) { //min is 1, if 0 then it will be still min 1 nachkommastelle (but always 0!) because of double format itself
        try {
            Double all100percentSeconds = Long.valueOf((getDateTime(getUntilDateTime()).getTimeInMillis() - getDateTime(getStartDateTime()).getTimeInMillis()) / 1000).doubleValue();
            Double leftXpercentSeconds = Long.valueOf((getDateTime(getUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000).doubleValue();

            StringBuilder nachkommaStellen = new StringBuilder();
            for (int i = 0; i < anzahlNachkomma; i++) {
                nachkommaStellen.append("0");
            }
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
            df.setMaximumFractionDigits(2); //min might be 0 (nachkommastellen)

            double percentageValueUnformatted ;
            if (getRemainingOtherwisePassedPercentage) {
                percentageValueUnformatted = (leftXpercentSeconds / all100percentSeconds) * 100;
            } else {
                percentageValueUnformatted = 100-((leftXpercentSeconds / all100percentSeconds) * 100); //get passed percentage if false
            }
            double result = df.parse(df.format(percentageValueUnformatted)).doubleValue(); //formatting percentage to 2 nachkommastellen
            //Double result = Double.parseDouble((new DecimalFormat("##,"+nachkommaStellen)).format((leftXpercentSeconds / all100percentSeconds) * 100)); //formatting percentage to 2 nachkommastellen
            return (result >= 0) ? ((result <= 100) ? result : 100) : 0; //always return 0-100
        } catch (NullPointerException | NumberFormatException | ParseException e) {
            Log.e(TAG, "getRemainingPercentage: Could not calculate remaining percentage.");
            e.printStackTrace();
        }
        return (-1D); //to show error
    }

    public String getTotalSecondsNoScientificNotation() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(','); //set separator
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
        Double totalSeconds = 0D;
        try {
            if (getDateTime(getStartDateTime()).compareTo(getCurrentDateTime()) > 0) {
                //date is in the future
                Toast.makeText(this.getContext(),"Countdown starts at: "+this.getStartDateTime(),Toast.LENGTH_SHORT).show();
                totalSeconds = 0D; //prevent from counting to infinity (because negative)
            } else {
                //date is in the past and countdown started already
                totalSeconds = Long.valueOf((getDateTime(getUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000).doubleValue();
            }
        } catch (NullPointerException e) {
            Log.e("getTotalSeconds","totalSeconds could not be calculated. Nullpointerexception!");
            e.printStackTrace();
        }
        if (totalSeconds < 0) {
            Toast.makeText(this.getContext(),"Countdown has already expired on: "+this.getUntilDateTime(),Toast.LENGTH_SHORT).show();
            totalSeconds = 0D; //prevent from counting to infinity (because negative)
        }

        return totalSeconds;
    }


    // DATE FUNCTIONS --------------------------------------------------------------------
    private String getCurrentDateTimeStr() {
        return getDateTime(new GregorianCalendar());
    }
    private GregorianCalendar getCurrentDateTime() { return new GregorianCalendar(); }

    private String getDateTime(int day, int month, int year, int hour, int minute, int second) {
        return getDateTime(new GregorianCalendar(year,month,day,hour,minute,second));
    }

    //also needed in ModifyCountdownActivity
    public GregorianCalendar getDateTime(String formattedDate) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.GERMAN);
        GregorianCalendar result;
        try {
            Date date = df.parse(formattedDate);
            result = new GregorianCalendar();
            result.setTime(date);
        } catch (ParseException e) {
            Log.e("getDateTime","Could not parse Stringdate to GregorianCalender Date.");
            e.printStackTrace();
            return null; //abort further execution of method
        }
        return result;
    }

    private String getDateTime(GregorianCalendar c) { //IMPORTANT: Separation of dot must be the same for all attributes
        return c.get(Calendar.DAY_OF_MONTH)+"."+c.get(Calendar.MONTH)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND);
    }


    // ESCAPE METHODS FOR SHARED PREFERENCES ------------------------------------
    private String escapeForSharedPreferences(@NonNull String string) {
        if (string.contains(";")) {
            string = string.replace(";",",");
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

    public void setCountdownTitle(String countdownTitle) {
        this.countdownTitle = escapeForSharedPreferences(countdownTitle);
    }

    public String getCountdownDescription() {
        return countdownDescription;
    }

    public void setCountdownDescription(String countdownDescription) {
        this.countdownDescription = escapeForSharedPreferences(countdownDescription);
    }

    public String getUntilDateTime() {
        return untilDateTime;
    }

    public void setUntilDateTime(String untilDateTime) {
        this.untilDateTime = escapeForSharedPreferences(untilDateTime);
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = escapeForSharedPreferences(createdDateTime);
    }

    public String getLastEditDateTime() {
        return lastEditDateTime;
    }

    public void setLastEditDateTime(String lastEditDateTime) {
        this.lastEditDateTime = escapeForSharedPreferences(lastEditDateTime);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) { //#000000
        this.category = escapeForSharedPreferences(category);
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
        this.startDateTime = escapeForSharedPreferences(startDateTime);
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
    //set handler not required because directly assigned on top
}
