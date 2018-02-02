package kevkevin.wsdt.tagueberstehen.classes;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;
import java.util.UUID;

import kevkevin.wsdt.tagueberstehen.R;

public class InAppPurchaseManager {
    private static final String TAG = "InAppPurchaseManager";
    private Bundle ownedProducts;
    private Bundle allAvailableProducts;
    private Context context;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;

    //TODO: all methods in thread (not on mainthread)
    //TODO: all strings into strings.xml etc. and constants
    public InAppPurchaseManager(@NonNull Context context) {
        this.setContext(context);

        //IMPORTANT: Call unbindInAppService after Purchase or loadingofproducts is DONE!!!
        this.bindInAppService(); //bindService so we can do sth
    }

    public void bindInAppService() {
        this.setmServiceConn(new ServiceConnection() { //must be called BEFORE bindService()!
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                setmService(IInAppBillingService.Stub.asInterface(iBinder));

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                setmService(null);
            }
        });
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending"); //important for security
        this.getContext().bindService(serviceIntent, this.getmServiceConn(), Context.BIND_AUTO_CREATE);
    }

    public void unbindInAppService() {
        if (this.getmService() != null) {
            this.getContext().unbindService(this.getmServiceConn());
        }
    }

    public Bundle getAllInAppProducts() {
        if (this.getAllAvailableProducts() == null) {
            try {
                ArrayList<String> skuList = new ArrayList<>();

                if (Constants.INAPP_PURCHASES.USE_STATIC_TEST_INAPP_PRODUCTS) {
                    for (Constants.INAPP_PURCHASES.TEST_INAPP_PRODUCTS.STATIC_TEST.GOOGLE_PLAY_STATIC_RESPONSES response : Constants.INAPP_PURCHASES.TEST_INAPP_PRODUCTS.STATIC_TEST.GOOGLE_PLAY_STATIC_RESPONSES.values()) {
                        Log.d(TAG, "getAllInAppProducts: Added test product to skuList: "+response.toString());
                        skuList.add(response.toString());
                    }
                } else {
                    //Add all products from enum within interfaces to list (so all get downloaded)
                    for (Constants.INAPP_PURCHASES.INAPP_PRODUCTS inAppProduct : Constants.INAPP_PURCHASES.INAPP_PRODUCTS.values()) {
                        Log.d(TAG, "getAllInAppProducts: Added product to skuList --> " + inAppProduct.toString());
                        skuList.add(inAppProduct.toString());
                    }
                }

                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                this.setAllAvailableProducts(getmService().getSkuDetails(3, this.getContext().getPackageName(), "inapp", querySkus));
                return this.getAllAvailableProducts();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "getInAppProductDetailsOfAllProducts: WARNING an error occured while receiving product details. Method returned NULL!");
                return null;
            }
        } else {
            Log.d(TAG, "getAllInAppProducts: AllInAppProducts-Bundle already downloaded for this session. Used it instead of reloading.");
            return this.getAllAvailableProducts();
        }
    }

    public PendingIntent buyManagedInAppProduct(String skuInAppProductId) {
        skuInAppProductId = (Constants.INAPP_PURCHASES.USE_STATIC_TEST_INAPP_PRODUCTS ? Constants.INAPP_PURCHASES.TEST_INAPP_PRODUCTS.STATIC_TEST.BUY_PRODUCT_DEFAULT_RESPONSE : skuInAppProductId);
        try {
            //Use static test in app products or real products (which is delivered by method parameter)
            Log.d(TAG, "buyInAppProduct: Buying following product --> "+skuInAppProductId);
            Bundle buyIntentBundle = this.getmService().getBuyIntent(3, this.getContext().getPackageName(), skuInAppProductId, "inapp", generateUniquePayload());
            PendingIntent buyPendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            if (buyPendingIntent == null) {
                Toast.makeText(this.getContext(), R.string.inAppPurchaseManager_buyManagedInAppProduct_msg_ProductAlreadyBoughtORnotLoggedInOnGoogle,Toast.LENGTH_LONG).show();
                Log.e(TAG, "buyInAppProduct: PendingIntent is NULL! Item might be already have been purchased or user needs to login into his Google account.");
            }
            return buyPendingIntent;
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "buyInAppProduct: Could not buy item (presumably: RemoteException).");
            return null;
        }
    }

    public Bundle getAllOwnedInAppProducts() {
        try {
            this.setOwnedProducts(this.getmService().getPurchases(3, this.getContext().getPackageName(), "inapp", null)); //continuationToken only necessary if we would have an large amount of owned products! (so loading occurs with several steps)
        } catch (RemoteException e) {
            Log.e(TAG, "getOwnedItems: Could not load purchases. Maybe a Nullpointerexception will be thrown!");
            e.printStackTrace();
        }
        return this.getOwnedProducts();
    }

    private String generateUniquePayload() {
        //For every purchase we need an unique payload (otherwise pendingintent is null!)
        String uuid = UUID.randomUUID().toString();
        Log.d(TAG, "generateUniquePayload: Generated unique payload: "+uuid);
        return uuid;
    }

    public void printAllBuyableInAppProductsAsNode(@NonNull LinearLayout nodeContainer) {
        //NodeContainer should have a vertical orientation and maybe be scrollable (so nodeContainer should be within a Scrollview)

    }


    //GETTER/SETTER
    public void setOwnedProducts(Bundle ownedItems) {
        this.ownedProducts = ownedItems;
    }
    public Bundle getOwnedProducts() {
        return this.ownedProducts;
    }

    public Context getContext() {
        if (this.context == null) {Log.w(TAG, "getContext: Context is NULL! Should not be possible/happen!");}
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public IInAppBillingService getmService() {
        return mService;
    }

    public void setmService(IInAppBillingService mService) {
        this.mService = mService;
    }

    public ServiceConnection getmServiceConn() {
        return mServiceConn;
    }

    public void setmServiceConn(ServiceConnection mServiceConn) {
        this.mServiceConn = mServiceConn;
    }

    public Bundle getAllAvailableProducts() {
        return allAvailableProducts;
    }

    public void setAllAvailableProducts(Bundle allAvailableProducts) {
        this.allAvailableProducts = allAvailableProducts;
    }
}
