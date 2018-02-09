package kevkevin.wsdt.tagueberstehen.classes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.util.IabHelper;
import kevkevin.wsdt.tagueberstehen.util.IabResult;
import kevkevin.wsdt.tagueberstehen.util.Inventory;
import kevkevin.wsdt.tagueberstehen.util.Purchase;
import kevkevin.wsdt.tagueberstehen.util.SkuDetails;

public class InAppPurchaseManager_newUsedHelper {
    private final static String TAG = "InAppPurchaseMgr_new";
    private Activity activityContext;
    private IabHelper iabHelper;
    private Inventory allInAppProducts;
    private String base64EncodedPublicKey; //see GooglePlayConsole

    public InAppPurchaseManager_newUsedHelper(Activity activityContext) {
        this.setActivityContext(activityContext);
        this.setIabHelper(new IabHelper(activityContext, this.getBase64EncodedPublicKey())); //important!!
    }


    public boolean isProductBought(String productSkuId) {
        try { //if internet should not happen, because at instantiation all products get downloaded
            return this.getAllInAppProducts().hasPurchase(productSkuId);
        } catch (NullPointerException e) {
            Log.e(TAG, "isProductBought: Inventory not already set. Try it later again. WARNING: Method returned FALSE!");
            Toast.makeText(this.getActivityContext(), R.string.inAppPurchaseManager_error_isProductPurchasedFailure, Toast.LENGTH_SHORT).show(); //maybe remove, because if not internet and not bought this msg is useless [but useful if no internet and bought]
        }
        return false; //return false (not bought) if error occurred
    }

    public void purchaseProduct(String productSkuId, int resultCode, @Nullable IabHelper.OnIabPurchaseFinishedListener onIabPurchaseFinishedListener) {
        try { //use given onPurchaseListener, if not given (= null) then use default one
            this.getIabHelper().launchPurchaseFlow(this.getActivityContext(), productSkuId, resultCode, (onIabPurchaseFinishedListener != null) ? onIabPurchaseFinishedListener : new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                    /*TODO: Security Recommendation: When you receive the purchase response from Google Play,
 todo                   ensure that you check the returned data signature and the orderId. Verify that the orderId
 todo                   exists and is a unique value that you have not previously processed. For added security,
 todo                   you should perform purchase validation on your own secure server.
 todo                   Query*/

                    if (result.isFailure()) {
                        Log.e(TAG, "purchaseProduct:onIabPurchaseFinished: Purchase failed --> " + result);
                        Toast.makeText(getActivityContext(), String.format(getActivityContext().getResources().getString(R.string.countdown_info_startDateInFuture), info.getSku()) + " / Result: " + result, Toast.LENGTH_LONG).show();
                    } else { //else if with separate productSkus?
                        //inventory.hasPurchase(SKU) --> wurde gekauft
                        //TODO: maybe we have to reload all views
                        Toast.makeText(activityContext, R.string.inAppPurchaseManager_success_purchaesProductSuccess, Toast.LENGTH_SHORT).show();
                        //RECOMMENDATION: Give custom OnPurchaseFinishedListener, because this one does not that much! After purchase completed the inventory.hasPurchase should be true for that sku!
                    }

                    /* EXAMPLE: --------------------------------------------------------
                    else if (purchase.getSku().equals(SKU_GAS)) {
                        // consume the gas and update the UI
                    }
                    else if (purchase.getSku().equals(SKU_PREMIUM)) {
                        // give user access to premium content and update the UI
                    }*/
                }
            }, ""); //todo: maybe developer payload ?
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.e(TAG, "purchaseProduct: Could not purchase Product.");
            Toast.makeText(this.getActivityContext(), R.string.inAppPurchaseManager_error_purchaseProductFailure, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public SkuDetails getProductDetails(String productSkuId) {
        Log.d(TAG, "getProductDetails: Returning SkuDetails of product: " + productSkuId);
        try {
            return this.queryAllProducts(false).getSkuDetails(productSkuId);
        } catch (NullPointerException e) {
            Log.e(TAG, "getProductDetails: Inventory not already set. Try it later again. WARNING: Method returned NULL!");
            e.printStackTrace();
        }
        return null;
    }

    public Inventory queryAllProducts(boolean forceRefresh) {
        //TODO: why is setup not done????? (while printing)
        if (this.getAllInAppProducts() == null || forceRefresh) {
            try {
                ArrayList<String> skuList = new ArrayList<>();

                if (Constants.INAPP_PURCHASES.USE_STATIC_TEST_INAPP_PRODUCTS) {
                    for (Constants.INAPP_PURCHASES.TEST_INAPP_PRODUCTS.STATIC_TEST.GOOGLE_PLAY_STATIC_RESPONSES response : Constants.INAPP_PURCHASES.TEST_INAPP_PRODUCTS.STATIC_TEST.GOOGLE_PLAY_STATIC_RESPONSES.values()) {
                        Log.d(TAG, "queryAllProducts: Added test product to skuList: " + response.toString());
                        skuList.add(response.toString());
                    }
                } else {
                    //Add all products from enum within interfaces to list (so all get downloaded)
                    for (Constants.INAPP_PURCHASES.INAPP_PRODUCTS inAppProduct : Constants.INAPP_PURCHASES.INAPP_PRODUCTS.values()) {
                        Log.d(TAG, "queryAllProducts: Added product to skuList --> " + inAppProduct.toString());
                        skuList.add(inAppProduct.toString());
                    }
                }

                //MoreSubSkus would be for subscriptions additionally to items!
                //Normally better, but problem that we do not know whether value is always available or it is available too late: this.getIabHelper().queryInventoryAsync();
                /*this.getIabHelper().queryInventoryAsync(true, skuList, null, new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                        if (result.isSuccess()) {
                            //WARNING: Value to setter might not get assigned fast enough (so eventually older values get used or nullpointer exceptions could occur!)
                            setAllInAppProducts(inv); //set inventory if successfully loaded (async)
                            Log.d(TAG, "queryAllProducts:onQueryInventoryFinished: Downloaded inventory successfully.");
                        } else {
                            Log.e(TAG, "queryAllProducts:onQueryInventoryFinished: Could not load all product details. Did not save anything!");
                            Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure, Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
                //TODO: asynctask or similar or solution for async so value always available or method would have to wait without blocking UI
                this.setAllInAppProducts(this.getIabHelper().queryInventory(true, skuList, null)); //here we have a fixed return value! but we have to wait on the UI thread --> asynctask so to show on UI thread with progress bar or similar
                Log.w(TAG, "queryAllProducts: Tried to download new product details. ");

                /*int count = 0;
                while(this.getAllInAppProducts() == null) {

                    //do not use thread sleep! sleep as long as inventory is not downloaded! (refresh every half second)
                    if ((count++) > 25) { //(25*500ms) --> appr. 15s timeout
                        Log.e(TAG, "queryAllProducts: Could not download inventory. Timeout exceeded.");
                        Toast.makeText(this.getActivityContext(), R.string.inAppPurchaseManager_error_inventoryDownloadFailure, Toast.LENGTH_LONG).show();
                        break;
                    }
                }*/

                return this.getAllInAppProducts(); //because of while until not null it should be always an inventory there except on error [prevent endless pause so it could nevertheless return NULL!] (always evaluate if value null)
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "queryAllProducts: WARNING an error occured while receiving product details. Method returned NULL!");
                return null;
            }
        } else {
            Log.d(TAG, "queryAllProducts: AllInAppProducts-Bundle already downloaded for this session. Used it instead of reloading.");
            return this.getAllInAppProducts();
        }
    }

    public void unbindIabHelper() { //should be called in Activity's onDestroy()
        if (this.getIabHelper() != null) {
            try {
                this.getIabHelper().dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                Log.e(TAG, "unbindIabHelper: Could not dispose IabHelper! But I tried to set it to null afterwards.");
                e.printStackTrace();
            }
        }
        this.setIabHelper(null);
    }

    //VIEW-Operations ###################################################################################
    public void printAllInAppProductsAsNode(@NonNull LinearLayout nodeContainer) {
        //NodeContainer should have a vertical orientation and maybe be scrollable (so nodeContainer should be within a Scrollview)
        //validate whether relativelayout of craft function is null otherwise do not add it
        this.queryAllProducts(false); //query all products btw. refresh them IF they are NULL (internally evaluated)

        if (this.getAllInAppProducts() != null) {
            for (Map.Entry<String, SkuDetails> entryStrSkuDetails : this.getAllInAppProducts().getmSkuMap().entrySet()) {
                printInAppProductAsNode(nodeContainer, entryStrSkuDetails);
            }
        } else {Log.w(TAG, "printAllInAppProductsAsNode: getAllInAppProducts() is NULL! Maybe no products in Google Play Console created or no internet?");}
    }

    private void printInAppProductAsNode(@NonNull LinearLayout nodeContainer, Map.Entry<String, SkuDetails> entryStrSkuDetails) {
        //json must be the playstore string
        RelativeLayout inappProductNode = null;
        try {
            inappProductNode = (RelativeLayout) ((LayoutInflater) this.getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.inappproductnode_template, nodeContainer, false); //to use layoutparams of xml file correctly

            //now assign values to views
            ((TextView) inappProductNode.findViewById(R.id.inappProductTitle)).setText(entryStrSkuDetails.getValue().getTitle());
            ((TextView) inappProductNode.findViewById(R.id.inappProductDescription)).setText(entryStrSkuDetails.getValue().getDescription());
            ((TextView) inappProductNode.findViewById(R.id.inappProductPrice)).setText(entryStrSkuDetails.getValue().getPrice());

            //Buy when clicking on it
            final String productId = entryStrSkuDetails.getKey();
            inappProductNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.d(TAG, "printInAppProductAsNode:onClick: purchaseWorkflow for productId: "+productId);
                        Intent futureResultIntent = new Intent();
                        futureResultIntent.putExtra("INAPP_PRODUCT_ID", productId);
                        purchaseProduct(productId, 0, null);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });

            nodeContainer.addView(inappProductNode);
        } catch (NullPointerException e) {
            Log.e(TAG, "printInAppProductAsNode: Could not print inApp product to view.");
            e.printStackTrace();
        }
        if (inappProductNode == null) {Log.e(TAG, "printInAppProductAsNode: Node is NULL. This should not happen!");}
    }




    //GETTER/SETTER #####################################################################################
    public IabHelper getIabHelper() {
        return iabHelper;
    }

    public void setIabHelper(IabHelper iabHelper) {
        if (iabHelper != null) {
            this.iabHelper = iabHelper;
            this.iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        //problem while setting up
                        Log.e(TAG, "setIabHelper:OnIabSetupFinishedListener: Could not setup iabHelper!");
                        Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_iabHelperSetupFailure, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "onIabSetupFinished: Setup successful. Trying to download all product details now.");
                        //Downloading inventory (so not null in future (or less likely to))
                        queryAllProducts(false); //async if not already downloaded what will be here the case!
                        //= good because if multiple getproduct hintereinander, so nicht viele anfragen zum Server gleichzeitig da Listener noch nicht erfüllt.
                    }
                }
            });
        } else {
            Log.e(TAG, "setIabHelper: IabHelper is null!");
        }
    }

    public Activity getActivityContext() {
        return activityContext;
    }

    public void setActivityContext(Activity activityContext) {
        this.activityContext = activityContext;
    }

    public String getBase64EncodedPublicKey() {
        if (this.base64EncodedPublicKey == null || this.base64EncodedPublicKey.equals("")) { //only manufactur it if not already done
            //manufactur public key so it is not written as static string in here
            StringBuilder base64EncodedPublicKey = new StringBuilder();
            int loopCounter = 0;
            for (String substr : Constants.INAPP_PURCHASES.BASE64ENCODED_PUBLICKEY.substr_arr) {
                if ((loopCounter++) == 0) {
                    base64EncodedPublicKey.append(Constants.INAPP_PURCHASES.BASE64ENCODED_PUBLICKEY.SEPARATOR);
                } //only first loop add separator (because it does not start with it)
                base64EncodedPublicKey.append(substr);
            }
            //set new value
            this.base64EncodedPublicKey = base64EncodedPublicKey.toString();
        } //always return it (old value or if null the new one)
        return this.base64EncodedPublicKey;
    }

    public Inventory getAllInAppProducts() {
        if (this.allInAppProducts == null) {
            Log.w(TAG, "getAllInAppProducts: Inventory is NULL!");
        }
        return allInAppProducts;
    }

    public void setAllInAppProducts(Inventory allInAppProducts) {
        this.allInAppProducts = allInAppProducts;
    }

}
