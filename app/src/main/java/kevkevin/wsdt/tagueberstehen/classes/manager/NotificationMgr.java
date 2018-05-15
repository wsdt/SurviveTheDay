package kevkevin.wsdt.tagueberstehen.classes.manager;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.entities.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CountdownCounter;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.services.NotificationBroadcastMgr;

import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_InAppPurchaseMgr.*;
import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_NotificationMgr.*;

public class NotificationMgr { //one instance for every countdown or similar
    private Context activityThisTarget;
    private int mNotificationId = 0; //start with 0 should be first notification (index starts at 0)
    private SparseArray<NotificationCompat.Builder> notifications = new SparseArray<>(); //NOT static, because every instance should have own Arraylist!
    private NotificationManager mNotifyMgr;
    private static final String TAG = "NotificationMgr";
    private Class targetActivityClass;
    private Resources res;

    public NotificationMgr(@NonNull Context activityThisTarget, Class targetActivityClass, NotificationManager mNotifyMgr) { //(NotifyManager) getSystemService(Notification_Service);
        this.setActivityThisTarget(activityThisTarget);
        this.setmNotifyMgr(mNotifyMgr);
        this.setTargetActivityClass(targetActivityClass);
        this.setRes(activityThisTarget.getResources());

        //Create NotificationChannels for Oreo
        createNotificationChannel(NOTIFICATION_CHANNEL_LIVECOUNTDOWN_ID,NOTIFICATION_CHANNEL_LIVECOUNTDOWN_NAME,3);
        createNotificationChannel(NOTIFICATION_CHANNEL_DEFAULT_ID,NOTIFICATION_CHANNEL_DEFAULT_NAME,3);
        createNotificationChannel(NOTIFICATION_CHANNEL_MOTIVATION_ID,NOTIFICATION_CHANNEL_MOTIVATION_NAME,3);

        //With countdown ID we are able to look in our persistent storage for the right countdown
        // IMPORTANT: Pending intent in create countdown so always correct one opened
        Log.d(TAG,"mNotificationId: "+getmNotificationId());
    }

    public void scheduleAllActiveCountdownNotifications() {
        Log.d(TAG, "scheduleAllActiveCountdownNotifications: Started method.");
        List<Countdown> allCountdowns = Countdown.queryMotivationOn(this.getActivityThisTarget());

        int count = 0;
        //do not call purchaseWorkflow or similar when only providing context [altough we mostly give an activity to this class we should not risk it]
        InAppPurchaseMgr inAppPurchaseMgr = new InAppPurchaseMgr(this.getActivityThisTarget());
        for (int i = 0;i<allCountdowns.size();i++) {
            final Countdown currCountdown = allCountdowns.get(i); //necessary because i cannot be final (i++)
            if ((count++) > 0) {
                inAppPurchaseMgr.executeIfProductIsBought(INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "scheduleAllActiveCountdownNotifications:success_is_true: Product is bought. Scheduling countdown.");
                        scheduleNotification(currCountdown.getCouId().intValue(), (long) currCountdown.getCouMotivationIntervalSeconds());
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "scheduleAllActiveCountdownNotifications:failure_is_false: Product not bought. Not scheduling countdown.");
                    }
                });
            } else {
                Log.d(TAG, "scheduleAllActiveCountdownNotifications: Scheduling first countdown (always free).");
                scheduleNotification(currCountdown.getCouId().intValue(), (long) currCountdown.getCouMotivationIntervalSeconds());
            }
        }
        Log.d(TAG, "scheduleAllActiveCountdownNotifications: Ended method.");
    }

    //broadcast receiver setting
    private void scheduleNotification(int countdownId, Long intervalInSeconds) {
        //Important: inexactRepeating to save battery!
        AlarmManager alarmManager = (AlarmManager) this.getActivityThisTarget().getSystemService(Context.ALARM_SERVICE);
        Intent tmpIntent = new Intent(this.getActivityThisTarget(), NotificationBroadcastMgr.class);
        tmpIntent.putExtra(IDENTIFIER_COUNTDOWN_ID,countdownId);

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
            this.getmNotifyMgr().notify(mNotificationId, this.getNotifications().get(mNotificationId).build());
        } catch(IndexOutOfBoundsException | NullPointerException e) {
            Log.e(TAG, "issueNotification: NotificationMgr not defined! NotificationMgr-No.: "+mNotificationId);
            e.printStackTrace();
        }
    }

    /** @param importance: Has to be supplied as normal int and not NotificationManager.IMPORTANCE_BLA because too low api level, just supply the int behind that constant! */
    private void createNotificationChannel(@NonNull String channelId, @NonNull String channelName, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.getmNotifyMgr().createNotificationChannel(new NotificationChannel(
                    channelId,
                    channelName,
                    importance));
        }
    }

    public Notification createCounterServiceNotification(Countdown countdown) {
        Intent tmpIntent = new Intent(this.getActivityThisTarget(), getTargetActivityClass());
        tmpIntent.putExtra(IDENTIFIER_COUNTDOWN_ID,countdown.getCouId()); //countdown to open
        //make this locally because of the same reason why pending intent has no getter
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                countdown.getCouId().intValue(),
                tmpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //because one notification for countdown
        //return (Constants.COUNTDOWNCOUNTERSERVICE.NOTIFICATION_ID+countdown.getCouId()); //e.g. 100 (high enough for collision avoidance) + countdownId (0,1,2,...) = 100,101,102 so easy to modify afterwards
        //not deprecated one requires api 16 (min is 15)
        boolean onGoing;
        String contentText;
        String shareText;

        if (!countdown.isUntilDateInTheFuture()) {
            //Remove countdown if it has expired (this method will never be called again for that countdown!
            Log.d(TAG, "createCounterServiceNotification: Countdown has expired! Making notification removable and making small changes.");
            onGoing = false; //notification is now removeable
            contentText = String.format(getRes().getString(R.string.customNotification_countdownCounter_expired),countdown.getCouUntilDateTime());
            shareText = getRes().getString(R.string.share_livecountdown_text_expired);
        } else { //Countdown has not expired
            onGoing = true; //not removeable
            String countdownTmpStr = CountdownCounter.craftBigCountdownString(countdown.getTotalSeconds(this.getActivityThisTarget()).longValue());
            contentText = String.format(getRes().getString(R.string.customNotification_countdownCounter_running),countdownTmpStr);
            shareText = String.format(getRes().getString(R.string.share_livecountdown_text_running),countdown.getCouTitle(),countdownTmpStr);
        }

        //Also liveCountdown should be shareable
        PendingIntent sharePendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                countdown.getCouId().intValue(), //use same request code as in other pendingintent above (really important!, otherwise not correct pendingintent used)
                ShareMgr.getSimpleShareIntent(null,countdown.getCouTitle(),shareText+" "+getRes().getString(R.string.share_postfix_appreference)),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this.getActivityThisTarget(),NOTIFICATION_CHANNEL_LIVECOUNTDOWN_ID)
                .setSmallIcon(R.drawable.light_notification_appicon)
                //Large icon is too small on new smartphones
                //.setLargeIcon(BitmapFactory.decodeResource(this.getRes(),R.drawable.notification_timebased_color))
                .setContentTitle(countdown.getCouTitle())
                .setAutoCancel(false) //remove after clicking on it
                .setContentIntent(pendingIntent)
                .setOngoing(onGoing) //notification is NOT REMOVEABLE
                .addAction(R.drawable.colorgrey555_share,getRes().getString(R.string.actionBar_countdownActivity_menu_shareCountdown_title),sharePendingIntent)
                //maybe no big text here because maybe hard to make notification small again (refresh etc.): .setStyle(new NotificationCompat.BigTextStyle().bigText("Remaining: "+countdown.getBigCountdownCurrentValues_String()+"\nTotal Years: 0\nTotal Months: 0\nTotal Weeks: 0\nTotal Days: 0\nTotal Hours: 0\nTotal Minutes: 0\nTotal Seconds: 0")) //make notification extendable
                .setContentText(contentText)
                .build();
    }

    //Public so we can create also other notifications (if not needed we can place a dummy countdown)
    public int createNotCountdownRelatedNotification(String title, String text, String bigtext, int icon) {
        //Since broadcast this does not really increment but uses a random number, so a new instance of this class would not overwrite old notifications!
        incrementmNotificationId(); //because of new notification (but return old No. because index starts at 0!

        //create result intent
        //IMPORTANT: Make no GETTER for this method, because this class is used for multiple countdowns!! So only the last assignment would open countdown
        //Create pending intent
        Intent tmpIntent = new Intent(this.getActivityThisTarget(), getTargetActivityClass());
        tmpIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY); //prevent activity to be added to history (preventing several back procedures) [also set in manifest]
        //make this locally because of the same reason why pending intent has no getter
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                this.getmNotificationId(),// instead of notificationId this was set (Problem: Always last intent was used): countdown.getCouId(),
                tmpIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //add notification
        this.getNotifications().put(this.getmNotificationId(), //save with current id
                new NotificationCompat.Builder(this.getActivityThisTarget(),NOTIFICATION_CHANNEL_DEFAULT_ID)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        //onMs = how long on / offMs = how long off (repeating, so blinking!)
                        //USE category color
                        .setLights(Color.parseColor("#ff0000"), NOTIFICATION_BLINK_ON_TIME_IN_MS, NOTIFICATION_BLINK_OFF_TIME_IN_MS)
                        .setAutoCancel(false) //remove NOT after clicking on it
                        .setOngoing(false) //removeable!! --> so you have to click on notification to remove it
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(bigtext)) //make notification extendable
                        .setContentText(text));

        return this.getmNotificationId(); //because randomly generated
        //return (mNotificationId-1); //saved under that id-1 because incremented
    }


    private int createNotification(Countdown countdown, String title, String text, int icon) {
        //Since broadcast this does not really increment but uses a random number, so a new instance of this class would not overwrite old notifications!
        incrementmNotificationId(); //because of new notification (but return old No. because index starts at 0!

        //create result intent
        //IMPORTANT: Make no GETTER for this method, because this class is used for multiple countdowns!! So only the last assignment would open countdown
        //Create pending intent
        Intent tmpIntent = new Intent(this.getActivityThisTarget(), getTargetActivityClass());
        tmpIntent.putExtra(IDENTIFIER_COUNTDOWN_ID,countdown.getCouId()); //countdown to open

        //Following attributes are added to call them in countdownActivity and showing in-app-notification again.
        tmpIntent.putExtra(IDENTIFIER_CONTENT_TITLE, title);
        tmpIntent.putExtra(IDENTIFIER_CONTENT_TEXT, text);
        tmpIntent.putExtra(IDENTIFIER_SMALL_ICON, icon);

        tmpIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_HISTORY); //prevent activity to be added to history (preventing several back procedures) [also set in manifest]
        //make this locally because of the same reason why pending intent has no getter
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                this.getmNotificationId(),// instead of notificationId this was set (Problem: Always last intent was used): countdown.getCouId(),
                tmpIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //For addAction (shareBtn)
        PendingIntent sharePendingIntent = PendingIntent.getActivity(
                this.getActivityThisTarget(),
                this.getmNotificationId(),
                ShareMgr.getSimpleShareIntent(null,title,text+" "+getRes().getString(R.string.share_postfix_appreference)),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //add notification
        this.getNotifications().put(this.getmNotificationId(), //save with current id
                new NotificationCompat.Builder(this.getActivityThisTarget(),NOTIFICATION_CHANNEL_MOTIVATION_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                //onMs = how long on / offMs = how long off (repeating, so blinking!)
                //USE category color
                .setLights(Color.parseColor(countdown.getCouCategoryColor()), NOTIFICATION_BLINK_ON_TIME_IN_MS, NOTIFICATION_BLINK_OFF_TIME_IN_MS)
                .setTicker(this.getRes().getString(R.string.customNotification_notificationTicker))
                .setAutoCancel(false) //remove NOT after clicking on it (realizing with button instead [action below])
                .setOngoing(false) //notification IS REMOVABLE
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)) //make notification extendable
                .addAction(R.drawable.colorgrey555_share,this.getActivityThisTarget().getString(R.string.actionBar_countdownActivity_menu_shareCountdown_title),sharePendingIntent)
                .setContentText(text));

        Log.d(TAG, "createNotfiication: Tried to set notification color: "+countdown.getCouCategoryColor());

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
        switch (HelperClass.getRandomInt(0,1)) { //random.nextInt(2-0)+0
            case 0: //GENERIC
                Log.d(TAG, "createRandomNotification: Chose random notification from GENERIC.");
                randomNotification = createRandomNotification_GENERIC(countdown);
                break;
            case 1: //TIMEBASED
                Log.d(TAG, "createRandomNotification: Chose random notification from TIMEBASED.");
                randomNotification = createRandomNotification_TIMEBASED(countdown);
                break;
            default:
                Log.e(TAG, "createRandomNotification: Could not determine random notification type!");
                return createNotification(countdown,this.getRes().getString(R.string.error_title_systemError), this.getRes().getString(R.string.error_contactAdministrator), R.drawable.light_notification_warning);
        }
        //Extract values from innerclass instance and create NotificationMgr in next method
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

    //NotificationIcons should be white acc. to internet and android studio
    private NotificationContent createRandomNotification_GENERIC(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)
        randomNotification.titleList.addAll(Arrays.asList(this.getRes().getStringArray(R.array.customNotification_random_generic_titles))); //converts array to list and adds all of them
        //NOT NECESSARY: we get random quote now directly -> randomNotification.textList = (countdown.getQuotesLanguagePacks_Quotes()); //add here all countdown selected languages for quotes
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.light_notification_generic_saying,R.drawable.light_notification_generic_sayingloud,R.drawable.light_notification_generic_quote));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(HelperClass.getRandomInt(0,randomNotification.titleList.size()-1)); //size() index does exist!
        randomNotification.text = countdown.getRandomQuoteSuitableForCountdown(this.getActivityThisTarget());//randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()));
        randomNotification.icon = randomNotification.iconList.get(HelperClass.getRandomInt(0,randomNotification.iconList.size()-1));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_TIMEBASED(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList(this.getRes().getStringArray(R.array.customNotification_random_timebased_titles))); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_0_secondsToGo),countdown.getCouTitle(),countdown.getTotalSecondsNoScientificNotation(this.getActivityThisTarget())),
                String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_1_percentageLeft),countdown.getCouTitle(),HelperClass.formatCommaNumber(countdown.getRemainingPercentage(true),2)),
                String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_2_countdownEndsOn),countdown.getCouTitle(),countdown.getCouUntilDateTime()),
                String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_3_percentageAchieved),countdown.getCouTitle(),HelperClass.formatCommaNumber(countdown.getRemainingPercentage(false),2)),
                String.format(this.getRes().getString(R.string.customNotification_random_timebased_text_4_notificationInterval),countdown.getCouTitle(),(this.getRes().getStringArray(R.array.countdownIntervalSpinner_LABELS)[(Arrays.asList(this.getRes().getStringArray(R.array.countdownIntervalSpinner_VALUES)).indexOf(""+countdown.getCouMotivationIntervalSeconds()))])))); //get label of corresponding seconds of strings.xml
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.light_notification_timebased_clock,R.drawable.light_notification_timebased_clockalert));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(HelperClass.getRandomInt(0,randomNotification.titleList.size()-1)); // size() index does  exist!
        randomNotification.text = randomNotification.textList.get(HelperClass.getRandomInt(0,randomNotification.textList.size()-1));
        randomNotification.icon = randomNotification.iconList.get(HelperClass.getRandomInt(0,randomNotification.iconList.size()-1));

        return randomNotification;
    }

    // PRIVATE METHODS FOR CREATERANDOMNOTIFICATION(COUNTDOWN countdown) {} - END ####################################################

    public void removeNotification(int mNotificationId) {
        this.getmNotifyMgr().cancel(mNotificationId);
        Log.d(TAG, "removeNotification: Tried to remove Notification with id: "+mNotificationId);
    }

    public void removeNotifications(ArrayList<Integer> mNotificationIds) {
        for (int mNotificationId : mNotificationIds) {
            removeNotification(mNotificationId);
        }
        Log.d(TAG, "removeNotifications: Tried to remove all given notifications.");
    }


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

    /** Creates random notification id (preserved ids for motivation / countdowncounter [live countdown] has other reserved ids)
     * Randomness necessary so many customnotification-instances can create notifications without overwriting each other's notifications.*/
    public void incrementmNotificationId() {
        int tmpId = (this.getmNotificationId()+1); //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]
        tmpId += HelperClass.getRandomInt(0,NOTIFICATION_ID-tmpId); //-tmpId, so we cannot get over the bound of the constant!
        Log.d(TAG,"incrementNoficiationId: Old: "+this.getmNotificationId()+"/ New: "+tmpId);
        this.mNotificationId = tmpId; //small probability that notification has the same id! So multiple instances of this class usable without overwriten old notifications
    }

    public NotificationManager getmNotifyMgr() {
        return mNotifyMgr;
    }

    public void setmNotifyMgr(NotificationManager mNotifyMgr) {
        this.mNotifyMgr = mNotifyMgr;
    }

    public SparseArray<NotificationCompat.Builder> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(SparseArray<NotificationCompat.Builder> notifications) {
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
