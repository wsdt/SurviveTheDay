package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.InAppPurchaseManager;

public class TEST_InAppPurchase extends AppCompatActivity{
    private InAppPurchaseManager inAppPurchaseManager;
    private static final String TAG = "TEST_InAppPurchase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test__in_app_purchase);

        this.inAppPurchaseManager = new InAppPurchaseManager(this);
        this.inAppPurchaseManager.bindInAppService();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.inAppPurchaseManager.unbindInAppService();
    }


    public void testbuy(View v) {
        try {
            Intent futureResultIntent = new Intent();
            futureResultIntent.putExtra("INAPP_PRODUCT_ID", Constants.INAPP_PURCHASES.INAPP_PRODUCTS.BUY_EVERYTHING_ID.toString());
            startIntentSenderForResult(this.inAppPurchaseManager.buyInAppProduct("android.test.purchased"/*Constants.INAPP_PURCHASES.INAPP_PRODUCTS.BUY_EVERYTHING_ID.toString()*/).getIntentSender(),0, futureResultIntent,0,0,0);
        } catch (IntentSender.SendIntentException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Product: "+data.getStringExtra("INAPP_PRODUCT_ID")+"\nRequest-Code: "+requestCode+"\nResult-Code: "+resultCode);//intent
    }
}
