package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.graphics.Color;
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

import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.NotificationService;
import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.InternalStorageMgr;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout nodeList;
    private static final String TAG = "MainActivity";

    /*
    TODO: Vulgaritätsschieberegler (umso höher desto vulgärer oder ausfälliger Sprüche
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Nodelist
        nodeList = (LinearLayout) findViewById(R.id.nodeList);

        //Create for each saved countdown one node
        loadAddNodes();

        //Start Service
        startService(new Intent(this,NotificationService.class)); //this line should be only called once
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadEverything();
    }

    private void reloadEverything() {
        //reload all nodes from sharedpreferences (remove them beforehand)
        removeAllNodesFromLayout();
        loadAddNodes();
        //restart of service happens in InternalStorageMgr!
    }

    private void loadAddNodes() {
        InternalStorageMgr storageMgr = new InternalStorageMgr(this);
        int anzahlCountdowns = 0;
        for (Map.Entry<Integer,Countdown> countdown : storageMgr.getAllCountdowns(false).entrySet()) {
            createAddNodeToLayout(countdown.getValue());
            anzahlCountdowns++;
        }

        if (anzahlCountdowns <= 0) {
            //add plus icon or similar to add new countdown
            TextView noCountdownsFound = new TextView(this);
            noCountdownsFound.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            noCountdownsFound.setText("No countdowns found.");
            noCountdownsFound.setTextSize(20);
            noCountdownsFound.setTextColor(Color.WHITE);
            noCountdownsFound.setGravity(Gravity.CENTER);
            nodeList.addView(noCountdownsFound);
        }
    }

    @Override
    public void onClick(View v) { //OnClick on node itself
        Intent tmpintent = new Intent(this,CountdownActivity.class);
        int countdownId = getCountdownIdFromNodeTag(v);

        if (countdownId >= 0) {
            tmpintent.putExtra("COUNTDOWN_ID", countdownId);
            startActivity(tmpintent);
        } else {
            //Toast was made in getCountdownIdFromNodeTag()
            Log.e(TAG, "onClick: An error occured in getCountdownIdFromNodeTag(). Did not open next activity.");
        }
    }

    public void onCountdownModifyButtons(View v) { //when clicked on a node buttons (not node itself)
        //Get countdownId of corresponding node to perform actions
        InternalStorageMgr storageMgr = new InternalStorageMgr(this);
        Countdown countdown;
        try {
            countdown = storageMgr.getCountdown(getCountdownIdFromNodeTag((ViewGroup) v.getParent()));
        } catch (ClassCastException e) {
            Log.e(TAG, "Could not cast parent view to view.");
            return;
        }

        if (countdown == null) {Log.e(TAG,"onCountdownModifyButtons: Countdown Obj is null!");return;} //exist if null (when other exception occured)
        switch (v.getId()) {
            case R.id.countdownMotivateMeToggle:
                if (countdown.isActive()) {
                    countdown.setActive(false);
                    Log.d(TAG, "onCountdownModifyButtons: Countdown "+countdown.getCountdownId()+" does not motivate now.");
                } else {
                    countdown.setActive(true);
                    Log.d(TAG, "onCountdownModifyButtons: Countdown "+countdown.getCountdownId()+" does motivate now.");
                }
                countdown.savePersistently();
                Toast.makeText(this,"Countdown motivation got "+((countdown.isActive()) ? "activated" : "deactivated")+".",Toast.LENGTH_SHORT).show();
                break;
            case R.id.countdownEdit:
                Intent modifyCountdownActivity = new Intent(this, ModifyCountdownActivity.class);
                modifyCountdownActivity.putExtra("COUNTDOWN_ID",countdown.getCountdownId());
                startActivity(modifyCountdownActivity);
                break;
            case R.id.countdownDelete:
                storageMgr.deleteCountdown(countdown.getCountdownId());
                Toast.makeText(this,"Deleted countdown.",Toast.LENGTH_SHORT).show();
                reloadEverything(); //reload everything to remove countdown from nodelist
                break;
            default: Log.e(TAG, "onCountdownModifyButtons: Option does not exist: "+v.getId());
        }
    }

    private int getCountdownIdFromNodeTag(View v) { //used from onCountdownModifyButtons and onClick()
        String nodeTag = (String) v.getTag(); //COUNTDOWN_N  --> N = CountdownActivity ID
        int nodeId = (-1);
        if (nodeTag.length() >= 11) {
            try {
                nodeId = Integer.parseInt(nodeTag.substring(10));
            } catch (NumberFormatException e) {
                Log.e(TAG,"getCountdownIdFromNodeTag: Node-Id could not be parsed to Integer. Wrong Tag: "+nodeTag);
                Toast.makeText(this, "Could not identify Countdown ID.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Log.e(TAG,"getCountdownIdFromNodeTag: Node-Tag WRONG labelled: "+nodeTag);
            Toast.makeText(this,"Could not perform action. Node maybe wrong build.",Toast.LENGTH_SHORT).show();
        }
        return nodeId;
    }

    private void createAddNodeToLayout(Countdown countdown) {
        RelativeLayout countdownView = (RelativeLayout) getLayoutInflater().inflate(R.layout.node_template,null);
        ((TextView)countdownView.findViewById(R.id.countdownTitle)).setText(countdown.getCountdownTitle());
        ((TextView)countdownView.findViewById(R.id.untilDateTime)).setText(countdown.getUntilDateTime());
        countdownView.setTag("COUNTDOWN_"+countdown.getCountdownId()); //to determine what countdown to open in CountdownActivity
        nodeList.addView(countdownView);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_addCountdown:
                Intent createCountdownAct = new Intent(this,ModifyCountdownActivity.class);
                createCountdownAct.putExtra("CRUD","C");
                startActivity(createCountdownAct);
                break;
            case R.id.action_removeAllCountdowns:
                InternalStorageMgr storageMgr = new InternalStorageMgr(this);
                storageMgr.deleteAllCountdowns();
                reloadEverything(); //because onResume gets not called
                Toast.makeText(this,"Deleted all countdowns.",Toast.LENGTH_LONG).show();
                break;
            case R.id.action_credits:
                Log.d(TAG, "onOptionsItemSelected: Tried to open CreditsActivity.");
                startActivity(new Intent(this, CreditsActivity.class));
                break;
            default: Log.e(TAG,"onOptionsItemSelected: Button does not exist: "+item.getItemId());
        }
        return true;
    }
}
