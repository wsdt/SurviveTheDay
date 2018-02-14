package kevkevin.wsdt.tagueberstehen.classes;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.util.Inventory;
import kevkevin.wsdt.tagueberstehen.util.SkuDetails;

public class DialogManager {
    private Activity context;
    private InAppPurchaseManager inAppPurchaseManager;
    private Resources res;
    private static final String TAG = "DialogManager";

    public DialogManager(Activity context) {
        this.setContext(context);
        this.setRes(context.getResources());
        this.setInAppPurchaseManager(new InAppPurchaseManager(context));
    }

    public void showDialog_InAppProductPromotion(@NonNull final String skuProductId) {
        //Download skuDetails and put them into the dialog (skudetails method is not necessary we can do it manually by query method)
        this.getInAppPurchaseManager().queryAllProducts_ASYNC(false, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                //resultcode 0 for inapppurchaseactivity and 1 for dialogs
                //getInAppPurchaseManager().purchaseProduct(skuProductId, 1, null);
                Inventory inventory = InAppPurchaseManager.getAllInAppProducts();
                if (inventory == null) {
                    Log.e(TAG, "showDialog_InAppProductPromotion: Inventory is null. (Unknown error).");
                    Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure+" (1)", Toast.LENGTH_SHORT).show();
                } else {
                    SkuDetails skuDetails = InAppPurchaseManager.getAllInAppProducts().getSkuDetails(skuProductId);
                    if (skuDetails == null) {
                        Log.e(TAG, "showDialog_InAppProductPromotion: Queried inventory does not contain product (maybe online deleted or wrong id?): "+skuProductId);
                        Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queriedInventoryDoesNotContainProduct, Toast.LENGTH_SHORT).show();
                    } else {
                        showDialog_Generic(skuDetails.getTitle(), skuDetails.getDescription(),  getRes().getString(R.string.dialog_inAppProduct_button_positive_buy), getRes().getString(R.string.dialog_inAppProduct_button_negative_buy), R.drawable.app_icon, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                            @Override
                            public void success_is_true() {
                                //resultcode 0 for inapppurchaseactivity and 1 for dialogs
                                Log.d(TAG, "showDialog_InAppProductPromotion: Trying to launch purchase flow by Dialog buy button.");
                                getInAppPurchaseManager().purchaseProduct(skuProductId, 1, null);
                            }

                            @Override
                            public void failure_is_false() {
                                Log.d(TAG, "showDialog_InAppProductPromotion: Dialog buy cancel btn clicked. Aborting purchase flow.");
                            }
                        });
                    }
                }
            }

            @Override
            public void failure_is_false() {
                //if failure downloading all products (not showing dialog for usability)
                Log.e(TAG, "showDialog_InAppProductPromotion: Could not download inventory. Not showing product dialog.");
                Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure+" (2)", Toast.LENGTH_SHORT).show();
            }
        });



    }

    public void showDialog_Generic(@Nullable String title, @Nullable String msg, @Nullable String lblPositiveBtn, @Nullable String lblNegativeBtn, int icon, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) { //to nullable icon just put a negative value in it
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        builder.setTitle((title == null) ? this.getRes().getString(R.string.dialog_generic_error_title) : title)
                .setMessage((msg == null) ? this.getRes().getString(R.string.dialog_generic_error_msg) : msg)
                .setIcon((icon < 0) ? R.drawable.app_icon : icon) //ids have to be positive
                .setPositiveButton((lblPositiveBtn == null) ? this.getRes().getString(R.string.dialog_generic_button_positive) : lblPositiveBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "showDialog_InAppProductPromotion: Closing dialog (Positive Button).");
                        dialog.dismiss();
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();}
                    }
                })
                //use provided listener if not null otherwise use default one below
                .setNegativeButton((lblNegativeBtn == null) ? this.getRes().getString(R.string.dialog_generic_button_negative) : lblNegativeBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "showDialog_InAppProductPromotion: Closing dialog (Negative Button).");
                        dialog.dismiss();
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();}
                    }
                })
                .show();
    }


    //GETTER/SETTER ----------------------------------
    public InAppPurchaseManager getInAppPurchaseManager() {
        return inAppPurchaseManager;
    }

    public void setInAppPurchaseManager(InAppPurchaseManager inAppPurchaseManager) {
        this.inAppPurchaseManager = inAppPurchaseManager;
    }

    public Activity getContext() {
        return this.context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public Resources getRes() {
        return res;
    }

    public void setRes(Resources res) {
        this.res = res;
    }
}
