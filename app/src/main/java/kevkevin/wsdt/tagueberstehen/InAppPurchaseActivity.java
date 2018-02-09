package kevkevin.wsdt.tagueberstehen;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.vending.billing.IInAppBillingService;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager_newUsedHelper;

public class InAppPurchaseActivity extends AppCompatActivity{
    private InAppPurchaseManager_newUsedHelper inAppPurchaseManager;
    private static final String TAG = "InAppPurchaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_purchase);

        //Load ads
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.inAppPurchaseAct_RL));

        this.inAppPurchaseManager = new InAppPurchaseManager_newUsedHelper(this);
        this.inAppPurchaseManager.printAllInAppProductsAsNode((LinearLayout) findViewById(R.id.inappProductList));
        /*this.inAppPurchaseManager.setmServiceConn(new ServiceConnection() { //serviceconnection must be overwritten BEFORE bindservice gets called
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                inAppPurchaseManager.setmService(IInAppBillingService.Stub.asInterface(iBinder));
                //call this method for that activity if loaded:
                inAppPurchaseManager.printAllInAppProductsAsNode((LinearLayout) findViewById(R.id.inappProductList));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                inAppPurchaseManager.setmService(null);
            }
        });*/
        //this.inAppPurchaseManager.bindInAppService(); //call after overwriting serviceconnectionlistener
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.inAppPurchaseManager.unbindIabHelper();
        Log.d(TAG, "onDestroy: Tried to unbind IabHelper.");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+requestCode+", "+resultCode+", "+data);

        //This procedure seems to be vital that everything works fine
        //Pass on the activity result to the helper for handling
        if (!this.inAppPurchaseManager.getIabHelper().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

}
