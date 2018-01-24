package kevkevin.wsdt.tagueberstehen.classes;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;
import kevkevin.wsdt.tagueberstehen.classes.services.NotificationService_AlarmmanagerBroadcastReceiver;

public class CustomNotification { //one instance for every countdown or similar
    private Context activityThisTarget;
    private int mNotificationId = 0; //start with 0 should be first notification (index starts at 0)
    private HashMap<Long, NotificationCompat.Builder> notifications = new HashMap<>(); //NOT static, because every instance should have own Arraylist!
    private NotificationManager mNotifyMgr;
    private static final String TAG = "CustomNotification";
    private Random random;
    private Class targetActivityClass;
    private Resources res;

    public CustomNotification(@NonNull Context activityThisTarget, Class targetActivityClass, NotificationManager mNotifyMgr) { //(NotifyManager) getSystemService(Notification_Service);
        this.setActivityThisTarget(activityThisTarget);
        this.setmNotifyMgr(mNotifyMgr);
        this.random =  new Random();
        this.setTargetActivityClass(targetActivityClass);
        this.setRes(activityThisTarget.getResources());

        //With countdown ID we are able to look in our persistent storage for the right countdown
        // IMPORTANT: Pending intent in create countdown so always correct one opened
        Log.d(TAG,"mNotificationId: "+getmNotificationId());
    }

    public HashMap<Integer, Countdown> scheduleAllActiveCountdownNotifications(Context context) {
        Log.d(TAG, "scheduleAllActiveCountdownNotifications: Started method.");
        HashMap<Integer, Countdown> allCountdowns = (new InternalCountdownStorageMgr(context)).getAllCountdowns(true);
        for (Map.Entry<Integer, Countdown> countdown : allCountdowns.entrySet()) {
            scheduleNotification(countdown.getValue().getCountdownId(), (long) countdown.getValue().getNotificationInterval());
        }
        Log.d(TAG, "scheduleAllActiveCountdownNotifications: Ended method.");
        return allCountdowns;
    }

    //Only for broadcast receiver mode! When background service used, this function SHOULDNT BE CALLED
    private void scheduleNotification(int countdownId, Long intervalInSeconds) {
        //Important: inexactRepeating to save battery!
        AlarmManager alarmManager = (AlarmManager) this.getActivityThisTarget().getSystemService(Context.ALARM_SERVICE);
        Intent tmpIntent = new Intent(this.getActivityThisTarget(), NotificationService_AlarmmanagerBroadcastReceiver.class);
        tmpIntent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID,countdownId);

        //PendingIntent ID = Countdown ID (important so reload overwrites old one! AND we can show different notifications because different pendingIntent IDs!!
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getActivityThisTarget(), countdownId, tmpIntent,0);
        if (alarmManager != null) {
            //save Battery is a global setting (default false)
            if ((new GlobalAppSettingsMgr(this.getActivityThisTarget()).saveBattery())) {
                Log.d(TAG, "scheduleNotification: Tried to save battery with setInexactRepeating. Interval: "+(intervalInSeconds)+" Seconds");
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, (new GregorianCalendar()).getTimeInMillis(), intervalInSeconds * 1000L, alarmIntent);
            } else {
                Log.d(TAG, "scheduleNotification: setExactRepeating might cause battery drain! Interval: "+(intervalInSeconds)+" Seconds");
                //More precise but more battery used!
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, (new GregorianCalendar()).getTimeInMillis(), intervalInSeconds * 1000L, alarmIntent);
            }
        } else {
            Log.e(TAG, "scheduleNotification: Alarmmanager is null!");
        }
    }

    public void issueNotification(int mNotificationId) {
        try {
            this.getmNotifyMgr().notify(mNotificationId, this.getNotifications().get((long) mNotificationId).build());
        } catch(IndexOutOfBoundsException | NullPointerException e) {
            Log.e(TAG, "issueNotification: CustomNotification not defined! CustomNotification-No.: "+mNotificationId);
            e.printStackTrace();
        }
    }

    public int createIssueCounterServiceNotification(Context context, Countdown countdown) {
        //TODO: use this function for foreground service (live countdown)

        Intent tmpIntent = new Intent(this.getActivityThisTarget(), getTargetActivityClass());
        tmpIntent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID,countdown.getCountdownId()); //countdown to open
        //make this locally because of the same reason why pending intent has no getter
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                countdown.getCountdownId(),
                tmpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        /* Creates custom foreground, unremoveable notification for specific countdowns which might be counted down */
        this.getmNotifyMgr().notify(Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID+countdown.getCountdownId(), //constantsNotNummer + countdownid (1000+1, etc.)
                //not deprecated one requires api 16 (min is 15)
                new NotificationCompat.Builder(this.getActivityThisTarget())
                        .setSmallIcon(R.drawable.app_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getRes(),R.drawable.app_icon))
                        .setContentTitle(countdown.getCountdownTitle())
                        .setAutoCancel(false) //remove after clicking on it
                        .setContentIntent(pendingIntent)
                        .setOngoing(true) //notification is NOT REMOVEABLE
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("BIG: "+countdown.getCountdownDescription())) //make notification extendable
                        .setContentText("CT: "+countdown.getUntilDateTime())
                .build());

        //because one notification for countdown
        return (Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID+countdown.getCountdownId()); //e.g. 100 (high enough for collision avoidance) + countdownId (0,1,2,...) = 100,101,102 so easy to modify afterwards
    }

    private int createNotification(Countdown countdown, String title, String text, int icon) {
        //Since broadcast this does not really increment but uses a random number, so a new instance of this class would not overwrite old notifications!
        incrementmNotificationId(); //because of new notification (but return old No. because index starts at 0!

        //create result intent
        //IMPORTANT: Make no GETTER for this method, because this class is used for multiple countdowns!! So only the last assignment would open countdown
        //Create pending intent
        Intent tmpIntent = new Intent(this.getActivityThisTarget(), getTargetActivityClass());
        tmpIntent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID,countdown.getCountdownId()); //countdown to open

        //Following attributes are added to call them in countdownActivity and showing notification again.
        tmpIntent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_CONTENT_TITLE, title);
        tmpIntent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_CONTENT_TEXT, text);
        tmpIntent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_SMALL_ICON, icon);

        tmpIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY); //prevent activity to be added to history (preventing several back procedures) [also set in manifest]
        //make this locally because of the same reason why pending intent has no getter
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                this.getmNotificationId(),// instead of notificationId this was set (Problem: Always last intent was used): countdown.getCountdownId(),
                tmpIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //add notification
        this.getNotifications().put((long) this.getmNotificationId(), //save with current id
                new NotificationCompat.Builder(this.getActivityThisTarget())
                .setSmallIcon(icon)
                .setContentTitle(title)
                //onMs = how long on / offMs = how long off (repeating, so blinking!)
                //USE category color
                .setLights(Color.parseColor(countdown.getCategory()), Constants.CUSTOMNOTIFICATION.NOTIFICATION_BLINK_ON_TIME_IN_MS, Constants.CUSTOMNOTIFICATION.NOTIFICATION_BLINK_OFF_TIME_IN_MS)
                .setTicker(this.getRes().getString(R.string.customNotification_notificationTicker))
                .setAutoCancel(true) //remove after clicking on it
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)) //make notification extendable
                .setContentText(text));

        Log.d(TAG, "createNotfiication: Tried to set notification color: "+countdown.getCategory());

        return this.getmNotificationId(); //because randomly generated
        //return (mNotificationId-1); //saved under that id-1 because incremented
    }

    public int createRandomNotification(Countdown countdown) {
        //Creates a suitable random notification for a specific countdown
        NotificationContent randomNotification;

        /*different kinds of random notifications
        *       GENERIC (0)
        *       TIMEBASED (1)
        *       CATEGORYBASED (2) */

        //Decide which type of random notification (generic, time-based, ...)
        switch (this.random.nextInt(2)) { //random.nextInt(2-0)+0
            case 0: //GENERIC
                Log.d(TAG, "createRandomNotification: Chose random notification from GENERIC.");
                randomNotification = createRandomNotification_GENERIC(countdown);
                break;
            case 1: //TIMEBASED
                Log.d(TAG, "createRandomNotification: Chose random notification from TIMEBASED.");
                randomNotification = createRandomNotification_TIMEBASED(countdown);
                break;
            /*case 2: //CATEGORYBASED --> IMPORTANT: nextInt(1) means that case 2 can never happen, this is because the category-based one has currently no use!
                Log.d(TAG, "createRandomNotification: Chose random notification from CATEGORYBASED.");
                randomNotification = createRandomNotification_CATEGORYBASED(countdown);
                break;*/
            default:
                Log.e(TAG, "createRandomNotification: Could not determine random notification type!");
                return createNotification(countdown,this.getRes().getString(R.string.error_title_systemError), this.getRes().getString(R.string.error_contactAdministrator), R.drawable.warning);
        }
        //Extract values from innerclass instance and create CustomNotification in next method
        return createNotification(countdown, randomNotification.title,randomNotification.text,randomNotification.icon);
    }


    // PRIVATE METHODS FOR CREATERANDOMNOTIFICATION(COUNTDOWN countdown) {} - START ####################################################
    private class NotificationContent {
        //Used for bundling resources (easier to give back to createNotification()
        String title;
        String text;
        int icon;
        //Create arraylists and add texts HERE [because specific for generic etc.] (choose random from alle by index) (because of that each category needs a separate instance of that inner class)
        ArrayList<String> titleList = new ArrayList<>(); //not static because otherwise categories might get mixed up!!
        ArrayList<String> textList = new ArrayList<>();
        ArrayList<Integer> iconList = new ArrayList<>();
    }

    private NotificationContent createRandomNotification_GENERIC(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)
        randomNotification.titleList.addAll(Arrays.asList(this.getRes().getStringArray(R.array.customNotification_random_generic_titles))); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(this.getRes().getStringArray(R.array.customNotification_random_generic_texts)));
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.notification_generic_blue,R.drawable.notification_generic_green,R.drawable.notification_generic_purple,R.drawable.notification_generic_red));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size())); //size() index does exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_TIMEBASED(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList(this.getRes().getStringArray(R.array.customNotification_random_timebased_titles))); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_0_secondsToGo),countdown.getCountdownTitle(),countdown.getTotalSecondsNoScientificNotation()),
                String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_1_percentageLeft),countdown.getCountdownTitle(),countdown.getRemainingPercentage(2, true)),
                countdown.getCountdownTitle()+" - Countdown ends on "+countdown.getUntilDateTime(),
                countdown.getCountdownTitle()+" - Already "+countdown.getRemainingPercentage(2, false)+" % passed!",
                countdown.getCountdownTitle()+" - A motivating notification will be sent every "+(this.getRes().getStringArray(R.array.countdownIntervalSpinner_LABELS)[(Arrays.asList(this.getRes().getStringArray(R.array.countdownIntervalSpinner_VALUES)).indexOf(""+countdown.getNotificationInterval()))])+". :)")); //get label of corresponding seconds of strings.xml
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.notification_timebased_color,R.drawable.notification_timebased_white));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size())); // size() index does  exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_CATEGORYBASED(Countdown countdown) {
        //TODO: implemented, but currently no use for it
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList("")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(countdown.getCountdownTitle()+" - Cat.: "+countdown.getCategory(),countdown.getCountdownTitle()+" - Until: "+countdown.getUntilDateTime(),countdown.getCountdownTitle()+" - Created: "+countdown.getCreatedDateTime()));
        //randomNotification.iconList.addAll(Arrays.asList(R.drawable.campfire_black,R.drawable.campfire_red,R.drawable.campfire_white));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size()-1)); //-1 because size() index does not exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()-1));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()-1));

        return randomNotification;
    }

    // PRIVATE METHODS FOR CREATERANDOMNOTIFICATION(COUNTDOWN countdown) {} - END ####################################################


    //GETTER / SETTER +++++++++++++++++++++++++
    public Context getActivityThisTarget() {
        return activityThisTarget;
    }

    public void setActivityThisTarget(Context activityThisTarget) {
        this.activityThisTarget = activityThisTarget;
    }


    public int getmNotificationId() {
        return this.mNotificationId;
    }

    public void incrementmNotificationId() {
        int tmpId = (this.getmNotificationId()+1+this.random.nextInt(999999999));
        Log.d(TAG,"incrementNoficiationId: Old: "+this.getmNotificationId()+"/ New: "+tmpId);
        this.mNotificationId = tmpId; //small probability that notification has the same id! So multiple instances of this class usable without overwriten old notifications
    }

    public NotificationManager getmNotifyMgr() {
        return mNotifyMgr;
    }

    public void setmNotifyMgr(NotificationManager mNotifyMgr) {
        this.mNotifyMgr = mNotifyMgr;
    }

    public HashMap<Long, NotificationCompat.Builder> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(HashMap<Long, NotificationCompat.Builder> notifications) {
        this.notifications = notifications;
    }


    public Class getTargetActivityClass() {
        return targetActivityClass;
    }

    public void setTargetActivityClass(Class targetActivityClass) {
        this.targetActivityClass = targetActivityClass;
    }

    public Resources getRes() {
        return res;
    }

    public void setRes(Resources res) {
        this.res = res;
    }

}
