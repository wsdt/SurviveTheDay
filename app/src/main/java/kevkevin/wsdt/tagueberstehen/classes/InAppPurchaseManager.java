package kevkevin.wsdt.tagueberstehen.classes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.util.IabHelper;
import kevkevin.wsdt.tagueberstehen.util.IabResult;
import kevkevin.wsdt.tagueberstehen.util.Inventory;
import kevkevin.wsdt.tagueberstehen.util.Purchase;
import kevkevin.wsdt.tagueberstehen.util.SkuDetails;


//TODO IMPORTANT TO RESOLVE ######################################################################################
//TODO: App stürzt ab sobald PRODUKT GEKAUFT!!!!!!!!!!!!
public class InAppPurchaseManager {
    //IMPORTANT: Helper should NOT be a global MEMBER! (only locally for each listener etc.) --> to avoid overlapping
    private Context activityContext;
    private String base64EncodedPublicKey;
    private static Inventory allInAppProducts;
    private final static String TAG = "InAppPurchaseMgr_NN";


    public InAppPurchaseManager(Context activityContext) {
        /*IMPORTANT: Context SHOULD be ALWAYS an Activity (esp. for launching purchases [otherwise we get a ClassCastException])
        * , but we allow normal Context so we can execute other methods in service and similar e.g. */
        Log.w(TAG, "Constructor: If provided context is NOT an activity, you should NOT launch purchase workflows or similar because you would get ClassCast Exceptions [catched].");
        this.setActivityContext(activityContext);
    }

    //IMPORTANT FOR NEW METHODS: new strategy --> In every method make the WHOLE workflow independently from other methods or values!!!!

    //DONE
    public void queryAllProducts_ASYNC(boolean forceRefresh, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
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
                Log.d(TAG, "queryAllProducts_ASYNC: Trying to setup helper.");
                final IabHelper iabHelper = createNewIabHelper();
                iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    @Override
                    public void onIabSetupFinished(IabResult result) {
                        Log.d(TAG, "queryAllProducts:onIabSetupFinished: Tried to finish Setup.");
                        if (result.isSuccess() && iabHelper != null) {
                            try {
                                iabHelper.queryInventoryAsync(true, skuList, null, new IabHelper.QueryInventoryFinishedListener() {
                                    @Override
                                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                                        if (result.isSuccess() && inv != null) {
                                            //WARNING: Value to setter might not get assigned fast enough (so eventually older values get used or nullpointer exceptions could occur!)
                                            setAllInAppProducts(inv); //set inventory if successfully loaded (async)
                                            Log.d(TAG, "queryAllProducts:onQueryInventoryFinished: Downloaded inventory successfully.");
                                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();
                                            }
                                        } else {
                                            Log.e(TAG, "queryAllProducts:onQueryInventoryFinished: Could not load all product details. Did not save anything! Maybe inventory null.");
                                            Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure + " (1)", Toast.LENGTH_SHORT).show();
                                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                                            }
                                        }
                                    }
                                });
                            } catch (IabHelper.IabAsyncInProgressException e) {
                                e.printStackTrace();
                                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                                }
                            }
                        } else {
                            Log.e(TAG, "queryAllProducts: Could not setup Iabhelper (maybe obj is null): " + result.toString());
                            Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure + " (2)", Toast.LENGTH_SHORT).show();
                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                            }
                        }
                        //DISPOSE HELPER REGARDLESS OF SUCCESS/FAILURE
                        unbindIabHelper(iabHelper);
                    }
                });

                Log.d(TAG, "queryAllProducts: Tried to download new product details. ");

                //return getAllInAppProducts(); //because of while until not null it should be always an inventory there except on error [prevent endless pause so it could nevertheless return NULL!] (always evaluate if value null)
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "queryAllProducts: WARNING an error occured while receiving product details.");
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                }
            }
        } else {
            Log.d(TAG, "queryAllProducts: AllInAppProducts-Inventory already downloaded for this session. Used it instead of reloading.");
            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                Log.d(TAG, "queryAllProducts: executeAfterCompletation is not null. Executing.");
                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();
            }
        }
    }


    public void printAllInAppProductsAsNode(final @NonNull LinearLayout nodeContainer) {
        //no iabsetupcompleted listener here necessary (is in queryallproducts)
        this.queryAllProducts_ASYNC(false, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                //NodeContainer should have a vertical orientation and maybe be scrollable (so nodeContainer should be within a Scrollview)
                //validate whether relativelayout of craft function is null otherwise do not add it
                if (getAllInAppProducts() != null) {
                    for (Map.Entry<String, SkuDetails> entryStrSkuDetails : getAllInAppProducts().getmSkuMap().entrySet()) {
                        printInAppProductAsNode(nodeContainer, entryStrSkuDetails);
                    }
                } else {
                    Log.w(TAG, "printAllInAppProductsAsNode: getAllInAppProducts() is NULL! Maybe no products in Google Play Console created or no internet?");
                }
            }

            @Override
            public void failure_is_false() {
                Log.e(TAG, "printAllInAppProductsAsNode: Could not query products!");
            }
        });
    }


    private void printInAppProductAsNode(@NonNull LinearLayout nodeContainer, Map.Entry<String, SkuDetails> entryStrSkuDetails) {
        //############################ IMPORTANT: THIS METHOD SHOULD NOT BE EXECUTED WITHOUT SETUP ENSURE!!! #####################################
        //json must be the playstore string
        RelativeLayout inappProductNode = null;
        try {
            inappProductNode = (RelativeLayout) ((LayoutInflater) getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.inappproductnode_template, nodeContainer, false); //to use layoutparams of xml file correctly

            //Remove from title PRODUCT_NAME (APPLICATION_NAME) the APPLICATION NAME incl. parenthesis --> happens now in SkuDetails itself!
            //now assign values to views
            ((TextView) inappProductNode.findViewById(R.id.inappProductTitle)).setText(entryStrSkuDetails.getValue().getTitle());
            ((TextView) inappProductNode.findViewById(R.id.inappProductDescription)).setText(entryStrSkuDetails.getValue().getDescription());
            Button inAppProductPrice = (Button) inappProductNode.findViewById(R.id.inAppProductPrice);
            inAppProductPrice.setText(entryStrSkuDetails.getValue().getPrice());

            //Buy when clicking on it
            final String productId = entryStrSkuDetails.getKey();
            inappProductNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "printInAppProductAsNode:onClick: purchaseWorkflow for productId: " + productId);
                    purchaseProduct(productId, 0, null);
                }
            });

            inAppProductPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "printInAppProductAsNode:onClick: purchaseWorkflow for productId (2): " + productId);
                    purchaseProduct(productId, 0, null);
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

    public void purchaseProduct(final String productSkuId, final int resultCode, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        Log.d(TAG, "purchaseProduct: Trying to launch purchase workflow.");
        try { //use given onPurchaseListener, if not given (= null) then use default one
            final IabHelper iabHelper = createNewIabHelper();
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess() && iabHelper != null) {
                        try {
                            //HERE context has to be an Activity !!
                            final Activity activityContext = (Activity) getActivityContext(); //will be also used in on purchase successful!
                            iabHelper.launchPurchaseFlow(activityContext, productSkuId, resultCode, new IabHelper.OnIabPurchaseFinishedListener() {
                                @Override
                                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        /*TODO: Security Recommendation: When you receive the purchase response from Google Play,
     todo                   ensure that you check the returned data signature and the orderId. Verify that the orderId
     todo                   exists and is a unique value that you have not previously processed. For added security,
     todo                   you should perform purchase validation on your own secure server.
     todo                   Query*/

                                    if (result.isFailure()) {
                                        Log.e(TAG, "purchaseProduct:onIabPurchaseFinished: Purchase failed --> " + result);
                                        //custom error messages or generic one if no suitable found
                                        switch (result.getResponse()) {
                                            case 7:
                                                Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_purchaseProductFailureAlreadyBought, Toast.LENGTH_SHORT).show();
                                                break;
                                            default:
                                                Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_purchaseProductFailure, Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                                        }
                                    } else { //else if with separate productSkus?
                                        //inventory.hasPurchase(SKU) --> wurde gekauft
                                        //maybe we have to reload all views (if null then we do it in the else{} with refreshing the whole activity)
                                        Log.d(TAG, "purchaseProduct:onIabPurchaseFinished: Purchase successful: " + result);
                                        Toast.makeText(getActivityContext(), String.format(getActivityContext().getResources().getString(R.string.inAppPurchaseManager_success_purchaseProductSuccess), info.getSku()) + " / Result: " + result, Toast.LENGTH_LONG).show();
                                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();
                                        } else { //TODO: not called when purchase successful [maybe only when uploaded? Purchase works trotzdem] (Class not found when unmarshalling: com.google.android.finsky.billing.common.PurchaseParams)
                                            //If interface method null then we just reload the whole activity [context has to be an activity for this method!!]
                                            Log.d(TAG, "purchaseProduct:onIabPurchaseFinished: No Interface method for success given. Launched default one and reload the whole activity.");
                                            setAllInAppProducts(null); //so it gets reloaded
                                            activityContext.finish();
                                            activityContext.startActivity(activityContext.getIntent());
                                        }
                                        //RECOMMENDATION: Give custom OnPurchaseFinishedListener, because this one does not that much! After purchase completed the inventory.hasPurchase should be true for that sku!

                                        //DISPOSE HELPER REGARDLESS OF SUCCESS/FAILURE
                                        unbindIabHelper(iabHelper);
                                    }
                                }
                                //WARNING: empty string of payload will be null while processing
                            }, generateUniquePayload()); //todo: maybe developer payload verify!
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                            }
                        }
                    } else {
                        Log.e(TAG, "purchaseProduct:onIabSetupFinished: Setup not successful. ");
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "purchaseProduct: Could not purchase Product.");
            Toast.makeText(this.getActivityContext(), R.string.inAppPurchaseManager_error_purchaseProductFailure, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
            }
        }
    }

    //TODO: use payload field for more security (but do not rely on it acc. to google play!)
    private String generateUniquePayload() {
        //For every purchase we need an unique payload (otherwise pendingintent is null!)
        String uuid = UUID.randomUUID().toString();
        Log.d(TAG, "generateUniquePayload: Generated unique payload: " + uuid);
        return uuid;
    }

    public void executeIfProductIsBought(final String productSkuId, final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        this.queryAllProducts_ASYNC(false, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                //Querying and setup successfull then verify purchase and execute again different interface methods
                try {
                    if (getAllInAppProducts().hasPurchase(productSkuId)) {
                        Log.d(TAG, "ExecuteIfProductIsBought:onIabSetupFinished: Needed to download inventory. Product is bought. Executed Interface execute().");
                        executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(); //overwritten execute method will be executed
                    } else {
                        Log.d(TAG, "ExecuteIfProductIsBought:onIabSetupFinished: Needed to download inventory. Product is NOT bought (executed negative method).");
                        executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "isProductBought: Inventory not already set. Try it later again.");
                    Toast.makeText(getActivityContext(), R.string.inAppPurchaseManager_error_isProductPurchasedFailure, Toast.LENGTH_SHORT).show(); //maybe remove, because if not internet and not bought this msg is useless [but useful if no internet and bought]
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                }
            }

            @Override
            public void failure_is_false() { //querying/setup failure then do failure-method of bought interface
                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                Log.e(TAG, "isProductBought:querySetupHelper: Could not setup helper or could not fetch inventory.");
            }
        });
    }

    /* NOT NECESSARY: Because we can use queryInventory_ASYNC and then just execute in successMethod: this.queryAllProducts(false).getSkuDetails(productSkuId);
    public void ExecuteAfterGetProductDetails(String productSkuId, final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        Log.d(TAG, "getProductDetails: Returning SkuDetails of product: " + productSkuId);
        this.queryAllProducts_ASYNC(false, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                Log.d(TAG, "ExecuteAfterGetProductDetails: Executing positive interface method. Inventory/Setup successful.");
                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();
            }

            @Override
            public void failure_is_false() {
                Log.e(TAG, "ExecuteAfterGetProductDetails: Could not fetch inventory or could not setup helper.");
                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
            }
        });
    }*/

    public void resetAllPurchasedItems() {
        //This method consumes all purchased items (although they are not consumable)
        Log.w(TAG, "resetAllPurchasedItems: WARNING this method should be ONLY called for testing purchases!");
        final IabHelper iabHelper = createNewIabHelper();
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                try {
                    iabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                        @Override
                        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                            List<Purchase> purchaseList = new ArrayList<>(inv.getmPurchaseMap().values());
                            try {
                                iabHelper.consumeAsync(purchaseList, new IabHelper.OnConsumeMultiFinishedListener() {
                                    @Override
                                    public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
                                        Log.d(TAG, "resetAllPurchasedItems: Tried to consume all items. Should have been worked! ");
                                        Log.w(TAG, "#################################################################################\n" +
                                                "REMOVE resetAllPurchasedItems() from Code (esp. when releasing app!) #####################\n" +
                                                "######################################################################################");
                                        unbindIabHelper(iabHelper);
                                    }
                                });
                            } catch (IabHelper.IabAsyncInProgressException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //GETTER/SETTER
    public IabHelper createNewIabHelper() {
        return new IabHelper(this.getActivityContext(), this.getBase64EncodedPublicKey());
    }

    public void unbindIabHelper(IabHelper iabHelper) { //should be called in Activity's onDestroy()
        Log.d(TAG, "unbindIabHelper: Trying to dispose helper now.");
        //IMPORTANT: Call this method after downloading! (always call within oniabsetupcompleted and after executing all methods)
        if (iabHelper != null) {
            try {
                iabHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException | IllegalStateException e) {
                Log.e(TAG, "unbindIabHelper: Could not dispose IabHelper! But I tried to set it to null afterwards.");
                //e.printStackTrace();
            }
        }
        iabHelper = null;
    }

    public static Inventory getAllInAppProducts() {
        return allInAppProducts;
    }

    public static void setAllInAppProducts(Inventory allInAppProducts) {
        InAppPurchaseManager.allInAppProducts = allInAppProducts;
    }

    public Context getActivityContext() {
        return this.activityContext;
    }

    public void setActivityContext(Context activityContext) {
        this.activityContext = activityContext;
    }

    public void setBase64EncodedPublicKey(String base64EncodedPublicKey) {
        this.base64EncodedPublicKey = base64EncodedPublicKey;
    }

    public String getBase64EncodedPublicKey() {
        if (this.base64EncodedPublicKey == null || this.base64EncodedPublicKey.equals("")) { //only manufactur it if not already done
            //manufactur public key so it is not written as static string in here
            StringBuilder base64EncodedPublicKey = new StringBuilder();
            int loopCounter = 0;
            for (String substr : Constants.INAPP_PURCHASES.BASE64ENCODED_PUBLICKEY.substr_arr) {
                if ((loopCounter++) > 0) {
                    base64EncodedPublicKey.append(Constants.INAPP_PURCHASES.BASE64ENCODED_PUBLICKEY.SEPARATOR);
                } //only first loop do not add separator (because it does not start with it)
                base64EncodedPublicKey.append(substr);
            }
            //set new value
            this.base64EncodedPublicKey = base64EncodedPublicKey.toString();
        } //always return it (old value or if null the new one)
        return this.base64EncodedPublicKey;
    }
}
