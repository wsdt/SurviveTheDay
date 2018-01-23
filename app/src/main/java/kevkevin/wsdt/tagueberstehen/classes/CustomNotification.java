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
        randomNotification.titleList.addAll(Arrays.asList("Keep going!","Do not give up!","Keep it up!", "You get that!", "That is nothing!", "Easy :)", "Never settle!")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList("The way get started is to quit talking and begin doing. (Walt Disney)",
                "The pessimist sees difficulty in every opportunity. The optimist sees opportunity in every difficulty. (Winston Churchill)",
                "Don't let yesterday take up too much of today. (Will Rogers)",
                "You learn more from failure than from success. Don't let it stop you. Failure build character.",
                "It's not whether you get knocked down, it's whether you get up. (Vince Lombardi)",
                "If you are working on something that you really care about, you don't have to be pushed. The vision pulls you. (Steve Jobs)",
                "People who are crazy enough to think they can change the world, are the ones who do. (Rob Siltanen)",
                "Failure will never overtake me if my determination to succeed is strong enough. (Og Mandino)",
                "Entrepreneurs are great at dealing with uncertainty and also very good at minimizing risk. That's the classic entrepreneur. (Mohnish Pabrai)",
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
                "Leaders are never satisfied; they continually strive to be better.",
                "The best and most beautiful things in the world cannot be seen or even touched - they must be felt with the heart. (Helen Keller)",
                "The best preparation for tomorrow is doing your best today. (H. Jackson Brown, Jr.)",
                "I can't change the direction of the wind, but I can adjust my sails to always reach my destination. (Jimmy Dean)",
                "We must let go of the life we have planned, so as to accept the one that is waiting for us. (Joseph Campbell)",
                "You must do the things you think you cannot do. (Eleanor Roosevelt)",
                "Put your heart, mind, and soul into even your smallest acts. This is the secret of success. (Swami Sivananda)",
                "Start by doing what's necessary; then do what's possible; and suddenly you are doing the impossible. (Francis of Assisi)",
                "The limits of the possible can only be defined by going beyond them into the impossible. (Arthur C. Clarke)",
                "Happiness is not something you postpone for the future; it is something you design for the present. (Jim Rohn)",
                "Try to be a rainbow in someone's cloud. (Maya Angelou)",
                "It is during our darkest moments that we must focus to see the light. (Aristotle)",
                "Health is the greatest gift, contentment the greatest wealth, faithfulness the best relationship. (Buddha)",
                "Change your thoughts and you change the world. (Norman Vincent Peale)",
                "Nothing is impossible, the word itself says 'I'm possible'. (Audrey Hepburn)",
                "My mission in life is not merely to survive, but to thrive; and to do so with some passion, some compassion, some humor, and some style. (Maya Angelou)",
                "Today I choose life. Every morning when I wake up I can choose joy, happiness, negativity, pain... To feel the freedom that comes from being able to continue to make mistakes and choices - today I choose to feel life, not to deny my humanity but embrace it. (Kevyn Aucoin)",
                "Your work is going to fill a large part of your life, and the only way to be truly satisfied is to do what you believe is great work. And the only way to do great work is to love what you do. If you haven't found it yet keep looking. Don't settle. As with all matters of the heart, you'll know when you find it. (Steve Jobs)",
                "Believe you can and you're halfway there. (Theodore Roosevelt)",
                "Keep your face always toward the sunshine - and shadows will fall behind you. (Walt Whitman)",
                "Perfection is not attainable, but if we chase perfection we can catch excellence. (Vince Lombardi)",
                "If opportunity doesn't knock, build a door. (Milton Berle)",
                "What we think, we become. (Buddha)",
                "Clouds come floating into my life, no longer to carry rain or usher storm, but to add color to my sunset sky. (Rabindranath Tagore)",
                "What lies behind you and what lies in front of you, pales in comparison to what lies inside of you. (Ralph Waldo Emerson)",
                "Someone is sitting in the shade today because someone planted a tree a long time ago. (Warren Buffett)",
                "No act of kindness, no matter how small, is ever wasted. (Aesop)",
                "I believe that if one always looked at the skies, one would end up with wings. (Gustave Flaubert)",
                "We know what we are, but know not what we may be. (William Shakespeare)",
                "Let us sacrifice our today so that our children can have a better tomorrow. (A. P. J. Abdul Kalam)",
                "There are two ways of spreading light: to be the candle or the mirror that reflects it. (Edith Wharton)",
                "Let us remember: One book, one pen, one child, and one teacher can change the world. (Malala Yousafzai)",
                "All you need is the plan, the road map, and the courage to press on to your destination. (Earl Nightingale)",
                "Your personal life, your professional life, and your creative life are all intertwined. I went through a few very difficult years where I felt like a failure. But it was actually really important for me to go through that. Struggle, for me, is the most inspirational thing in the world at the end of the day - as long as you treat it that way. (Skylar Grey)",
                "Your present circumstances don't determine where you can go; they merely determine where you start. (Nido Qubein)",
                "I will love the light for it shows me that way, yet I will endure the darkness because it shows me the stars. (Og Mandino)",
                "It is in your moments of decision that your destiny is shaped. (Tony Robbins)",
                "Thousands of candles can be lighted from a single candle, and the life of the candle will not be shortened. Happiness never decreases by being shared. (Buddha)",
                "The only journey is the one within. (Rainer Maria Rilke)",
                "As we express our gratitude, we must never forget that the highest appreciation is not to utter words, but to live by them. (John F. Kennedy)",
                "The bird is powered by its own life and by its motivation. (A. P. J. Abdul Kalam)",
                "No matter what people tell you, words and ideas can change the world. (Robin Williams)",
                "Don't judge each day by the harvest you reap but by the seeds you plant. (Robert Louis Stevenson)",
                "I believe in pink. I believe that laughing is the best calorie burner. I believe in kissing, kissing a lot. I believe in being strong when everything seems to be going wrong. I believe that happy girls are the prettiest girls. I believe that tomorrow is another day and I believe in miracles. (Audrey Hepburn)",
                "Shoot for the moon and if you miss you will still be among the stars. (Les Brown)",
                "Let your life lightly dance on the edges of Time like dew on the tip of a leaf. (Rabindranath Tagore)",
                "I hated every minute of training, but I said, 'Don't quit. Suffer now and live the rest of your life as a champion.' (Muhammad Ali)",
                "We can't help everyone, but everyone can help someone. (Ronald Reagan)",
                "God always gives His best to those who leave the choice with him. (Jim Elliot)",
                "When you have a dream, you've got to grab it and never let go. (Carol Burnett)",
                "If you believe in yourself and have dedication and pride - and never quit, you'll be a winner. The price of victory is high but so are the rewards. (Paul Bryant)",
                "Let us make our future now, and let us make our dreams tomorrow's reality. (Malala Yousafzai)",
                "A hero is someone who has given his or her life to something bigger than oneself. (Joseph Campbell)",
                "When the sun is shining I can do anything; no mountain is too high, no trouble too difficult to overcome. (Wilma Rudolph)",
                "Throw your dreams into space like a kite, and you do not know what it will bring back, a new life, a new friend, a new love, a new country. (Anais Nin)",
                "If you always put limit on everything you do, physical or anything else. It will spread into your work and into your life. There are no limits. There are only plateaus, and you must not stay there, you must go beyond them. (Bruce Lee)",
                "To love means loving the unloveable. To forgive means pardoning the unpardonable. Faith means believing the unbelievable. Hope means hoping when everything seems hopeless. (Gilbert K. Chesterton)",
                "The measure of who we are is what we do with what we have. (Vince Lombardi)",
                "It is never too late to be what you might have been. (George Eliot)",
                "There is nothing impossible to him who will try. (Alexander the Great)",
                "Two roads diverged in a wood and I - took the one less traveled by, and that has made all the difference. (Robert Frost)",
                "From a small seed a mighty trunk may grow. (Aeschylus)",
                "Give light, and the darkess will disappear of itself. (Desiderius Erasmus)",
                "Love is a fruit in season at all times, and within reach of every hand. (Mother Teresa)",
                "Be brave enough to live life creatively. The creative place where no one else has ever been. (Alan Alda)",
                "If I have seen further than others, it is by standing upon the shoulders of giants. (Isaac Newton)",
                "Follow your bliss and the universe will open doors where there were only walls. (Joseph Campbell)",
                "Thinking: the talking of the soul with itself. (Plato)",
                "Happiness resides not in possessions, and not in gold, happiness dwells in the soul. (Democritus)",
                "How wonderful it is that nobody need wait a single moment before starting to improve the world. (Anne Frank)",
                "When we seek to discover the best in others, we somehow bring out the best in ourselves. (William Arthur Ward)",
                "With self-discipline most anything is possible. (Theodore Roosevelt)",
                "To the mind that is still, the whole universe surrenders. (Lao Tzu)",
                "Today is the only day. Yesterday is gone. (John Wooden)",
                "Your big opportunity may be right where you are now. (Napoleon Hill)",
                "The power of imagination makes us infinite. (John Muir)",
                "Out of difficulties grow miracles. (Jean de la Bruyere)",
                "What makes the desert beautiful is that somewhere it hides a well. (Antoine de Saint-Exupery)",
                "Tomorrow is the most important thing in life. Comes into us at midnight very clean. It's perfect when it arrives and it puts itself in our hands. It hopes we've learning something from yesterday. (John Wayne)",
                "If the world seems cold to you, kindle fires to warm it. (Lucy Larcom)",
                "How glorious a greeting the sun gives the mountains. (John Muir)",
                "When you get into a tight place and everything goes against you, till it seems as though you could not hang on a minute longer, never give up then, for that is just the place and time that the tide will turn. (Harriet Beecher Stowe)",
                "Happiness is a butterfly, which when pursued, is always beyond your grasp, but which, if you will sit down quietly, may alight upon you. (Nathaniel Hawthorne)",
                "Whoever is happy will make others happy too. (Anne Frank)",
                "In a gentle way, you can shake the world. (Mahatma Gandhi)",
                "Don't limit yourself. Many people limit themselves to what they think they can do. You can go as far as your mind lets you. What you believe, remember, you can achieve. (Mary Kay Ash)",
                "We can change our lives. We can do, have, and be exactly what we wish. (Tony Robbings)",
                "Even if I knew that tomorrow the would would go to pieces, I would still plant my apple tree. (Martin Luther)",
                "Memories of our lives, of our works and our deeds will continue in others. (Rosa Parks)",
                "The things that we love tell us what we are. (Thomas Aquinas)",
                "Somewhere, something incredible is waiting to be known. (Sharon Begley)",
                "The glow of one warm thought is to me worth more than money. (Thomas Jefferson)",
                "If a man does not keep pace with his companions, perhaps it is because he hears a different drummer. Let him step to the music which he hears, however measured or far away. (Henry David Thoreau)",
                "Accept the things to which fate binds you, and love the people whom fate brings you together, but do so with all your heart. (Marcus Aurelius)",
                "If we did all the things we are capable of, we would literally astound ourselves. (Thomas A. Edison)",
                "Each day provides its own gifts. (Marcus Aurelius)",
                "Keep your feet on the ground, but let your heart soar as high as it will. Refuse to be average or to surrender to the chill of your spiritual environment. (Arthur Helps)",
                "Let us dream of tomorrow where we can truly love from the soul, and know love as the ultimate truth at the heart of all creation. (Michael Jackson)",
                "You change your life by changing your heart. (Max Lucado)",
                "A champion is someone who gets up when he can't. (Jack Dempsey)"
        ));
        randomNotification.iconList.addAll(Arrays.asList(R.drawable.notification_generic_blue,R.drawable.notification_generic_green,R.drawable.notification_generic_purple,R.drawable.notification_generic_red));

        //Choose one for each arraylist by random index (max is size-1!)
        randomNotification.title = randomNotification.titleList.get(this.random.nextInt(randomNotification.titleList.size())); //size() index does exist!
        randomNotification.text = randomNotification.textList.get(this.random.nextInt(randomNotification.textList.size()));
        randomNotification.icon = randomNotification.iconList.get(this.random.nextInt(randomNotification.iconList.size()));

        return randomNotification;
    }

    private NotificationContent createRandomNotification_TIMEBASED(Countdown countdown) {
        NotificationContent randomNotification = new NotificationContent(); //create custom instance (important not to use same instance for each cateogry)

        randomNotification.titleList.addAll(Arrays.asList("Almost done!", "You got it soon!", "Be confident, the end is near.")); //converts array to list and adds all of them
        randomNotification.textList.addAll(Arrays.asList(countdown.getCountdownTitle()+" - Only "+countdown.getTotalSecondsNoScientificNotation()+" seconds to go!",
                countdown.getCountdownTitle()+" - Just "+countdown.getRemainingPercentage(2, true)+" % left.",
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
