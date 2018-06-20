package kevkevin.wsdt.tagueberstehen;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import kevkevin.wsdt.tagueberstehen.R;

public class LoginInFirebaseActivity extends AppCompatActivity {

    static RelativeLayout relLayout;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            relLayout.setVisibility(View.VISIBLE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in_firebase);

        relLayout = (RelativeLayout) findViewById(R.id.relLay1);

        handler.postDelayed(runnable, 2000);
   }

   public static void invisible(){
        relLayout.setVisibility(View.INVISIBLE);
   }
}
