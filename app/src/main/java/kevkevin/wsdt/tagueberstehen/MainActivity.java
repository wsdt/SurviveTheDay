package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.DialogManager;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalCountdownStorageMgr;
import kevkevin.wsdt.tagueberstehen.classes.services.CountdownCounterService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout nodeList;
    private static final String TAG = "MainActivity";
    private InAppPurchaseManager inAppPurchaseManager;
    private AdManager adManager; //used e.g. for banner ad (so we can dynamically remove it etc.)
    private RelativeLayout mainActivityPage;
    private DialogManager dialogManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //InAppPurchaseMgr
        this.setInAppPurchaseManager(new InAppPurchaseManager(this));

        //Dialog Manager (deleting e.g.)
        this.setDialogManager(new DialogManager(this));

        //Initiliaze AdMob
        this.setAdManager(new AdManager(this));
        this.getAdManager().initializeAdmob(); //no fullpage ad because this happens already in loading screen
        this.setMainActivityPage((RelativeLayout) findViewById(R.id.mainActivityPage));
        this.getAdManager().loadBannerAd(this.getMainActivityPage());

        //Nodelist
        nodeList = (LinearLayout) findViewById(R.id.nodeList);

        //Create for each saved countdown one node
        loadAddNodes();


        //IMPORTANT: IF ELSE so NOT BOTH get started !!
        //Start background service is forward compatibility off/false OR startBroadcast Receivers if ON
        GlobalAppSettingsMgr globalAppSettingsMgr = new GlobalAppSettingsMgr(this);
        globalAppSettingsMgr.startBroadcastORBackgroundService();
        //Start foreground service
        startService(new Intent(this, CountdownCounterService.class));
    }


    @Override
    protected void onRestart() {
        //used onRestart instead of onResume, because maybe better performance (not that often a refresh)
        super.onRestart();
        reloadEverything();
        this.invalidateOptionsMenu(); //invalidate also options menu (no need in reloadEverything, would be too often [only in PurchaseActivity ads will be removed, so restart() is enough])
    }

    private void reloadEverything() {
        //reload all nodes from sharedpreferences (remove them beforehand)
        removeAllNodesFromLayout();
        loadAddNodes();
        this.getAdManager().loadBannerAd(this.getMainActivityPage()); //ad might get removed if settings have changed (in app purchase, temporarly ad free etc.)
        //restart of service happens in InternalCountdownStorageMgr!
    }

    private void loadAddNodes() {
        InternalCountdownStorageMgr storageMgr = new InternalCountdownStorageMgr(this);
        int anzahlCountdowns = 0;
        for (final Map.Entry<Integer, Countdown> countdown : storageMgr.getAllCountdowns(false, false).entrySet()) {
            if (anzahlCountdowns > 0) {
                //Already at least one node shown! Not showing more without purchasing product
                this.getInAppPurchaseManager().executeIfProductIsBought(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "createAddNodeToLayout:isProductBought:is_true: Product is bought. Showing more than one node (if there are any).");
                        createAddNodeToLayout(countdown.getValue());
                        //incrementation of anzahlCountdowns not necessary because only used for == 0 (No Countdowns found) or > 0 (is inapp product bought) because already incremented always 1 and so bigger than 0
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "createAddNodeToLayout:isProductBought:is_false: UseMoreCountdown-Nodes Product not bought! Not displaying more.");
                        Toast.makeText(MainActivity.this, R.string.inAppProduct_notBought_useMoreCountdownNodes, Toast.LENGTH_SHORT).show();
                    }
                });
            } else { //first node
                createAddNodeToLayout(countdown.getValue());
                anzahlCountdowns++;
            }
        }


        TextView noCountdownsFound = (TextView) findViewById(R.id.MainActivity_TextView_NoCountdownsFound);
        if (anzahlCountdowns <= 0) {
            //add plus icon or similar to add new countdown
            noCountdownsFound.setVisibility(View.VISIBLE);
        } else {
            //if not then remove it from layout if already shown!!
            Log.d(TAG, "loadAddNodes: NoCountdownsFound-Textview found, removing it because nodes are found.");
            //do not use view.gone (if you do then verify when referencing this view that findView is NOT null (nullpointer exception)
            noCountdownsFound.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) { //OnClick on node itself
        Intent tmpintent = new Intent(this, CountdownActivity.class);
        int countdownId = getCountdownIdFromNodeTag((RelativeLayout) v);

        if (countdownId >= 0) {
            tmpintent.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID, countdownId);
            startActivity(tmpintent);
        } else {
            //Toast was made in getCountdownIdFromNodeTag()
            Log.e(TAG, "onClick: An error occured in getCountdownIdFromNodeTag(). Did not open next activity.");
        }
    }

    public void onCountdownModifyButtons(View v) { //when clicked on a node buttons (not node itself)
        //Get countdownId of corresponding node to perform actions
        final InternalCountdownStorageMgr storageMgr = new InternalCountdownStorageMgr(this);
        final Countdown countdown;
        try {
            countdown = storageMgr.getCountdown(getCountdownIdFromNodeTag((RelativeLayout) v.getParent()));
        } catch (ClassCastException e) {
            Log.e(TAG, "Could not cast parent view to RelativeLayout. Maybe it is not a node.");
            return;
        }

        if (countdown == null) {
            Log.e(TAG, "onCountdownModifyButtons: Countdown Obj is null!");
            return;
        } //exist if null (when other exception occured)
        switch (v.getId()) {
            case R.id.countdownMotivateMeToggle:
                if (countdown.isActive()) {
                    countdown.setActive(false);
                    Log.d(TAG, "onCountdownModifyButtons: Countdown " + countdown.getCountdownId() + " does not motivate now.");
                } else {
                    countdown.setActive(true);
                    Log.d(TAG, "onCountdownModifyButtons: Countdown " + countdown.getCountdownId() + " does motivate now.");
                }
                countdown.savePersistently();
                Resources res = getResources();
                Toast.makeText(this, String.format(res.getString(R.string.mainActivity_countdownMotivationToggleOnOff), ((countdown.isActive()) ? res.getString(R.string.mainActivity_countdownMotivationToggleOnOff_activated) : res.getString(R.string.mainActivity_countdownMotivationToggleOnOff_deactivated))), Toast.LENGTH_SHORT).show();
                break;
            case R.id.countdownEdit:
                Intent modifyCountdownActivity = new Intent(this, ModifyCountdownActivity.class);
                modifyCountdownActivity.putExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID, countdown.getCountdownId());
                startActivity(modifyCountdownActivity);
                break;
            case R.id.countdownDelete:
                //create dialog to ask user whether he wants really delete countdown
                this.getDialogManager().showDialog_Generic(
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_title),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_description),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_yesDelete),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_noCancel),
                        R.drawable.dark_delete_small, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                            @Override
                            public void success_is_true() {
                                Log.d(TAG, "onCountdownModifyButtons:countdownDelete:success_is_true: User clicked on delete. Trying to erase countdown.");
                                storageMgr.deleteCountdown(countdown.getCountdownId());
                                Toast.makeText(MainActivity.this, R.string.mainActivity_deletedCountdown, Toast.LENGTH_SHORT).show();
                                reloadEverything(); //reload everything to remove countdown from nodelist
                            }

                            @Override
                            public void failure_is_false() {
                                Log.d(TAG, "onCountdownModifyButtons:countdownDelete:failure_is_false: Countdown won't be deleted.");
                            }
                        });
                break;
            default:
                Log.e(TAG, "onCountdownModifyButtons: Option does not exist: " + v.getId());
        }
    }

    private int getCountdownIdFromNodeTag(@NonNull RelativeLayout v) { //used from onCountdownModifyButtons and onClick()
        String nodeTag = (String) v.getTag(); //COUNTDOWN_N  --> N = CountdownActivity ID
        Log.d(TAG, "getCountdownIdFromNodeTag: Nodetag of countdown is: " + ((nodeTag == null) ? "null" : nodeTag));
        int nodeId = (-1);
        try {
            if (nodeTag.length() >= 11) {
                try {
                    nodeId = Integer.parseInt(nodeTag.substring(10));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "getCountdownIdFromNodeTag: Node-Id could not be parsed to Integer. Wrong Tag: " + nodeTag);
                    Toast.makeText(this, "Could not identify Countdown ID.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "getCountdownIdFromNodeTag: Node-Tag WRONG labelled: " + nodeTag);
                Toast.makeText(this, R.string.mainActivity_countdownNode_error_nodeTagWrongBuild, Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, R.string.error_contactAdministrator, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "getCountdownIdFromNodeTag: Nullpointerexception (presumably nodeTag == null!).");
            e.printStackTrace();
        }
        return nodeId;
    }

    private void createAddNodeToLayout(Countdown countdown) {
        RelativeLayout countdownView = (RelativeLayout) getLayoutInflater().inflate(R.layout.node_template, (LinearLayout) findViewById(R.id.nodeList), false); //give relativelayout so layoutparams get done
        ((TextView) countdownView.findViewById(R.id.countdownTitle)).setText(countdown.getCountdownTitle());
        ((TextView) countdownView.findViewById(R.id.countdownDescription)).setText(countdown.getCountdownDescription());
        ((TextView) countdownView.findViewById(R.id.startAndUntilDateTime)).setText(String.format(getResources().getString(R.string.mainActivity_countdownNode_DateTimeValues), countdown.getStartDateTime(), countdown.getUntilDateTime()));
        countdownView.setTag(Constants.MAIN_ACTIVITY.COUNTDOWN_VIEW_TAG_PREFIX + countdown.getCountdownId()); //IMPORTANT: to determine what countdown to open in CountdownActivity

        //set category color, if not valid or other then overwrite it with default color and save that countdown so this error will not happen again
        try {
            (countdownView.findViewById(R.id.categoryColorView)).setBackgroundColor(Color.parseColor(countdown.getCategory()));
        } catch (Exception e) {
            Log.e(TAG, "createAddNodeToLayout: ParseException by defining color! Selected default color and saved it into countdown.");
            //Set default color
            (countdownView.findViewById(R.id.categoryColorView)).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            //save into countdown itself
            countdown.setCategory("#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
            countdown.savePersistently();
            Toast.makeText(this, R.string.mainActivity_countdownNode_error_categoryColorWrongRetained, Toast.LENGTH_SHORT).show();
        }
        nodeList.addView(countdownView);
        Log.d(TAG, "createAddNodeToLayout: Added countdown as node to layout: " + countdownView.getTag());

    }

    private void removeAllNodesFromLayout() {
        if (nodeList == null) {
            this.nodeList = (LinearLayout) findViewById(R.id.nodeList);
        }
        nodeList.removeAllViews();
    }

    // ACTION BAR ------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //Remove menu points dynamically (not in onCreateOptionsMenu)
        this.getInAppPurchaseManager().executeIfProductIsBought(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.REMOVE_ALL_ADS.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                MenuItem rewardedAdMenuItem = menu.findItem(R.id.action_removeAdsTemporary);
                if (rewardedAdMenuItem != null) {
                    Log.d(TAG, "onPrepareOptionsMenu: Tried to hide rewarded Ad menu point, because all ads are already hidden because of buying package.");
                    rewardedAdMenuItem.setVisible(false);
                } else {
                    Log.e(TAG, "onPrepareOptionsMenu: Could not hide menu point, because menu item was not found!");
                }
            }

            @Override
            public void failure_is_false() {
                Log.d(TAG, "onPrepareOptionsMenu: Not hiding rewardedAd menu button, because failure happened or product not bought!");
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addCountdown:
                startActivity(new Intent(this, ModifyCountdownActivity.class));
                break;
            case R.id.action_removeAllCountdowns:
                this.getDialogManager().showDialog_Generic(
                        getResources().getString(R.string.mainActivity_countdownNode_deleteAll_warningDialog_title),
                        getResources().getString(R.string.mainActivity_countdownNode_deleteAll_warningDialog_description),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_yesDelete),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_noCancel),
                        R.drawable.light_delete_big, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                            @Override
                            public void success_is_true() {
                                Log.d(TAG, "onOptionsItemSelected:removeAllCountdowns:success_is_true: User clicked on delete. Trying to erase all countdowns.");
                                InternalCountdownStorageMgr storageMgr = new InternalCountdownStorageMgr(MainActivity.this);
                                storageMgr.deleteAllCountdowns();
                                reloadEverything(); //because onResume gets not called
                                Toast.makeText(MainActivity.this, R.string.mainActivity_deletedAllCountdowns, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void failure_is_false() {
                                Log.d(TAG, "onOptionsItemSelected:removeAllCountdowns:failure_is_false: Countdowns won't be deleted.");
                            }
                        });
                break;
            case R.id.action_showInAppProducts:
                Log.d(TAG, "onOptionsItemSelected: Tried to start InAppPurchaseActivity.");
                startActivity(new Intent(this, InAppPurchaseActivity.class));
                break;
            case R.id.action_removeAdsTemporary:
                Log.d(TAG, "onOptionsItemSelected: Tried to startRewardedAdActivity.");
                startActivity(new Intent(this, RewardedVideoAdActivity.class));
                break;
            case R.id.action_credits:
                Log.d(TAG, "onOptionsItemSelected: Tried to open CreditsActivity.");
                startActivity(new Intent(this, CreditsActivity.class));
                break;
            case R.id.action_settings:
                Log.d(TAG, "onOptionsItemSelected: Tried to open SettingsActivity.");
                startActivity(new Intent(this, AppSettingsActivity.class));
                break;
            default:
                Log.e(TAG, "onOptionsItemSelected: Button does not exist: " + item.getItemId());
        }
        return true;
    }

    public InAppPurchaseManager getInAppPurchaseManager() {
        return this.inAppPurchaseManager;
    }

    public void setInAppPurchaseManager(InAppPurchaseManager inAppPurchaseManager) {
        this.inAppPurchaseManager = inAppPurchaseManager;
    }

    public AdManager getAdManager() {
        return adManager;
    }

    public void setAdManager(AdManager adManager) {
        this.adManager = adManager;
    }

    public RelativeLayout getMainActivityPage() {
        return mainActivityPage;
    }

    public void setMainActivityPage(RelativeLayout mainActivityPage) {
        this.mainActivityPage = mainActivityPage;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public void setDialogManager(DialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }
}
