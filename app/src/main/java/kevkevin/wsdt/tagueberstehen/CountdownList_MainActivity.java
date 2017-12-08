package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class CountdownList_MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_list__main);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e("CountdownList","Actionbar could not be hidden, because already null!");
        }

        LinearLayout nodeList = (LinearLayout) findViewById(R.id.nodeList);
        /*Node countdown0 = new Node(this);
        countdown0.setOnClickListener(this);
        nodeList.addView(countdown0);
*/
        RelativeLayout countdown0 = (RelativeLayout) getLayoutInflater().inflate(R.layout.node_template,null);
        nodeList.addView(countdown0);

    }

    @Override
    public void onClick(View v) {
        String nodeTag = (String) v.getTag(); //COUNTDOWN_N  --> N = Countdown ID
        if (nodeTag.length() >= 11) {
            try {
                int nodeId = Integer.parseInt(nodeTag.substring(10));
                Intent tmpintent = new Intent(this,Countdown.class);
                tmpintent.putExtra("COUNTDOWN_ID",nodeId);
                startActivity(tmpintent);
            } catch (NumberFormatException e) {
                Log.e("onNodeClick","Node-Id could not be parsed to Integer. Wrong Tag: "+nodeTag);
                Toast.makeText(this, "Could not identify Countdown ID.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("onNodeClick","Node-Tag WRONG labelled: "+nodeTag);
            Toast.makeText(this,"Could not open Countdown.",Toast.LENGTH_SHORT).show();
        }
    }
}
