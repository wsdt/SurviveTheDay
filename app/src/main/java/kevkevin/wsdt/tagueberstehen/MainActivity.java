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

import kevkevin.wsdt.tagueberstehen.classes.Countdown;
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

        // CREATE NODE LIST -------------------------------------------------------------------
        //Create for each saved countdown one node
        InternalStorageMgr storageMgr = new InternalStorageMgr(this);
        int anzahlCountdowns = 0;
        for (Countdown countdown : storageMgr.getAllCountdowns(false)) {
            createAddNodeToLayout(countdown);
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

        // CREATE NODE LIST - END -------------------------------------------------------------
    }

    @Override
    public void onClick(View v) {
        String nodeTag = (String) v.getTag(); //COUNTDOWN_N  --> N = CountdownActivity ID
        if (nodeTag.length() >= 11) {
            try {
                int nodeId = Integer.parseInt(nodeTag.substring(10));
                Intent tmpintent = new Intent(this,CountdownActivity.class);
                tmpintent.putExtra("COUNTDOWN_ID",nodeId);
                startActivity(tmpintent);
            } catch (NumberFormatException e) {
                Log.e("onNodeClick","Node-Id could not be parsed to Integer. Wrong Tag: "+nodeTag);
                Toast.makeText(this, "Could not identify CountdownActivity ID.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("onNodeClick","Node-Tag WRONG labelled: "+nodeTag);
            Toast.makeText(this,"Could not open CountdownActivity.",Toast.LENGTH_SHORT).show();
        }
    }

    private void createAddNodeToLayout(Countdown countdown) {
        RelativeLayout countdownView = (RelativeLayout) getLayoutInflater().inflate(R.layout.node_template,null);
        ((TextView)countdownView.findViewById(R.id.countdownTitle)).setText(countdown.getCountdownTitle());
        ((TextView)countdownView.findViewById(R.id.untilDateTime)).setText(countdown.getUntilDateTime());
        nodeList.addView(countdownView);
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
            default: Log.e(TAG,"onOptionsItemSelected: Button does not exist: "+item.getItemId());
        }
        return true;
    }
}
