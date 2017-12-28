package kevkevin.wsdt.tagueberstehen.classes;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import kevkevin.wsdt.tagueberstehen.R;

public class Notification /*implements Parcelable*/ { //one instance for every countdown or similar
    private Context activityThisTarget;
    private Intent resultIntent;
    private int mNotificationId = 0; //start with 0 should be first notification (index starts at 0)
    private ArrayList<NotificationCompat.Builder> notifications = new ArrayList<>(); //NOT static, because every instance should have own Arraylist!
    private NotificationManager mNotifyMgr;
    private PendingIntent resultPendingIntent; //open countdown of current notifications
    private static final String TAG = "Notification";
    private Random random = new Random();

    public Notification (Context activityThisTarget,Class targetActivityClass, NotificationManager mNotifyMgr, int countdownId) { //(NotifyManager) getSystemService(Notification_Service);
        this.setActivityThisTarget(activityThisTarget);
        this.setmNotifyMgr(mNotifyMgr);

        //With countdown ID we are able to look in our persistent storage for the right countdown
        Intent tmp = new Intent(this.getActivityThisTarget(),targetActivityClass);
        tmp.putExtra("COUNTDOWN_ID",countdownId);
        this.setResultIntent(tmp);
        this.setResultPendingIntent(
                PendingIntent.getActivity(
                        this.getActivityThisTarget(),
                        0,
                        this.getResultIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        Log.d(TAG,"mNotificationId: "+getmNotificationId());
    }

    public void issueNotification(int mNotificationId) {
        try {
            this.getmNotifyMgr().notify(mNotificationId, this.getNotifications().get(mNotificationId).build());
        } catch(IndexOutOfBoundsException e) {
            Log.e(TAG, "issueNotification: Notification not defined! Notification-No.: "+mNotificationId);
            e.printStackTrace();
        }
    }

    public int createNotification(String title,String text, int icon) {
        this.getNotifications().add( //save with current id
                new NotificationCompat.Builder(this.getActivityThisTarget())
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentIntent(this.getResultPendingIntent())
                .setContentText(text));

        incrementmNotificationId(); //because of new notification (but return old No. because index starts at 0!
        return (mNotificationId-1); //saved under that id-1 because incremented
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
            case 2: //CATEGORYBASED
                Log.d(TAG, "createRandomNotification: Chose random notification from CATEGORYBASED.");
                randomNotification = createRandomNotification_CATEGORYBASED(countdown);
                break;
            default:
                Log.e(TAG, "createRandomNotification: Could not determine random notification type!");
                return createNotification("System Error", "Please contact administrator. ", R.drawable.warning);
        }
        //Extract values from innerclass instance and create Notification in next method
        return createNotification(randomNotification.title,randomNotification.text,randomNotification.icon);
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
        randomNotification.titleList.addAll(Arrays.asList("Keep going!","Almost done!","Do not give up!","Keep it up!")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(countdown.getCountdownTitle()+" - "+countdown.getCategory(),countdown.getCountdownTitle()+" - "+countdown.getUntilDateTime()));
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.campfire_black,R.drawable.campfire_red,R.drawable.campfire_white));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size()-1)); //-1 because size() index does not exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()-1));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()-1));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_TIMEBASED(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList("Just "+countdown.getTotalSeconds()+" seconds to go!","You get this!", "Easy man!")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(countdown.getCountdownTitle()+" - "+countdown.getCountdownDescription(),countdown.getCountdownTitle()+" - You get the half!",countdown.getCountdownTitle()+" - Only "+countdown.getTotalSeconds()+" s to go!"));
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.campfire_black,R.drawable.campfire_red,R.drawable.campfire_white));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size()-1)); //-1 because size() index does not exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()-1));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()-1));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_CATEGORYBASED(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList("Too easy man!", "YOu can do that!")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(countdown.getCountdownTitle()+" - Cat.: "+countdown.getCategory(),countdown.getCountdownTitle()+" - Until: "+countdown.getUntilDateTime(),countdown.getCountdownTitle()+" - Created: "+countdown.getCreatedDateTime()));
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.campfire_black,R.drawable.campfire_red,R.drawable.campfire_white));

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
        Log.d(TAG,"incrementNoficiationId: Old: "+this.getmNotificationId()+"/ Incremented: "+(this.getmNotificationId()+1));
        this.mNotificationId = (this.getmNotificationId()+1);
    }

    public NotificationManager getmNotifyMgr() {
        return mNotifyMgr;
    }

    public void setmNotifyMgr(NotificationManager mNotifyMgr) {
        this.mNotifyMgr = mNotifyMgr;
    }

    public ArrayList<NotificationCompat.Builder> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(ArrayList<NotificationCompat.Builder> notifications) {
        this.notifications = notifications;
    }

    public PendingIntent getResultPendingIntent() {
        return resultPendingIntent;
    }

    public void setResultPendingIntent(PendingIntent resultPendingIntent) {
        this.resultPendingIntent = resultPendingIntent;
    }

    public Intent getResultIntent() {
        return resultIntent;
    }

    public void setResultIntent(Intent resultIntent) {
        this.resultIntent = resultIntent;
    }

}
