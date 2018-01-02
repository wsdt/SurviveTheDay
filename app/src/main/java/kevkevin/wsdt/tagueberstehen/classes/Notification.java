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
                .setAutoCancel(true) //remove after clicking on it
                .setContentIntent(this.getResultPendingIntent())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)) //make notification extendable
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
            /*case 2: //CATEGORYBASED --> IMPORTANT: nextInt(1) means that case 2 can never happen, this is because the category-based one has currently no use!
                Log.d(TAG, "createRandomNotification: Chose random notification from CATEGORYBASED.");
                randomNotification = createRandomNotification_CATEGORYBASED(countdown);
                break;*/
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
        randomNotification.titleList.addAll(Arrays.asList("Keep going!","Do not give up!","Keep it up!", "You get that!", "That is nothing!", "Easy :)", "Never settle!")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList("The way get started is to quit talking and begin doing. (Walt Disney)",
                "The pessimist sees difficulty in every opportunity. The optimist sees opportunity in every difficulty. (Winston Churchill)",
                "Don't let yesterday take up too much of today. (Will Rogers)",
                "You learn more from failure than from success. Don't let it stop you. Failure build character.",
                "It's not whether you get knocked down, it's whether you get up. (Vince Lombardi)",
                "If you are working on something that you really care about, you don't have to be pushed. The vision pulls you. (Steve Jobs)",
                "People who are crazy enough to think they can change the world, are the ones who do. (Rob Siltanen)",
                "Failure will never overtake me if my determination to succeed is strong enough. (Og Mandino)",
                "Enterpreneurs are great at dealing with uncertainty and also very good at minimizing risk. That's the classic entrepreneur. (Mohnish Pabrai)",
                "We may encounter many defeats but we must not be defeated. (Maya Angelou)",
                "Knowing is not enough; We must apply. Wishing is not enough; We must do. (Johann Wolfgang von Goethe)",
                "Imagine your life is perfect in every respect; What would it look like? (Brian Tracy)",
                "We generate fears while we sit. We overcome them by action. (Dr. Henry Link)",
                "Whether you think you can or think you can't, you're right. (Henry Ford)",
                "Security is mostly a superstition. Life is either a daring adventure or nothing. (Helen Keller)",
                "The man who has confidence in himself gains the confidence of others. (Hasidic Proverb)",
                "The only limit to our realization of tomorrow will be our doubts of today. (Franklin D. Roosevelt)",
                "Creativity is intelligence having fun. (Albert Einstein)",
                "What you lack in talent can be made up with desire, hustle and giving 110 % all the time. (Don Zimmer)",
                "Do what you can with all you have, wherever you are. (Theodore Roosevelt)",
                "Develop an attitude of gratitude. Say thank you to everyone you meet for everything they do for you. (Brian Tracy)",
                "You are never too old to set another goal or to dream a new dream. (C.S. Lewis)",
                "To see what is right and not do it is a lack of courage. (Confucious)",
                "Reading is to the mind, as exercise to the body. (Brian Tracy)",
                "Fake it until you make it! Act as if you had all the confidence you require until it becomes your reality. (Brian Tracy)",
                "The future belong to the competent. Get good, get better, be the best! (Brian Tracy)",
                "For every reason it's not possible, there are hundreds of people who have faced the same circumstances and succeeded. (Jack Canfield)",
                "Things work out best for those who make the best of how things work out. (John Wooden)",
                "A room without books is like a body without a soul. (Marcus Tullius Cicero)",
                "I think goals should never be easy, they should force you to work, even if they are uncomfortable at the time. (Michael Phelps)",
                "One of the lessons that I grew up with was to always stay true to yourself and never let what somebody else days distracts you from your goals. (Michelle Obama)",
                "Today's accomplishments were yesterday's impossibilities. (Robert H. Schuller)",
                "The only way to do great work is to love what you do. If you haven't found it yet, keep looking. Don't settle. (Steve Jobs)",
                "You Don’t Have To Be Great To Start, But You Have To Start To Be Great. (Zig Ziglar)",
                "A Clear Vision, Backed By Definite Plans, Gives You A Tremendous Feeling Of Confidence And Personal Power. (Brian Tracy)",
                "There Are No Limits To What You Can Accomplish, Except The Limits You Place On Your Own Thinking. (Brian Tracy)",
                "Integrity is the most valuable and respected quality of leadership. Always keep your word.",
                "Leadership is the ability to get extraordinary achievement from ordinary people.",
                "Leaders set high standards. Refuse to tolerate mediocrity or poor performance.",
                "Clarity is the key to effective leadership. What are your goals?",
                "The best leaders have a high Consideration Factor. They really care about their people.",
                "Leaders think and talk about the solutions. Followers think and talk about the problems.",
                "The key responsibility of leadership is to think about the future. No one else can do it for you.",
                "The effective leader recognizes that they are more dependent on their people than they are on them. Walk softly.",
                "Leaders never use the word failure. They look upon setbacks as learning experiences.",
                "Practice Golden Rule Management in everything you do. Manage others the way you would like to be managed.",
                "Superior leaders are willing to admit a mistake and cut their losses. Be willing to admit that you’ve changed your mind. Don’t persist when the original decision turns out to be a poor one.",
                "Leaders are anticipatory thinkers. They consider all consequences of their behaviors before they act.",
                "The true test of leadership is how well you function in a crisis.",
                "Leaders concentrate single-mindedly on one thing– the most important thing, and they stay at it until it’s complete.",
                "The three ‘C’s’ of leadership are Consideration, Caring, and Courtesy. Be polite to everyone.",
                "Respect is the key determinant of high-performance leadership. How much people respect you determines how well they perform.",
                "Leadership is more who you are than what you do.",
                "Entrepreneurial leadership requires the ability to move quickly when opportunity presents itself.",
                "Leaders are innovative, entrepreneurial, and future oriented. They focus on getting the job done.",
                "Leaders are never satisfied; they continually strive to be better."));
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.notification_generic_blue,R.drawable.notification_generic_green,R.drawable.notification_generic_purple,R.drawable.notification_generic_red));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size())); //-1 because size() index does not exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_TIMEBASED(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList("Almost done!", "You got it soon!", "Be confident, the end is near.")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(countdown.getCountdownTitle()+" - Only "+countdown.getTotalSecondsNoScientificNotation()+" seconds to go!",
                countdown.getCountdownTitle()+" - Just "+countdown.getRemainingPercentage(2)+" % left.",
                countdown.getCountdownTitle()+" - Countdown ends on "+countdown.getUntilDateTime(),
                countdown.getCountdownTitle()+" - A motivating notification will be sent every "+(countdown.getNotificationInterval()/1000)+" seconds. :)"));
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
