package kevkevin.wsdt.tagueberstehen.classes;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

public class InAppPurchaseManager {
    private static final String TAG = "InAppPurchaseManager";
    private Bundle ownedItems;
    private Context context;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IInAppBillingService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    private void bindInAppService(Context context) {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        context.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        this.context = context;
    }

    private void unbindInAppService(Context context) {
        if (mService != null) {
            context.unbindService(mServiceConn);
        }
    }

    public Bundle getOwnedItems() {
        try {
            this.ownedItems = this.mService.getPurchases(3, this.context.getPackageName(), "inapp", null);
        } catch (RemoteException e) {
            Log.e(TAG, "getOwnedItems: Could not load purchases. Maybe a Nullpointerexception will be thrown!");
            e.printStackTrace();
        }
        return this.ownedItems;
    }
    public Bundle buyItem(String productId) {
        return null; //return this.mService.getBuyIntent(3, this.context.getPackageName(), ,"inapp", productId);
    }


}
