package kevkevin.wsdt.tagueberstehen;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Node extends RelativeLayout {
    private int android_id;
    private String android_tag;
    private String android_text;


    public Node(Context context) {
        super(context);
        Log.d("Node","Started creating Node.");

        /*LayoutParams tmp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmp.setMargins(5,5,5,5);
        this.setLayoutParams(tmp);

        //Create Node content
        //Create Heading --------------------------------
        TextView countdownHeading = new TextView(this.getContext(), null, R.style.text); //context saved in that relative layout
        LayoutParams tmpparams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmpparams1.setMargins(5,5,5,5);
        countdownHeading.setLayoutParams(tmpparams1);
        countdownHeading.setText(this.getAndroid_text());
        this.addView(countdownHeading);

        //Create Imagebuttons
        ImageButton stopStartToggle = new ImageButton(this.getContext());
        LayoutParams tmpparams2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmpparams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        stopStartToggle.setLayoutParams(tmpparams2);
        this.addView(stopStartToggle);

        //Create Imagebuttons
        ImageButton countdownMuteMotivateToggle = new ImageButton(this.getContext());
        LayoutParams tmpparams3 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmpparams3.setMargins(0,0,20,0);
        tmpparams3.addRule(ALIGN_PARENT_RIGHT);
        countdownMuteMotivateToggle.setLayoutParams(tmpparams3);
        this.addView(countdownMuteMotivateToggle);

        //Create Imagebuttons
        ImageButton countdownEdit = new ImageButton(this.getContext());
        LayoutParams tmpparams4 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmpparams4.setMargins(0,0,40,0);
        tmpparams4.addRule(ALIGN_PARENT_RIGHT);
        countdownEdit.setLayoutParams(tmpparams4);
        this.addView(countdownEdit);

        //Countdown
        TextView countdownCounter = new TextView(this.getContext(),null, R.style.text);
        LayoutParams tmpparams5 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmpparams5.setMargins(5,5,5,5);
        tmpparams5.addRule(ALIGN_PARENT_RIGHT);
        countdownCounter.setLayoutParams(tmpparams5);
        countdownCounter.setTextSize(25);
        countdownCounter.setText("00:00:00:00:00:00:00");
        this.addView(countdownCounter);
*/
        Log.d("Node","Created Node and added all children to it. ");
    }

    /*
    <RelativeLayout
            android:id="@+id/countdownId_0"
            android:tag="COUNTDOWN_0"
            android:onClick="onNodeClick" --> in CountdownListMainActivity
           >
            <TextView
                android:id="@+id/CountdownHeading_0"
               />
            <ImageButton
                android:id="@+id/countdownStopStartToggle_0"
          />
            <ImageButton
                android:id="@+id/countdownMuteMotivateToggle_0" />
            <ImageButton
                android:id="@+id/countdownEdit_0" />
            <TextView
                android:id="@+id/Countdown_0"
                android:layout_below="@id/CountdownHeading_0"/>
        </RelativeLayout>

    */

    public int getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id(int android_id) {
        this.android_id = android_id;
    }

    public String getAndroid_tag() {
        return android_tag;
    }

    public void setAndroid_tag(String android_tag) {
        this.android_tag = android_tag;
    }

    public String getAndroid_text() {
        return android_text;
    }

    public void setAndroid_text(String android_text) {
        this.android_text = android_text;
    }

    /*public Node(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    /*public Node(Context context, AttributeSet attrs) {
        super(context, attrs);
    }*/
}

