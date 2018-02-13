package kevkevin.wsdt.tagueberstehen.classes;


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
import kevkevin.wsdt.tagueberstehen.util.SkuDetails;

public class InAppPurchaseManager_newNew {
    //IMPORTANT: Helper should NOT be a global MEMBER! (only locally for each listener etc.) --> to avoid overlapping
    private Context context;
    private String base64EncodedPublicKey;
    private static Inventory allInAppProducts;
    private final static String TAG = "InAppPurchaseMgr_NN";

    public InAppPurchaseManager_newNew(Context context) {
        this.setContext(context);
    }

    //TODO: new strategy --> In every method make the WHOLE workflow independently from other methods or values!!!!

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
                final IabHelper iabHelper = createNewIabHelper();
                iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    @Override
                    public void onIabSetupFinished(IabResult result) {
                        if (result.isSuccess() && iabHelper != null) {
                            try {
                                iabHelper.queryInventoryAsync(true, skuList, null, new IabHelper.QueryInventoryFinishedListener() {
                                    @Override
                                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                                        if (result.isSuccess() && inv != null) {
                                            //WARNING: Value to setter might not get assigned fast enough (so eventually older values get used or nullpointer exceptions could occur!)
                                            setAllInAppProducts(inv); //set inventory if successfully loaded (async)
                                            Log.d(TAG, "queryAllProducts:onQueryInventoryFinished: Downloaded inventory successfully.");
                                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();}
                                        } else {
                                            Log.e(TAG, "queryAllProducts:onQueryInventoryFinished: Could not load all product details. Did not save anything! Maybe inventory null.");
                                            Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure+" (1)", Toast.LENGTH_SHORT).show();
                                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();}
                                        }
                                    }
                                });
                            } catch (IabHelper.IabAsyncInProgressException e) {
                                e.printStackTrace();
                                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();}
                            }
                        } else {
                            Log.e(TAG, "queryAllProducts: Could not setup Iabhelper (maybe obj is null): "+result.toString());
                            Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure+" (2)", Toast.LENGTH_SHORT).show();
                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();}
                        }
                    }
                });

                Log.d(TAG, "queryAllProducts: Tried to download new product details. ");

                //return getAllInAppProducts(); //because of while until not null it should be always an inventory there except on error [prevent endless pause so it could nevertheless return NULL!] (always evaluate if value null)
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "queryAllProducts: WARNING an error occured while receiving product details.");
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();}
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
            inappProductNode = (RelativeLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.inappproductnode_template, nodeContainer, false); //to use layoutparams of xml file correctly

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

    //GETTER/SETTER
    public IabHelper createNewIabHelper() {
        return new IabHelper(this.getContext(), this.getBase64EncodedPublicKey());
    }

    public static Inventory getAllInAppProducts() {
        return allInAppProducts;
    }

    public static void setAllInAppProducts(Inventory allInAppProducts) {
        InAppPurchaseManager_newNew.allInAppProducts = allInAppProducts;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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
}
