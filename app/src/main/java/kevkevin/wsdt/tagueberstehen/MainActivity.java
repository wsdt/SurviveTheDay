package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e("CountdownList","Actionbar could not be hidden, because already null!");
        }

        // CREATE NODE LIST -------------------------------------------------------------------
        LinearLayout nodeList = (LinearLayout) findViewById(R.id.nodeList);

        //Node 0
        RelativeLayout countdown0 = (RelativeLayout) getLayoutInflater().inflate(R.layout.node_template,null);
        nodeList.addView(countdown0);
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
}
