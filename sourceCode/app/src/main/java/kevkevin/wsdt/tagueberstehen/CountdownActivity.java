package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.daimajia.swipe.SwipeLayout;

import java.text.DecimalFormat;

import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.classes.entities.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.CountdownCounter;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppNotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.ShareMgr;

import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.INotificationMgr.*;

public class CountdownActivity extends AppCompatActivity {
    private Long countdownId = (-1L);
    private Countdown countdown;
    private static final String TAG = "CountdownActivity";
    private Intent lastIntent;
    private CountdownCounter countdownCounter;
    private InAppNotificationMgr inAppNotificationMgr = new InAppNotificationMgr(); //must be a member! (to prevent influencing iapnotifications of other activities)
    public static boolean runGeneratingRandomQuotes = true; //true by default, because surfaceView, when false then countdownCounter thread will NOT automatically refresh quotes (also used to pause etc.)
    private static ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);


        // SHOW FULL PAGE ADD
        AdMgr adMgr = new AdMgr(this);
        //make it possible to load and prevent stopping ui (also afterwards: because coutndown does not refresh!) --> esp. when fullpage from other activities opens to slow and gets closed in the countdownactivity
        //IMPORTANT: Do not place here fullpage ad because this blocks the countdown!
        adMgr.loadBannerAd((RelativeLayout) findViewById(R.id.content_main));
        //adMgr.loadFullPageAd(null, null); //why returns this asshole back to mainActivity?


        //Notifications regularly: How long do you need to work today or similar and easy type in maybe in notification bar!
        //motivational notifications: just 2 hours to go! etc.
        //Drink a glass of water and the day goes by faster etc.

        //ACTIVITY OPENED BY OTHER ACTIVITY: ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        this.setLastIntent(getIntent());
        this.countdownId = this.getLastIntent().getLongExtra(IDENTIFIER_COUNTDOWN_ID, -1);
        //maybe by main menu or notification, but we get the same Extra: COUNTDOWN_ID with the ID
        if (this.getCountdown() == null) {this.setCountdown(Countdown.query(this, this.countdownId));} //load countdown if not already loaded by actionbar menu
        initializeCountdownDataSwipeLayout(); //to restore current bottom view if surface view would get updated (preventing it)
        startCountdownOnUI(); //0 is default value
        loadCountdownDataToUI();
        showInAppNotification(); //show inappnotification with countdown/notification data if available (using helperclass)
    }


    @Override
    protected void onStop() {
        super.onStop();
        HelperClass.stopPeriodically(); //also stop refreshing share intent!

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
        SwipeLayout swipeLayout = (findViewById(R.id.swipeLayout_countdownActivity));
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
            TextSwitcher randomQuoteView = (findViewById(R.id.swipeLayout_countdownActivity_randomQuotes_quote));
            randomQuoteView.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() { //for switcher to draw textview in it
                    TextView quoteText = new TextView(CountdownActivity.this);
                    quoteText.setTextSize(14);
                    quoteText.setTextColor(getResources().getColor(R.color.colorLight_ddd));
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
        //When used outside of onClick, then v might/will be NULL!, also use here user selected quote language packages!
        if (this.getCountdown() != null) { //might be called after countdown is already deleted.
            ((TextSwitcher) findViewById(R.id.swipeLayout_countdownActivity_randomQuotes_quote)).setText(
                    this.getCountdown().getRandomQuoteSuitableForCountdown(this)); //use random quote
        }
    }

    public void onSwipeLayoutClick_UserInstruction(View v) { //only use on mainView of SwipeLayout, after swiping they will know that they can do it!
        Log.d(TAG, "onSwipeLayoutClick: Clicked on swipeLayout. Informed user. ");
        Toast.makeText(this, R.string.mainActivity_swipeLayout_onClickInstruction,Toast.LENGTH_SHORT).show();
    }

    public void loadCountdownDataToUI() { //only on start
        setNewRandomQuote(null);
        Log.d(TAG, "loadCountdownDataToUI: Trying to load countdown details to UI.");
        if (this.getCountdown() != null) {
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_title)).setText(this.getCountdown().getCouTitle());
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_description)).setText(this.getCountdown().getCouDescription());
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_fromDateTime)).setText(this.getCountdown().getCouStartDateTime());
            ((TextView) findViewById(R.id.swipeLayout_countdownActivity_countdownData_untilDateTime)).setText(this.getCountdown().getCouUntilDateTime());
        } else { //do not show toast or similar (because already showing in startCountdownOnUI())
            Log.e(TAG, "loadCountdownDataToUI: Countdown not found. Could not load countdown data.");
        }
    }

    public void startCountdownOnUI() {
        if (this.getCountdown() != null) { //if (-1) was e.g. found as intent countdown id then it will be null
            //search in storage and get total seconds then start countdown (if not found because smaller 0 or deleted and notification referenced it
            this.setCountdownCounter(new CountdownCounter(this, this.getCountdown())); //USE MEMBER, so we can refresh also ShareIntent
            this.getCountdownCounter().runOnUI();
            HelperClass.doPeriodically(this, 1000, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                @Override
                public void success_is_true(@Nullable Object... args) {
                    refreshShareIntent();
                    Log.d(TAG, "startCountdownOnUI:doPeriodically: Refreshed share intent.");
                }

                @Override
                public void failure_is_false(@Nullable Object... args) {
                    Log.d(TAG, "startCountdownOnUI:doPeriodically: Stopped refreshing share intent.");
                }
            });
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


    public Countdown getCountdown() {
        return countdown;
    }

    public void setCountdown(Countdown countdown) {
        this.countdown = countdown;
    }

    public CountdownCounter getCountdownCounter() {
        return countdownCounter;
    }

    public void setCountdownCounter(CountdownCounter countdownCounter) {
        this.countdownCounter = countdownCounter;
    }

    public InAppNotificationMgr getInAppNotificationMgr() {
        return inAppNotificationMgr;
    }

    public ShareActionProvider getShareActionProvider() {
        return shareActionProvider;
    }

    public void setShareActionProvider(ShareActionProvider shareActionProvider) {
        CountdownActivity.shareActionProvider = shareActionProvider;
    }


    // ################################################################################################################
    // NOTIFICATION-CONTENT SHOWER ####################################################################################
    // ################################################################################################################

    //Show in-app-notification (incl. animation)
    private void showInAppNotification() {
        Log.d(TAG, "showInAppNotificationIfAvailable: Started method.");
        try {
            this.getInAppNotificationMgr().showInAppNotification(this,
                    this.getLastIntent().getStringExtra(IDENTIFIER_CONTENT_TITLE),
                    this.getLastIntent().getStringExtra(IDENTIFIER_CONTENT_TEXT),
                    this.getLastIntent().getIntExtra(IDENTIFIER_SMALL_ICON, -1),
                    (RelativeLayout) findViewById(R.id.content_main),false); //do not preventmultiple, because reopen of notification would block new notification until old animation (which we cant see anymore is done)
        } catch (Exception e) {
            Log.e(TAG, "showInApNotificationIfAvailable: Could not show inapp-notification. Maybe activity was opened from main menu.");
            e.printStackTrace();
        }
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
                this.setCountdown(Countdown.query(this, this.countdownId));

                //Provider is member so we can refresh share intent!
                this.setShareActionProvider(shareActionProvider); //must be called before refreshShareIntent()!
                refreshShareIntent();
                Log.d(TAG, "onCreateOptionsMenu: Tried to set share intent first time.");
            } catch (NullPointerException e) {
                Log.e(TAG, "onCreateOptionsMenu: Could not share countdown, because countdown not found!");
            }
        } else {
            Log.w(TAG, "onCreateOptionsMenu: Could not share values, because Actionprovider is null!");
        }//needing to call in menuOnCreate() first time (but we can change it afterwards with setShareIntent)

        return true;
    }


    //TODO: use for getSimpleShareIntent({nonNullIntent}) to avoid object allocation in loop (so we have to keep the current share intent and just change the extras)
    private void refreshShareIntent() { //call when to refresh! (because of totalSeconds e.g.)
        /*Set implicit intent with extras and actions so other apps know what to share!
        Extra method, so we can setShareIntent dynamically not only on activity creation [countdown values share etc.]*/
        try {
            Intent shareIntent;
            Resources res = getResources();
            if (this.getCountdown() != null) {
                Log.d(TAG, "refreshShareIntent: Trying to refresh message (reset extras).");
                shareIntent = ShareMgr.getSimpleShareIntent(null, res.getString(R.string.app_name),
                        String.format(res.getString(R.string.actionBar_countdownActivity_menu_shareCountdown_shareContent_text_extended),
                                String.valueOf(new DecimalFormat("0.00").format(this.getCountdown().getTotalHoursValue(this))),
                                String.valueOf(new DecimalFormat("0.00").format(this.getCountdown().getTotalMinutesValue(this))),
                                String.valueOf(new DecimalFormat("0").format(this.getCountdown().getTotalSecondsValue(this))),

                                this.getCountdown().getCouTitle(), this.getCountdown().getCouDescription()));
            } else {
                Log.e(TAG, "refreshShareIntent: ShareIntent or/and Countdown is NULL! Cannot set/refresh share content. ");
                shareIntent = ShareMgr.getSimpleShareIntent(null, res.getString(R.string.app_name), res.getString(R.string.error_contactAdministrator));
            }
            this.getShareActionProvider().setShareIntent(shareIntent);
            Log.d(TAG, "refreshShareIntent: Tried to refresh share intent.");
        } catch (NullPointerException e) {
            Log.e(TAG, "refreshShareIntent: Could not refresh shareIntent!");
            e.printStackTrace();
        }
    }
}
