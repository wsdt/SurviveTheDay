package kevkevin.wsdt.tagueberstehen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.daimajia.swipe.SwipeLayout;

import java.util.Random;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CountdownCounter;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;

public class CountdownActivity extends AppCompatActivity {
    private int countdownId = (-1);
    private Countdown countdown;
    private static final String TAG = "CountdownActivity";
    private Intent lastIntent;
    private GlobalAppSettingsMgr globalAppSettingsMgr;
    private Intent shareIntent; //used for refreshing extras
    private CountdownCounter countdownCounter;
    public static boolean runGeneratingRandomQuotes = true; //true by default, because surfaceView, when false then countdownCounter thread will NOT automatically refresh quotes (also used to pause etc.)

    //TODO: With swipeLayout or/and automatically random quotes (only quotes)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        //Create activity globalStorageMgr
        this.globalAppSettingsMgr = new GlobalAppSettingsMgr(this);

        // SHOW FULL PAGE ADD
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        //make it possible to load and prevent stopping ui (also afterwards: because coutndown does not refresh!) --> esp. when fullpage from other activities opens to slow and gets closed in the countdownactivity
        //IMPORTANT: Do not place here fullpage ad because this blocks the countdown!
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.content_main));
        //adManager.loadFullPageAd(null, null); //why returns this asshole back to mainActivity?


        //Notifications regularly: How long do you need to work today or similar and easy type in maybe in notification bar!
        //motivational notifications: just 2 hours to go! etc.
        //Drink a glass of water and the day goes by faster etc.

        //ACTIVITY OPENED BY OTHER ACTIVITY: ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        this.setLastIntent(getIntent());
        this.countdownId = this.getLastIntent().getIntExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID, -1);
        //maybe by main menu or notification, but we get the same Extra: COUNTDOWN_ID with the ID
        if (this.getCountdown() == null) {this.setCountdown((new InternalCountdownStorageMgr(this).getCountdown(this.countdownId)));} //load countdown if not already loaded by actionbar menu
        initializeCountdownDataSwipeLayout(); //to restore current bottom view if surface view would get updated (preventing it)
        startCountdownOnUI(); //0 is default value
        loadCountdownDataToUI();

        //Wait until views are drawn (for size etc.)
        final RelativeLayout inAppNotification = ((RelativeLayout) findViewById(R.id.notificationContent));
        final ViewTreeObserver observer = inAppNotification.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                showInAppNotificationIfAvailable();
                inAppNotification.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        //IMPORTANT: Stop thread of countdowncounterservice when activity gets hidden
        if (this.getCountdownCounter() != null) {
            if (this.getCountdownCounter().getCountdownCounterThread() != null) {
                this.getCountdownCounter().getCountdownCounterThread().interrupt(); //stop it
            } else {
                Log.d(TAG, "onStop: CountdownCounterThread is null. Could not stop it.");
            }
        } else {Log.e(TAG, "onStop: CountdownCounter itself is null. (Countdown might have been deleted before)");}
    }

    public void pausePlayRandomQuote(@Nullable View v) { //if null, then called from other methods instead of onclick pause btn
        if (runGeneratingRandomQuotes) { //toggle value
            runGeneratingRandomQuotes = false;
            Toast.makeText(this, R.string.countdownActivity_countdownDetails_randomQuotes_quoteGeneratorPause, Toast.LENGTH_SHORT).show();
        } else {
            runGeneratingRandomQuotes = true;
            Toast.makeText(this, R.string.countdownActivity_countdownDetails_randomQuotes_quoteGeneratorPlay, Toast.LENGTH_SHORT).show();
        }
    }

    public void initializeCountdownDataSwipeLayout() { //by setting this value to false, we could also PAUSE automatic refresh!
        SwipeLayout swipeLayout = ((SwipeLayout) findViewById(R.id.swipeLayout_countdownActivity));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, findViewById(R.id.swipeLayout_countdownActivity_countdownData));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, findViewById(R.id.swipeLayout_countdownActivity_shareQuote));
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) { //set here false, to prevent skipping animation (because quote gets updated)
                runGeneratingRandomQuotes = false; //going to other bottom view! so no need to update random quotes
                Log.d(TAG, "initializeCountdownDataSwipeLayout:onStartOpen: Set runGeneratingRandomQuotes to false.");
            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {
                runGeneratingRandomQuotes = true; //set here true and not onStartClose to prevent quitting animation
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        //Set animation to randomQuote View of Swipelayout (onTextChange fadein/out)
        try {
            TextSwitcher randomQuoteView = ((TextSwitcher) findViewById(R.id.swipeLayout_countdownActivity_randomQuotes_quote));
            randomQuoteView.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() { //for switcher to draw textview in it
                    TextView quoteText = new TextView(CountdownActivity.this);
                    quoteText.setTextSize(14);
                    quoteText.setTextColor(getResources().getColor(R.color.colorLight));
                    return quoteText;
                }
            });

            Animation animationFadein = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            animationFadein.setDuration(200);
            randomQuoteView.setInAnimation(animationFadein);
            Animation animationFadeout = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
            animationFadeout.setDuration(200);
            randomQuoteView.setOutAnimation(animationFadeout);
        } catch (Exception e) {
            Log.e(TAG, "initializeCountdownDataSwipeLayout: Could not set TextSwitcher animation for random quotes!");
            e.printStackTrace();
        }
    }

    public void setNewRandomQuote(@Nullable View v) { //is called when clicking onRefreshButton, onActivity start and regularly (automatic refresh)
        //When used outside of onClick, then v might/will be NULL!
        //TODO: also use here user selected quote language packages!
        String[] quotes = this.getResources().getStringArray(R.array.customNotification_random_generic_texts_en);
        ((TextSwitcher) findViewById(R.id.swipeLayout_countdownActivity_randomQuotes_quote)).setText(
                quotes[(new Random()).nextInt(quotes.length-1)] //use random quote
        );
    }

    public void onSwipeLayoutClick_UserInstruction(View v) { //only use on mainView of SwipeLayout, after swiping they will know that they can do it!
        Log.d(TAG, "onSwipeLayoutClick: Clicked on swipeLayout. Informed user. ");
        Toast.makeText(this, R.string.mainActivity_swipeLayout_onClickInstruction,Toast.LENGTH_SHORT).show();
    }

    public void loadCountdownDataToUI() { //only on start
        setNewRandomQuote(null);
        Log.d(TAG, "loadCountdownDataToUI: Trying to load countdown details to UI.");
        if (this.getCountdown() != null) {
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_title)).setText(this.getCountdown().getCountdownTitle());
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_description)).setText(this.getCountdown().getCountdownDescription());
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_fromDateTime)).setText(this.getCountdown().getStartDateTime());
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_untilDateTime)).setText(this.getCountdown().getUntilDateTime());
        } else { //do not show toast or similar (because already showing in startCountdownOnUI())
            Log.e(TAG, "loadCountdownDataToUI: Countdown not found. Could not load countdown data.");
        }
    }

    public void startCountdownOnUI() {
        if (this.getCountdown() != null) { //if (-1) was e.g. found as intent countdown id then it will be null
            //search in storage and get total seconds then start countdown (if not found because smaller 0 or deleted and notification referenced it
            this.setCountdownCounter(new CountdownCounter(this, this.getCountdown()));
            this.getCountdownCounter().runOnUI();
        } else {
            Toast.makeText(this, R.string.countdownActivity_countdownNotFound, Toast.LENGTH_LONG).show();
            Log.e(TAG, "startCountdownOnUI: Countdown not found. ID: " + countdownId);
        }
    }

    public Intent getLastIntent() {
        return lastIntent;
    }

    public void setLastIntent(Intent lastIntent) {
        this.lastIntent = lastIntent;
    }


    public Intent getShareIntent() {
        return shareIntent;
    }

    public void setShareIntent(Intent shareIntent) {
        this.shareIntent = shareIntent;
    }

    public Countdown getCountdown() {
        return countdown;
    }

    public void setCountdown(Countdown countdown) {
        this.countdown = countdown;
    }


    // ################################################################################################################
    // NOTIFICATION-CONTENT SHOWER ####################################################################################
    // ################################################################################################################

    //Show in-app-notification (incl. animation)
    private void showInAppNotificationIfAvailable() {
        Log.d(TAG, "showInAppNotificationIfAvailable: Started method.");
        try {
            int notificationImage = this.getLastIntent().getIntExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_SMALL_ICON, -1);
            String notificationHeading = this.getLastIntent().getStringExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_CONTENT_TITLE);
            String notificationFullText = this.getLastIntent().getStringExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_CONTENT_TEXT);
            final RelativeLayout notificationContent = (RelativeLayout) findViewById(R.id.notificationContent); //call after values assigned so correct height

            if (notificationImage != (-1) && !notificationHeading.equals("") && !notificationFullText.equals("")) {
                //Set notification contents
                ((ImageView) findViewById(R.id.notificationImage)).setImageBitmap(BitmapFactory.decodeResource(getResources(), notificationImage));
                ((TextView) findViewById(R.id.notificationHeading)).setText(notificationHeading);
                ((TextView) findViewById(R.id.notificationFullText)).setText(notificationFullText);
                Log.d(TAG, "showInAppNotificationIfAvailable: Tried to assign values to view. ");

                //Set negative Y but assign values BEFORE (because height might change because of wrap content)
                final int hiddenPosition = ((notificationContent.getHeight()) * (-1));
                notificationContent.setY(hiddenPosition); //assign height*-1 so notification will get exactly behind display
                Log.d(TAG, "showInAppNotificationIfAvailable: Tried to positionate inapp-notification outside screen: " + hiddenPosition);

                //Set OnClickListener for close Button
                notificationContent.findViewById(R.id.notificationClose).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "showInAppNotificationIfAvailable:onClick: Tried to close inapp notification, because close btn clicked.");
                        closeActiveInAppNotification(notificationContent, 0); //close instantly when clicking on button
                    }
                });

                //TODO: multiple properties (alpha also so it hides or comes): https://stackoverflow.com/questions/28352352/change-multiple-properties-with-single-objectanimator
                final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(notificationContent, "y", 0); //get it back to positive animated (0 because we want no small space above shape)
                objectAnimator.setDuration(Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS); //1,5 seconds
            /*objectAnimator.setRepeatCount(1); //show it and hide it after duration expired [making this with count var and restarting animation in onAnimationEnd()]
            objectAnimator.setRepeatMode(ValueAnimator.REVERSE);*/
                objectAnimator.addListener(new Animator.AnimatorListener() {
                    int count = 0;

                    @Override
                    public void onAnimationStart(Animator animator) {
                        Log.d(TAG, "onAnimationStart: Inapp notification animation started.");
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (count++ < 1) { //create new object animator because we want not the same animation but reverse
                            closeActiveInAppNotification(notificationContent, globalAppSettingsMgr.getInAppNotificationShowDurationInMs() + Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS); //how long should be notification displayed (adding animation duration because time period delay is inclusive animation));
                        } else {
                            Log.d(TAG, "onAnimationEnd: Inapp notification animation finished.");
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        Log.d(TAG, "onAnimationCancel: Cancelled animation of inappnotification.");
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                        Log.d(TAG, "onAnimationRepeat: Repeated in app notification animation.");
                        //objectAnimator.setStartDelay(3000); //do not use animator (start delay would be ignored) does not work because not called
                    }
                });
                objectAnimator.start();
            } else {
                notificationContent.setVisibility(View.GONE);
                Log.w(TAG, "showInAppNotificationIfAvailable: Activity not called by notification.");
            }
        } catch (Exception e) {
            Log.e(TAG, "showInApNotificationIfAvailable: Could not show inapp-notification. Maybe activity was opened from main menu.");
            e.printStackTrace();
        }
    }

    //Helper method for closeBtn of inAppNotification and objectAnimator end
    private void closeActiveInAppNotification(@NonNull RelativeLayout notificationContent, int delay) {
        ObjectAnimator objectAnimatorClose = ObjectAnimator.ofFloat(notificationContent, "alpha", 0);
        objectAnimatorClose.setDuration(Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_CLOSE_ANIMATION_DURATION_IN_MS);
        objectAnimatorClose.setStartDelay(delay);
        objectAnimatorClose.start(); //no delay if clicked on close btn, but delay if automatic close
        Log.d(TAG, "closeActiveInAppNotification: Tried to close active in app notification.");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setLastIntent(intent); //set last calling intent (because of single top would not call activity again, so maybe always same intent will be shown)
    }

    // ACTIONBAR - MENU +++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Trying to create actionbar menu.");
        //Inflate menu resource file
        getMenuInflater().inflate(R.menu.menu_countdown, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_shareCountdown);

        //fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Log.d(TAG, "onCreateOptionsMenu: Trying to set ShareIntent.");
        if (shareActionProvider != null) {
            try {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND); //implicit intent for sharing
                shareIntent.setType("text/plain"); //currently only text (todo: later picture of countdown? etc.)
                this.setCountdown(new InternalCountdownStorageMgr(this).getCountdown(this.countdownId));
                this.setShareIntent(shareIntent); //important, so we can modify extras afterwards
                refreshShareIntent(); //refreshes set Intent (setShareIntent must be called before!)
                shareActionProvider.setShareIntent(shareIntent);
                //TODO: always return current value (not only when created)
                /*shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                    @Override
                    public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                        Log.d(TAG, "onCreateOptionsMenu:onShareTargetSelected: Trying to refresh share intent.");
                        //class member intent seems not to be refreshed
                        source.setShareIntent(refreshShareIntent());
                        return false;
                    }
                });*/
            } catch (NullPointerException e) {
                Log.e(TAG, "onOptionsItemSelected: Could not share countdown, because countdown not found!");
            }
        } else {
            Log.w(TAG, "setShareIntent: Could not share values, because Actionprovider is null!");
        }//needing to call in menuOnCreate() first time (but we can change it afterwards with setShareIntent)

        return true;
    }


    private Intent refreshShareIntent() { //call when to refresh! (because of totalSeconds e.g.)
        /*Set implicit intent with extras and actions so other apps know what to share!
        Extra method, so we can setShareIntent dynamically not only on activity creation [countdown values share etc.]*/
        if (this.getCountdown() != null && this.getShareIntent() != null) {
            Log.d(TAG, "refreshShareIntent: Trying to refresh message (reset extras).");
            this.getShareIntent().putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            this.getShareIntent().putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.actionBar_countdownActivity_menu_shareCountdown_shareContent_text), this.getCountdown().getTotalSecondsNoScientificNotation(), this.getCountdown().getCountdownTitle(), this.getCountdown().getCountdownDescription()));
        } else {
            Log.e(TAG, "refreshShareIntent: ShareIntent or/and Countdown is NULL! Cannot set/refresh share content.");
        }
        return this.getShareIntent();
    }

    public CountdownCounter getCountdownCounter() {
        return countdownCounter;
    }

    public void setCountdownCounter(CountdownCounter countdownCounter) {
        this.countdownCounter = countdownCounter;
    }
}
