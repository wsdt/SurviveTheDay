package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppPurchaseMgr;

public class InAppPurchaseActivity extends AppCompatActivity{
    private static final String TAG = "InAppPurchaseActivity";
    private static int counterActivityResume = 0; //how often got activity into foreground (so no repeat in showing all products [print method]) --> because in onCreate/onStart UI Thread is blocked despite Thread and join

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_purchase);

        //Load ads
        AdMgr adMgr = new AdMgr(this);
        adMgr.initializeAdmob();
        adMgr.loadBannerAd((RelativeLayout) findViewById(R.id.inAppPurchaseAct_RL));

        //IMPORTANT: Purchase failed is only when we clicked on buttons before. But this code worked before!
        Log.d(TAG, "onStart: Now trying to load resources from Google play.");
        new InAppPurchaseMgr(this).printAllInAppProductsAsNode((LinearLayout) findViewById(R.id.inappProductList));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.inAppPurchaseManager.unbindIabHelper(); --> not necessary because every helper in all methods gets unbinded
        Log.d(TAG, "onDestroy: Tried to unbind IabHelper.");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+requestCode+", "+resultCode+", "+data);

        //This procedure seems to be vital that everything works fine
        //Pass on the activity result to the helper for handling
        /*if (!this.inAppPurchaseManager.getIabHelper().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }*/
    }

}
