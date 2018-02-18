package kevkevin.wsdt.tagueberstehen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

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
        //adManager.loadFullPageAd(null, null);


        //Notifications regularly: How long do you need to work today or similar and easy type in maybe in notification bar!
        //motivational notifications: just 2 hours to go! etc.
        //Drink a glass of water and the day goes by faster etc.

        //ACTIVITY OPENED BY OTHER ACTIVITY: ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        this.setLastIntent(getIntent());
        this.countdownId = this.getLastIntent().getIntExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID, -1);
        //maybe by main menu or notification, but we get the same Extra: COUNTDOWN_ID with the ID
        startCountdownOnUI((new InternalCountdownStorageMgr(this).getCountdown(this.countdownId))); //0 is default value

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
        this.getCountdownCounter().getCountdownCounterThread().interrupt(); //stop it
    }

    public Double loadCountdownFromSharedPreferences(int countdownId) {
        return new InternalCountdownStorageMgr(this).getCountdown(countdownId).getTotalSeconds();
    }

    public void startCountdownOnUI(Countdown countdown) {
        if (countdown != null) { //if (-1) was e.g. found as intent countdown id then it will be null
            //search in storage and get total seconds then start countdown (if not found because smaller 0 or deleted and notification referenced it
            this.setCountdownCounter(new CountdownCounter(this, countdown));
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

                //TODO: multiple properties (alpha also so it hides or comes): https://stackoverflow.com/questions/28352352/change-multiple-properties-with-single-objectanimator
                final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(notificationContent, "y", 15); //get it back to positive animated (+5 because we want a small space above shape)
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
                            //TODO: works now, but in future if extreme long texts notification might not get hidden completely (*2)
                            ObjectAnimator objectAnimatorReverse = ObjectAnimator.ofFloat(notificationContent, "y", (hiddenPosition * 2)); //get it back to positive animated (*2 so it is in every case outside of screen [no idea why this might be necessary for long codes]
                            objectAnimatorReverse.setDuration(Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS);
                            objectAnimatorReverse.setStartDelay(globalAppSettingsMgr.getInAppNotificationShowDurationInMs() + Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS); //how long should be notification displayed (adding animation duration because time period delay is inclusive animation)
                            objectAnimatorReverse.start(); //start again
                            Log.d(TAG, "onAnimationEnd: Inapp notification repeated with delay in onAnimationEnd.");
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


    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shareCountdown:
                Log.d(TAG, "onOptionsItemSelected: Trying to share countdown (start procedure).");
                this.refreshShareIntent(); //refresh values
                break;
            default:
                Log.e(TAG, "onOptionsItemSelected: Button does not exist: " + item.getItemId());
        }
        return true;
    }*/
}
