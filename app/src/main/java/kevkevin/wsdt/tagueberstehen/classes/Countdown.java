package kevkevin.wsdt.tagueberstehen.classes;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.support.annotation.NonNull;
import android.util.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.R;


public class Countdown {
    private int countdownId;
    private String countdownDescription;
    private String untilDateTime;
    private String category;
    private Context context;
    private Notification notificationBuilder;
    private static SharedPreferences allCountdowns;
    private static NotificationManager notificationManager;
    private static HashMap<String,ArrayList<Integer>> notificationsSeparatedByCategory; //associative list: notificationsSeparatedByCategory.get("WORK") contains an ArrayList<Integer> of schedulable Notifications (IDs of them are stored there)

    public Countdown() {
        Log.w("SchedulePersistCountd.","Used default constructor of Countdown! Do not do this because this can cause NullpointerExceptions! \nThis method should only be used by the broadcast receiver.");
    }


    public Countdown(Context context, SharedPreferences allCountdowns, NotificationManager notificationManager, int countdownId, String countdownDescription, String untilDateTime, String category) {
        //IMPORTANT: notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE) !
        if (Countdown.getAllCountdowns() == null) {
            Countdown.setAllCountdowns(allCountdowns);
            Log.d("SchedPersCountdown","Sharedpreferences initialized.");
        }
        if (Countdown.getNotificationManager() == null) {
            Countdown.setNotificationManager(notificationManager);
        }


        if (!doesCountdownAlreadyExist(countdownId)) {
            this.setContext(context);
            this.setCountdownId(countdownId);
            this.setCountdownDescription(countdownDescription);
            this.setUntilDateTime(untilDateTime);
            this.setCategory(category);
        } else {
            Log.w("Countdown","Countdown does already exist and given values got ignored, Countdown-ID: "+countdownId);
        }
    }

    private boolean doesCountdownAlreadyExist(int countdownId) {
        boolean doesExist = false;

        //iterate through all countdown data sets
        Map<String,?> savedCountdowns = getAllCountdowns().getAll();
        for (Map.Entry<String,?> entry : savedCountdowns.entrySet()) {
            if (entry.getKey().equals("COUNTDOWN_"+countdownId)) {
                doesExist = true;
            }
        }
        return doesExist;
    }


    public HashMap<String, ArrayList<Integer>> defineNotificationList() {
        /*
        IMPORTANT: This function needs only to be executed ONCE, after that for related Countdowns there can be the correct category extracted and then applied to that countdown.
        ALSO DO NOT call any this Getters here, because you would save there values of this instance and maybe not of the countdown it is supposed to! */

        setNotificationsSeparatedByCategory(new HashMap<String, ArrayList<Integer>>());
        this.setNotificationBuilder(new Notification(this.getContext(),CountdownActivity.class, Countdown.getNotificationManager(),this.getCountdownId()));

        // CATEGORY: WORK ########################################################
        ArrayList<Integer> work = new ArrayList<>();
        work.add(getNotificationBuilder().createNotification("Test1","NId: 1 - Category: Work", R.drawable.campfire_black));
        //TODO: add here more notifications
        getNotificationsSeparatedByCategory().put("WORK",work);
        work = null;


        // CATEGORY: UNIVERSITY ###################################################
        ArrayList<Integer> university = new ArrayList<>();
        university.add(getNotificationBuilder().createNotification("Test2"," - NId: 2 - Cat.: University",R.drawable.campfire_red));
        //TODO: add here more notifications
        getNotificationsSeparatedByCategory().put("UNIVERSITY",university);
        university = null;

        // CATEGORY: SCHOOL #######################################################
        ArrayList<Integer> school = new ArrayList<>();
        school.add(getNotificationBuilder().createNotification("Test3","NId: 3 - Category: School",R.drawable.campfire_white));
        //TODO: add here more notifications
        getNotificationsSeparatedByCategory().put("SCHOOL",school);
        school = null;

        return getNotificationsSeparatedByCategory();
    }

    public void saveCountdown() {
        SharedPreferences.Editor editor = getAllCountdowns().edit();
        // ; will be the splitter! So there is no ; in the strings allowed! This will be escaped to , automatically by escapeForSharedPreferences()
        editor.putString("COUNTDOWN_"+this.getCountdownId(),this.getCountdownId()+";"+this.getCountdownDescription()+";"+this.getCurrentDateTime()+";"+this.getUntilDateTime()+";"+this.getCategory()); //Current Timestamp with date: DD.MM.YYYY hh:mm:ss
        editor.apply();
    }

    private static String getCountdownString(int countdownId) { //e.g. COUNTDOWN_1 ...
        if (getAllCountdowns() == null) {
            Log.e("getCountdownString","Could not load Countdown. Never made an Instance of Countdown before. Please set Sharedpreferences.");
            return null;
        }
        return getAllCountdowns().getString("COUNTDOWN_"+countdownId,"empty;empty;empty;empty;empty"); //same format as in saveCountdown()
    }

    public static Countdown getCountdownObj(int countdownId) {
        if (getAllCountdowns() == null) {
            Log.e("getCountdownObj","Could not load Countdown. Never made an Instance of Countdown before. Please set Sharedpreferences.");
        } else {
            try {
                String[] countdown = getCountdownString(countdownId).split(";");
                return new Countdown(null,null,null,Integer.parseInt(countdown[0]),countdown[1],countdown[2],countdown[3]);
            } catch (Exception e) {
                Log.e("getCountdownObj","Exception while parsing Countdown to Object.");
                e.printStackTrace();
            }
        }
        return null;
    }

    public Long getTotalSeconds() {
        Long totalSeconds = 0L;
        try {
            totalSeconds = (getFinishDateTime(getUntilDateTime()).getTimeInMillis() - getCurrentDateTime().getTimeInMillis()) / 1000;
        } catch (NullPointerException e) {
            Log.e("getTotalSeconds","totalSeconds could not be calculated. Nullpointerexception!");
            e.printStackTrace();
        }
        return totalSeconds;
    }



    private String getCurrentDateTimeStr() {
        return formatGregorianDate(new GregorianCalendar());
    }
    private GregorianCalendar getCurrentDateTime() { return new GregorianCalendar(); }

    private String getFinishDateTime(int day, int month, int year, int hour, int minute, int second) {
        return formatGregorianDate(new GregorianCalendar(year,month,day,hour,minute,second));
    }

    private GregorianCalendar getFinishDateTime(String formattedDate) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.GERMAN);
        GregorianCalendar result;
        try {
            Date date = df.parse(formattedDate);
            result = new GregorianCalendar();
            result.setTime(date);
        } catch (ParseException e) {
            Log.e("getFinishDateTime","Could not parse Stringdate to GregorianCalender Date.");
            e.printStackTrace();
            return null; //abort further execution of method
        }
        return result;
    }

    private String formatGregorianDate(GregorianCalendar c) { //IMPORTANT: Separation of dot must be the same for all attributes
        return c.get(Calendar.DAY_OF_MONTH)+"."+c.get(Calendar.MONTH)+"."+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND);
    }

    // GETTER / SETTER ++++++++++++++++++++++++++++++++++++++++++++
    public int getCountdownId() {
        return this.countdownId;
    }

    public void setCountdownId(int countdownId) {
        this.countdownId = countdownId;
    }

    public String getUntilDateTime() {
        return this.untilDateTime;
    }

    public void setUntilDateTime(String untilDateTime) {
        if (untilDateTime.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}")) { //Format: DD.MM.YYYY hh:mm:ss
            this.untilDateTime = escapeForSharedPreferences(untilDateTime);
        } else {
            try {
                throw new FormatException("setUntilDateTime: Date/Time does not match with Regex: "+untilDateTime);
            } catch (FormatException e) {
                Log.e("setUntilDateTime","Date/Time does not match with Regex: "+untilDateTime);
                e.printStackTrace();
            }
        }
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = escapeForSharedPreferences(category);
    }


    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public static void setNotificationManager(NotificationManager notificationManager) {
        Countdown.notificationManager = notificationManager;
    }

    public static SharedPreferences getAllCountdowns() {
        return allCountdowns;
    }

    public static void setAllCountdowns(SharedPreferences allCountdowns) {
        Countdown.allCountdowns = allCountdowns;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public Notification getNotificationBuilder() {
        return notificationBuilder;
    }

    public void setNotificationBuilder(Notification notificationBuilder) {
        this.notificationBuilder = notificationBuilder;
    }

    public static HashMap<String, ArrayList<Integer>> getNotificationsSeparatedByCategory() {
        return notificationsSeparatedByCategory;
    }

    public static void setNotificationsSeparatedByCategory(HashMap<String, ArrayList<Integer>> notificationsSeparatedByCategory) {
        Countdown.notificationsSeparatedByCategory = notificationsSeparatedByCategory;
    }

    public String getCountdownDescription() {
        return countdownDescription;
    }

    public void setCountdownDescription(String countdownDescription) {
        this.countdownDescription = escapeForSharedPreferences(countdownDescription); //so no ; allowed in String
    }

    // ESCAPE METHODS FOR SHARED PREFERENCES ------------------------------------
    private String escapeForSharedPreferences(@NonNull String string) {
        if (string.contains(";")) {
            string = string.replace(";",",");
        }
        return string;
    }

}
