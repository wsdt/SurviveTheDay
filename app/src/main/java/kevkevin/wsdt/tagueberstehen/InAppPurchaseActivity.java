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

import com.android.vending.billing.IInAppBillingService;

import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager;

public class InAppPurchaseActivity extends AppCompatActivity{
    private InAppPurchaseManager inAppPurchaseManager;
    private static final String TAG = "InAppPurchaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_purchase);

        this.inAppPurchaseManager = new InAppPurchaseManager(this);
        this.inAppPurchaseManager.setmServiceConn(new ServiceConnection() { //serviceconnection must be overwritten BEFORE bindservice gets called
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
        });
        this.inAppPurchaseManager.bindInAppService(); //call after overwriting serviceconnectionlistener
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.inAppPurchaseManager.unbindInAppService();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Product: "+data.getStringExtra("INAPP_PRODUCT_ID")+"\nRequest-Code: "+requestCode+"\nResult-Code: "+resultCode);//intent
    }

    /*show all  ArrayList<String> detailsList = getAllInAppProducts().getStringArrayList("DETAILS_LIST");
            Log.d(TAG, "onServiceConnected: RESPONSE-CODE-->"+getAllInAppProducts().getInt("RESPONSE_CODE")+" // PRODUCT-LIST: "+detailsList);*/




}
