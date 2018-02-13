package kevkevin.wsdt.tagueberstehen.classes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.util.IabException;
import kevkevin.wsdt.tagueberstehen.util.IabHelper;
import kevkevin.wsdt.tagueberstehen.util.IabResult;
import kevkevin.wsdt.tagueberstehen.util.Inventory;
import kevkevin.wsdt.tagueberstehen.util.Purchase;
import kevkevin.wsdt.tagueberstehen.util.SkuDetails;

public class InAppPurchaseManager_newUsedHelper {
    private final static String TAG = "InAppPurchaseMgr_new";
    private Activity activityContext;
    private IabHelper iabHelper;
    private static Inventory allInAppProducts; //static so data is shared over all instances (faster, no reload except on forceReload==true)
    private String base64EncodedPublicKey; //see GooglePlayConsole

    public InAppPurchaseManager_newUsedHelper(Activity activityContext) {
        this.setActivityContext(activityContext);
        //this.setIabHelper(new IabHelper(activityContext, this.getBase64EncodedPublicKey()), null); //important!!
    }





//todo -----------------------------------------------------------------------------------------------------------
    //TODO: maybe download all data in advance at start of the app and then store it for runtime [maybe objects get destroyed!!] or better store it in globalSharedPref for an specific amount of time (same logic as in RewardedAd)
    //todo -----------------------------------------------------------------------------------------------------------

    /*public void executeIfProductIsBought(final String productSkuId, final HelperClass.ExecuteIfTrueFalseAfterCompletation executeIfTrueFalseAfterCompletation) {
        this.setIabHelper(new IabHelper(this.getActivityContext(), this.getBase64EncodedPublicKey()), new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                //if inventory was null, then setup helper and download it (if already set up then just execute this method)
                if (result.isSuccess()) {
                    queryAllProducts(false, new HelperClass.ExecuteAfterCompletation() {
                        @Override
                        public void execute() {
                            try {
                                if (getAllInAppProducts().hasPurchase(productSkuId)) {
                                    Log.d(TAG, "ExecuteIfProductIsBought:onIabSetupFinished: Needed to download inventory. Product is bought. Executed Interface execute().");
                                    executeIfTrueFalseAfterCompletation.is_true(); //overwritten execute method will be executed
                                } else {
                                    Log.d(TAG, "ExecuteIfProductIsBought:onIabSetupFinished: Needed to download inventory. Product is NOT bought (executed negative method).");
                                    executeIfTrueFalseAfterCompletation.is_false();
                                }
                            } catch(NullPointerException e) {
                                Log.e(TAG, "isProductBought_everythingLoaded: Inventory not already set. Try it later again. WARNING: Method returned FALSE!");
                                Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_isProductPurchasedFailure, Toast.LENGTH_SHORT).show(); //maybe remove, because if not internet and not bought this msg is useless [but useful if no internet and bought]
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "executeIfProductIsBought:onIabSetupFinished: Could not setup helper! Assuming product is NOT bought.");
                    //assume false, because of when app has no internet!! (security)
                    executeIfTrueFalseAfterCompletation.is_false();
                }
            }
        });
    }*/

    /*private boolean isProductBought_everythingLoaded(final String productSkuId) {
        //IMPORTANT: THIS METHOD REQUIRES THAT HELPER IS SET UP!
        try { //if internet should not happen, because at instantiation all products get downloaded
            queryAllProducts(false, new HelperClass.ExecuteAfterCompletation() {
                @Override
                public void execute() {
                    return getAllInAppProducts().hasPurchase(productSkuId);
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "isProductBought_everythingLoaded: Inventory not already set. Try it later again. WARNING: Method returned FALSE!");
            Toast.makeText(this.getActivityContext(), R.string.inAppPurchaseManager_error_isProductPurchasedFailure, Toast.LENGTH_SHORT).show(); //maybe remove, because if not internet and not bought this msg is useless [but useful if no internet and bought]
        }
        return false; //return false (not bought) if error occurred
    }*/

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
                        Toast.makeText(activityContext, R.string.inAppPurchaseManager_error_purchaseProductFailure, Toast.LENGTH_SHORT).show();
                    } else { //else if with separate productSkus?
                        //inventory.hasPurchase(SKU) --> wurde gekauft
                        //TODO: maybe we have to reload all views
                        Toast.makeText(getActivityContext(), String.format(getActivityContext().getResources().getString(R.string.inAppPurchaseManager_success_purchaseProductSuccess), info.getSku()) + " / Result: " + result, Toast.LENGTH_LONG).show();
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

    public SkuDetails NOT_DONE_getProductDetails(String productSkuId) {
        Log.d(TAG, "getProductDetails: Returning SkuDetails of product: " + productSkuId);
        try {//TODO: NOT IMPLEMENTED YET
            //return this.queryAllProducts(false).getSkuDetails(productSkuId);
        } catch (NullPointerException e) {
            Log.e(TAG, "getProductDetails: Inventory not already set. Try it later again. WARNING: Method returned NULL!");
            e.printStackTrace();
        }
        return null;
    }


    public void queryAllProducts(boolean forceRefresh/*, @Nullable final HelperClass.ExecuteAfterCompletation executeAfterCompletation*/) {
        if (getAllInAppProducts() == null || forceRefresh) {
            try {
                final ArrayList<String> skuList = new ArrayList<>();

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

                //Query inventory setup Secure
                //boolean isHelperReady = false; //if false we have to reset Helper

                /*if (this.getIabHelper() != null) {
                    if (this.getIabHelper().mSetupDone) {
                        Log.d(TAG, "queryAllProducts_SetupSecure: Setup was already done. Continuing.");
                        setAllInAppProducts(this.getIabHelper().queryInventory(true, skuList, null)); //here we have a fixed return value! but we have to wait on the UI thread --> asynctask so to show on UI thread with progress bar or similar
                        isHelperReady = true;
                        if (executeAfterCompletation != null) {
                            Log.d(TAG, "queryAllProducts: ExecuteAfterCompletation is NOT null, so we execute method.");
                            executeAfterCompletation.execute();
                        }
                    } else {
                        Log.d(TAG, "queryAllProducts_SetupSecure: Helper not set up.");
                    }
                } else {
                    Log.d(TAG, "queryAllProducts_SetupSecure: IabHelper is NULL. Setting up..");
                }*/

                //if (!isHelperReady) {
                    Log.d(TAG, "queryAllProducts_SetupSecure: IabHelper was not fully set up, now trying to set a new one up.");
                    /*this.setIabHelper(new IabHelper(this.getActivityContext(), this.getBase64EncodedPublicKey()), new IabHelper.OnIabSetupFinishedListener() {
                        @Override
                        public void onIabSetupFinished(IabResult result) {
                            Log.d(TAG, "queryAllProducts_SetupSecure:OnIabSetupFinished: Resetting helper.");
                            if (result.isFailure()) {
                                Log.e(TAG, "queryAllProducts_SetupSecure:OnIabSetupFinished: Could not setup Helper!");
                            } else {*/
                                Log.d(TAG, "queryAllProducts_SetupSecure:OnIabSetupFinished: Resetted helper successfully. Querying all products now.");
                                //try {
                                    this.executeAfterIabHelperSetup(this.createNewIabHelper(), new HelperClass.ExecuteAfterCompletation() {
                                        @Override
                                        public void execute() {
                                            try {
                                                setAllInAppProducts(getIabHelper().queryInventory(true, skuList, null)); //here we have a fixed return value! but we have to wait on the UI thread --> asynctask so to show on UI thread with progress bar or similar
                                            } catch (IabException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                       /*if (executeAfterCompletation != null) { //method should be executed after queryInventory (queryInventory is sync!!)
                                        Log.d(TAG, "queryAllProducts:OnIabSetupFinishedListener:OnIabSetupFinished: ExecuteAfterCompletation is NOT null, so we execute method.");
                                        executeAfterCompletation.execute(); //execute given method
                                    }*/
                                /*} catch (IabException e) {
                                    e.printStackTrace();
                                }*/
                            /*}
                        }
                    });*/
                //}



                //TODO: asynctask or similar or solution for async so value always available or method would have to wait without blocking UI
                 Log.d(TAG, "queryAllProducts: Tried to download new product details. ");

                //return getAllInAppProducts(); //because of while until not null it should be always an inventory there except on error [prevent endless pause so it could nevertheless return NULL!] (always evaluate if value null)
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "queryAllProducts: WARNING an error occured while receiving product details. Method returned NULL!");
                //return null;
            }
        } else {
            Log.d(TAG, "queryAllProducts: AllInAppProducts-Bundle already downloaded for this session. Used it instead of reloading.");
            /*if (executeAfterCompletation != null) {
                Log.d(TAG, "queryAllProducts: executeAfterCompletation is not null. Executing.");
                executeAfterCompletation.execute();
            }*/
            //return getAllInAppProducts();
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
        this.iabHelper = null; //not using setter because setter does more things
    }

    /*private void waitUntilInAppPurchaseMgrReady(/*final Runnable runnable*) {
        if (!this.getIabHelper_NOREADYCHECK().mSetupDone) { //only create thread if not already ready
            /*new Thread(new Runnable() {
                @Override
                public void run() {*

            Thread purchaseMgrReadyControllerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int timeoutCounter = 0;
                    while (!getIabHelper_NOREADYCHECK().mSetupDone) { //if inventory load async then we would need to add it here also
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if ((timeoutCounter++) > 50) {
                            Log.d(TAG, "waitUntilInAppPurchaseMgrReady:Thread: Timeout exceeded. Leaving loop.");
//                            Toast.makeText(activityContext, R.string.inAppPurchaseManager_error_iabHelperSetupFailure, Toast.LENGTH_SHORT).show();
                            break; //exit loop because of timeout
                        } else if (timeoutCounter == 25) { //needs to be the 2nd if else (because 25 always true before 50 would be called
                            //trying to resetup instance
                            Log.d(TAG, "waitUntilInAppPurchaseMgrReady:Thread: First timeout exceeded. Trying to resetup Manager.");
                            setIabHelper(new IabHelper(activityContext, getBase64EncodedPublicKey()),null);
                            //No break here
                        }
                        //}
//                getActivityContext().runOnUiThread(runnable); //what to run after everything loaded
                    }
                }
            });
            purchaseMgrReadyControllerThread.start();
            try {
                purchaseMgrReadyControllerThread.join(); //wait on other thread to finish before continue
            } catch (InterruptedException e) {
                Log.w(TAG, "waitUntilAppPurchaseMgrReady: Purchase Manager might not be successfully set up!");
                e.printStackTrace();
            }
            //exit thread (not necessary but better)
            //Thread.currentThread().interrupt();
            //return;
        } else {
            Log.d(TAG, "waitUntilInAppPurchaseMgrReady: Manager ready. Successfully set up!");
        }
    }*/

    //VIEW-Operations ###################################################################################
    public void printAllInAppProductsAsNode(final @NonNull LinearLayout nodeContainer) {

        //NodeContainer should have a vertical orientation and maybe be scrollable (so nodeContainer should be within a Scrollview)
        //validate whether relativelayout of craft function is null otherwise do not add it
        /*queryAllProducts(false, new HelperClass.ExecuteAfterCompletation() {
            @Override
            public void execute() {*/
                if (getAllInAppProducts() != null) {
                    for (Map.Entry<String, SkuDetails> entryStrSkuDetails : getAllInAppProducts().getmSkuMap().entrySet()) {
                        printInAppProductAsNode(nodeContainer, entryStrSkuDetails);
                    }
                } else {
                    Log.w(TAG, "printAllInAppProductsAsNode: getAllInAppProducts() is NULL! Maybe no products in Google Play Console created or no internet?");
                }
           /* }
        }); //query all products btw. refresh them IF they are NULL (internally evaluated)*/
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
                        Log.d(TAG, "printInAppProductAsNode:onClick: purchaseWorkflow for productId: " + productId);
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
        if (inappProductNode == null) {
            Log.e(TAG, "printInAppProductAsNode: Node is NULL. This should not happen!");
        }
    }


    //GETTER/SETTER #####################################################################################
    /*private IabHelper getIabHelper_NOREADYCHECK() {
        //use this special getter only for the waitUntilInAppPurchaseMgrReady() method (because of stackoverflow)!
        return this.iabHelper;
    }*/

    public IabHelper createNewIabHelper() {
        this.iabHelper = new IabHelper(this.getActivityContext(), this.getBase64EncodedPublicKey());
        return this.getIabHelper();
    }

    public IabHelper getIabHelper() {
        //Return helper if he is ready, but do not if it is not !
        //waitUntilInAppPurchaseMgrReady();
        return this.iabHelper;
    }

    public void executeAfterIabHelperSetup(IabHelper iabHelper, @NonNull final HelperClass.ExecuteAfterCompletation executeAfterCompletation) {
        if (iabHelper == null) {
            this.iabHelper = null;
            Log.d(TAG, "setIabHelper: Provided iabHelper is NULL!");
        } else {
            this.iabHelper = iabHelper;
            //if (!this.iabHelper.mSetupDone) { //if not already set up set it up and execute function after it
                this.iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    @Override
                    public void onIabSetupFinished(IabResult result) {
                        if (!result.isSuccess()) {
                            //problem while setting up
                            Log.e(TAG, "setIabHelper:OnIabSetupFinishedListener: Could not setup iabHelper!");
                            Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_iabHelperSetupFailure, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "onIabSetupFinished: Setup successful. Trying to execute Interface-method.");
                            //Downloading inventory (so not null in future (or less likely to)) --> because static, app might need a restart in order to see refreshed inventory
                            //queryAllProducts(false, null); //async if not already downloaded what will be here the case!
                            //= good because if multiple getproduct hintereinander, so nicht viele anfragen zum Server gleichzeitig da Listener noch nicht erfüllt.
                            executeAfterCompletation.execute();
                        }
                    }
                });
            /*} else {
                Log.d(TAG, "setIabHelper: Setup already done for this instance!");
                if (onIabSetupFinishedListener != null) {
                    Log.d(TAG, "setIabHelper: Setup done and trying to execute provided listener (not null) manually.");
                    onIabSetupFinishedListener.onIabSetupFinished(new IabResult(IabHelper.BILLING_RESPONSE_RESULT_OK, "Setup already done, executed listener manually."));
                }
            }*/
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

    public static Inventory getAllInAppProducts() {
        if (allInAppProducts == null) {
            Log.w(TAG, "getAllInAppProducts: Inventory is NULL!");
        }
        return allInAppProducts;
    }

    public static void setAllInAppProducts(Inventory allInAppProductsNew) {
        allInAppProducts = allInAppProductsNew;
    }

}
