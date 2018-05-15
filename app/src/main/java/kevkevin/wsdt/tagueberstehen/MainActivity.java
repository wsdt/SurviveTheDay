package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import kevkevin.wsdt.tagueberstehen.classes.entities.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.DialogMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppPurchaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.classes.services.LiveCountdown_ForegroundService;

import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_InAppPurchaseMgr.*;
import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_NotificationMgr.*;
import static kevkevin.wsdt.tagueberstehen.interfaces.IConstants_MainActivity.COUNTDOWN_VIEW_TAG_PREFIX;

public class MainActivity extends AppCompatActivity {
    private LinearLayout nodeList;
    private int anzahlShowingNodes = 0;
    private static final String TAG = "MainActivity";
    private InAppPurchaseMgr inAppPurchaseMgr;
    private AdMgr adMgr; //used e.g. for banner ad (so we can dynamically remove it etc.)
    private RelativeLayout mainActivityPage;
    private DialogMgr dialogMgr;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Manager setting up
        this.setInAppPurchaseMgr(new InAppPurchaseMgr(this));
        this.setDialogMgr(new DialogMgr(this));
        //this.setInternalCountdownStorageMgr(new InternalCountdownStorageMgr(this));

        //Initiliaze AdMob
        this.setAdMgr(new AdMgr(this));
        //no fullpage ad because this happens already in loading screen
        this.setMainActivityPage((RelativeLayout) findViewById(R.id.mainActivityPage));
        this.getAdMgr().loadBannerAd(this.getMainActivityPage());
        this.getAdMgr().loadFullPageAd(null, null); //now frequency capping (so real interstitial ad is only 2x every 10 minutes shown when calling main activity)

        //Nodelist
        nodeList = findViewById(R.id.nodeList);

        //Create for each saved countdown one node
        loadAddNodes();


        //IMPORTANT: IF ELSE so NOT BOTH get started !!
        //Start background service is forward compatibility off/false OR startBroadcast Receivers if ON
        new GlobalAppSettingsMgr(this).startBroadcastReceiver();
        //Start foreground service
        startService(new Intent(this, LiveCountdown_ForegroundService.class));

        //Set up onRefresh for pulling down
        initializePullForRefresh();
    }


    @Override
    protected void onRestart() {
        //used onRestart instead of onResume, because maybe better performance (not that often a refresh)
        super.onRestart();
        reloadEverything();
        this.invalidateOptionsMenu(); //invalidate also options menu (no need in reloadEverything, would be too often [only in PurchaseActivity ads will be removed, so restart() is enough])
    }


    private void reloadEverything() { //TODO: use if possible for small changes (like motivation toggle not this method (only change text --> better user experience)
        this.anzahlShowingNodes = 0;
        //reload all nodes from sharedpreferences (remove them beforehand)
        removeAllNodesFromLayout();
        loadAddNodes();
        this.getAdMgr().loadBannerAd(this.getMainActivityPage()); //ad might get removed if settings have changed (in app purchase, temporarly ad free etc.)
        //restart of service happens in InternalCountdownStorageMgr!

        //Stop refreshing
        if (this.getSwipeRefreshLayout() != null) {
            if (this.getSwipeRefreshLayout().isRefreshing()) {
                //Only restart services etc. when user really wants to refresh
                Log.d(TAG, "reloadEverything:isRefreshing: User wants to refresh. So we are also refreshing the services.");
                DatabaseMgr.getSingletonInstance(this).restartNotificationService(this);
                Toast.makeText(this, R.string.mainActivity_swipeRefreshLayout_pulledDown_refreshDone, Toast.LENGTH_SHORT).show();
                this.getSwipeRefreshLayout().setRefreshing(false); //done with loading
            }
        }
    }

    private void loadAddNodes() {
        final SparseArray<Countdown> allCountdowns = DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this, false);
        for (int i = 0, nsize = allCountdowns.size(); i < nsize; i++) {
            final Countdown currCountdown = allCountdowns.valueAt(i); //necessary because i cannot be final (i++)
            if (this.anzahlShowingNodes > 0) {
                //Already at least one node shown! Not showing more without purchasing product
                this.getInAppPurchaseMgr().executeIfProductIsBought(INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "createAddNodeToLayout:isProductBought:is_true: Product is bought. Showing more than one node (if there are any).");
                        createAddNodeToLayout(currCountdown);
                        anzahlShowingNodes++;
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "createAddNodeToLayout:isProductBought:is_false: UseMoreCountdown-Nodes Product not bought! Not displaying more.");
                        Toast.makeText(MainActivity.this, R.string.inAppProduct_notBought_useMoreCountdownNodes, Toast.LENGTH_SHORT).show();
                    }
                });
            } else { //first node
                createAddNodeToLayout(currCountdown);
                this.anzahlShowingNodes++;
            }
        }

        TextView noCountdownsFound = findViewById(R.id.MainActivity_TextView_NoCountdownsFound);
        if (this.anzahlShowingNodes <= 0) {
            //add plus icon or similar to add new countdown
            noCountdownsFound.setVisibility(View.VISIBLE);
        } else {
            //if not then remove it from layout if already shown!!
            Log.d(TAG, "loadAddNodes: NoCountdownsFound-Textview found, removing it because nodes are found.");
            //do not use view.gone (if you do then verify when referencing this view that findView is NOT null (nullpointer exception)
            noCountdownsFound.setVisibility(View.INVISIBLE);
        }
    }

    //Pull to refresh view - SwipeREFRESHLayout
    private void initializePullForRefresh() {
        setSwipeRefreshLayout((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout));
        getSwipeRefreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefreshListener: Calling reloadEverything().");
                reloadEverything(); //IMPORTANT: Do not forget to call setRefreshing(false) to stop it
            }
        });
        getSwipeRefreshLayout().setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright
        );
    }


    //SWIPELAYOUT - ONCLICK METHODS ###################################################################
    public void onClick_node_sl_instruction(View v) {
        Toast.makeText(this, R.string.mainActivity_swipeLayout_onClickInstruction, Toast.LENGTH_SHORT).show();
    }

    public void onClick_node_sl_bottomview_leftMenu_openCountdown(View v) {
        Intent tmpintent = new Intent(this, CountdownActivity.class);
        int countdownId;
        try {
            countdownId = getCountdownIdFromNodeTag((SwipeLayout) v.getParent());
        } catch (ClassCastException e) {
            Log.e(TAG, "onClick_node_sl_bottomview_leftMenu_openCountdown: Could not cast parent view to SwipeLayout. Maybe it's not a node.");
            return;
        }

        //Open CountdownActivity
        if (countdownId >= 0) {
            tmpintent.putExtra(IDENTIFIER_COUNTDOWN_ID, countdownId);
            startActivity(tmpintent);
        } else {
            //Toast was made in getCountdownIdFromNodeTag()
            Log.e(TAG, "onClick_node_sl_bottomview_leftMenu_openCountdown: An error occured in getCountdownIdFromNodeTag(). Did not open next activity.");
        }
    }

    public void onClick_node_sl_bottomview_rightMenu_deleteNode(final View v) {
        //Get countdownId of corresponding node to perform actions
        final Countdown countdown = getCountdownFromNode(v);
        if (countdown == null) {
            Log.e(TAG, "onClick_node_sl_bottomview_rightMenu_deleteNode: Countdown obj is null!");
            return;
        }

        //create dialog to ask user whether he wants really delete countdown
        this.getDialogMgr().showDialog_Generic(
                getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_title),
                getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_description),
                getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_yesDelete),
                getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_noCancel),
                R.drawable.light_delete, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true() {
                        Log.d(TAG, "onClick_node_sl_bottomview_rightMenu_deleteNode:success_is_true: User clicked on delete. Trying to erase countdown.");
                        DatabaseMgr.getSingletonInstance(MainActivity.this).deleteCountdown(MainActivity.this, (int) countdown.getCouId());
                        Toast.makeText(MainActivity.this, R.string.mainActivity_deletedCountdown, Toast.LENGTH_SHORT).show();
                        try { //for better performance try removing node from view without reloading other nodes
                            ((SwipeLayout) v.getParent().getParent()).setVisibility(View.GONE);
                            if ((--anzahlShowingNodes) <= 0) {
                                Log.d(TAG, "onClick_node_sl_bottomview_rightMenu_deleteNode: After deleting node, no countdowns to show, showing nothing found msg.");
                                findViewById(R.id.MainActivity_TextView_NoCountdownsFound).setVisibility(View.VISIBLE); //show that no countdowns active
                            }
                            Log.d(TAG, "onClick_node_sl_bottomview_rightMenu_deleteNode: Tried to remove view without reloading other nodes (better performance).");
                        } catch (ClassCastException | NullPointerException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onClick_node_sl_bottomview_rightMenu_deleteNode: Could not remove deleted node from activity. Reloaded instead everything.");
                            reloadEverything(); //reload everything to remove countdown from nodelist in case try failed
                        }
                    }

                    @Override
                    public void failure_is_false() {
                        Log.d(TAG, "onClick_node_sl_bottomview_rightMenu_deleteNode:failure_is_false: Countdown won't be deleted.");
                    }
                });
    }

    public void onClick_node_sl_bottomview_rightMenu_editNode(View v) {
        //Get countdownId of corresponding node to perform actions
        Countdown countdown = getCountdownFromNode(v);
        if (countdown == null) {
            Log.e(TAG, "onClick_node_sl_bottomview_rightMenu_editNode: Countdown obj is null!");
            return;
        }

        Intent modifyCountdownActivity = new Intent(this, ModifyCountdownActivity.class);
        modifyCountdownActivity.putExtra(IDENTIFIER_COUNTDOWN_ID, countdown.getCouId());
        startActivity(modifyCountdownActivity);
    }

    public void onClick_node_sl_bottomview_rightMenu_toggleMotivationNode(View v) {
        //Get countdownId of corresponding node to perform actions
        Countdown countdown = getCountdownFromNode(v);
        if (countdown == null) {
            Log.e(TAG, "onClick_node_sl_bottomview_rightMenu_toggleMotivationNode: Countdown obj is null!");
            return;
        }

        if (countdown.isCouIsMotivationOn()) {
            countdown.setCouIsMotivationOn(false);
            Log.d(TAG, "onCountdownModifyButtons: Countdown " + countdown.getCouId() + " does not motivate now.");
        } else {
            countdown.setCouIsMotivationOn(true);
            Log.d(TAG, "onCountdownModifyButtons: Countdown " + countdown.getCouId() + " does motivate now.");
        }
        countdown.savePersistently();
        try {
            //try to refresh msg without reloading all nodes (better performance), otherwise if error reloadeverything in catch
            countdown.getEventMsgOrAndSetView((LinearLayout) ((SwipeLayout) v.getParent().getParent()).findViewById(R.id.node_countdown).findViewById(R.id.countdownEventMsg_ll));
            Log.d(TAG, "onClick_node_sl_bottomview_rightMenu_toggleMotivationNode: Tried to change eventMsg without reloading all nodes (better performance).");
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "onClick_node_sl_bottomview_rightMenu_toggleMotivationNode: Could not change eventMsg. Reloading all nodes.");
            reloadEverything(); //reloadEverything because we have now eventMessages on node which might be different now!
        }
        Toast.makeText(this, String.format(getResources().getString(R.string.mainActivity_countdownMotivationToggleOnOff), ((countdown.isCouIsMotivationOn()) ? getResources().getString(R.string.mainActivity_countdownMotivationToggleOnOff_activated) : getResources().getString(R.string.mainActivity_countdownMotivationToggleOnOff_deactivated))), Toast.LENGTH_SHORT).show();
    }

    private Countdown getCountdownFromNode(View v) { //needs to be Swipelayout!
        Countdown countdown;
        try {
            countdown = DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this, false).get(getCountdownIdFromNodeTag((SwipeLayout) v.getParent().getParent())); //2 getparent() because right Menu consists of more buttons
        } catch (ClassCastException e) {
            Log.e(TAG, "onClick_node_sl_bottomview_rightMenu_editNode: Could not cast parent view to SwipeLayout. Maybe it is not a node.");
            return null;
        }
        return countdown;
    }

    private int getCountdownIdFromNodeTag(@NonNull SwipeLayout v) { //used from onCountdownModifyButtons and onClick()
        String nodeTag = (String) v.getTag(); //COUNTDOWN_N  --> N = CountdownActivity ID
        Log.d(TAG, "getCountdownIdFromNodeTag: Nodetag of countdown is: " + ((nodeTag == null) ? "null" : nodeTag));
        int nodeId = (-1);
        try {
            if (nodeTag.length() >= (COUNTDOWN_VIEW_TAG_PREFIX.length()+1)) { //+1 so we know that at least a no. between 0 and 9 is given
                try {
                    nodeId = Integer.parseInt(nodeTag.substring(COUNTDOWN_VIEW_TAG_PREFIX.length()));
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
        //node_template's superior tag is now a swipelayout from that we can extract the relativelayout by [2]
        SwipeLayout swipeLayout = (SwipeLayout) getLayoutInflater().inflate(R.layout.node_template, (LinearLayout) findViewById(R.id.nodeList), false); //give relativelayout so layoutparams get done

        //[2]: extract old relativelayout from swipelayout
        RelativeLayout countdownView = swipeLayout.findViewById(R.id.node_countdown);
        ((TextView) countdownView.findViewById(R.id.couTitle)).setText(countdown.getCouTitle());
        ((TextView) countdownView.findViewById(R.id.couDescription)).setText(countdown.getCouDescription());
        //set event msg to view (with icon, color etc. in method automatically)
        countdown.getEventMsgOrAndSetView(((LinearLayout) countdownView.findViewById(R.id.countdownEventMsg_ll))); //sets text etc. (works because of reference)

        //((TextView) countdownView.findViewById(R.id.startAndUntilDateTime)).setText(String.format(getResources().getString(R.string.mainActivity_countdownNode_DateTimeValues), countdown.getCouStartDateTime(), countdown.getCouUntilDateTime()));
        //Set tag to swipeLayout! so we can access it from every top/right menu etc.
        swipeLayout.setTag(COUNTDOWN_VIEW_TAG_PREFIX + countdown.getCouId()); //IMPORTANT: to determine what countdown to open in CountdownActivity

        //set category color
        View categoryColor = countdownView.findViewById(R.id.categoryColorView);
        categoryColor.setBackgroundColor(Color.parseColor(countdown.getCouCategoryColor()));

        //expand categorycolor to whole height of node (because of wrap content) --> HAS TO BE AFTER SETTEXT (because they change the size of the view)
        countdownView.measure(countdownView.getLayoutParams().width, countdownView.getLayoutParams().height); //remeasure because of settext etc.
        //remain old width with own layoutparam.width and set new height with new measured parent height (-25 because of padding top/bottom in sum)
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(categoryColor.getLayoutParams().width, countdownView.getMeasuredHeight() - 25); //set height of categorycolor view to same as relativelayout (not wrap content or match parent!)
        Log.d(TAG, "createAddNodeToLayout: CategoryColorView: Future height -> " + countdownView.getMeasuredHeight());
        categoryColor.setLayoutParams(layoutParams);

        //Swipe layout configuration (node menu) -------------------------------
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown); //set show mode

        //Add drag edge (If BottomView has "layout_gravity" attribute, this line is unnecessary
        //IMPORTANT: findViewById mit vorangestellter SwipeLayout Instanz (damit mehrere Nodes möglich [error was: spec. Child already has a parent])
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.node_sl_bottomview_rightMenu));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.node_sl_bottomview_leftMenu));
        //When superior layout is scrollable the swipelayout is not very useful because scrollview reacts

        //SwipeLayout listener
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        nodeList.addView(swipeLayout);
        Log.d(TAG, "createAddNodeToLayout: Crafted countdown as node: " + countdownView.getTag());
    }

    private void removeAllNodesFromLayout() {
        if (nodeList == null) {
            this.nodeList = findViewById(R.id.nodeList);
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
        this.getInAppPurchaseMgr().executeIfProductIsBought(INAPP_PRODUCTS.REMOVE_ALL_ADS.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
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
                this.getDialogMgr().showDialog_Generic(
                        getResources().getString(R.string.mainActivity_countdownNode_deleteAll_warningDialog_title),
                        getResources().getString(R.string.mainActivity_countdownNode_deleteAll_warningDialog_description),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_yesDelete),
                        getResources().getString(R.string.mainActivity_countdownNode_delete_warningDialog_noCancel),
                        R.drawable.light_delete, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                            @Override
                            public void success_is_true() {
                                Log.d(TAG, "onOptionsItemSelected:removeAllCountdowns:success_is_true: User clicked on delete. Trying to erase all countdowns.");
                                DatabaseMgr.getSingletonInstance(MainActivity.this).deleteAllCountdowns(MainActivity.this);
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

    public InAppPurchaseMgr getInAppPurchaseMgr() {
        return this.inAppPurchaseMgr;
    }

    public void setInAppPurchaseMgr(InAppPurchaseMgr inAppPurchaseMgr) {
        this.inAppPurchaseMgr = inAppPurchaseMgr;
    }

    public AdMgr getAdMgr() {
        return adMgr;
    }

    public void setAdMgr(AdMgr adMgr) {
        this.adMgr = adMgr;
    }

    public RelativeLayout getMainActivityPage() {
        return mainActivityPage;
    }

    public void setMainActivityPage(RelativeLayout mainActivityPage) {
        this.mainActivityPage = mainActivityPage;
    }

    public DialogMgr getDialogMgr() {
        return dialogMgr;
    }

    public void setDialogMgr(DialogMgr dialogMgr) {
        this.dialogMgr = dialogMgr;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }
}
