package kevkevin.wsdt.tagueberstehen.classes.manager;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.util.Inventory;
import kevkevin.wsdt.tagueberstehen.util.SkuDetails;

public class DialogMgr {
    private Activity context;
    private InAppPurchaseMgr inAppPurchaseMgr;
    private Resources res;
    private static final String TAG = "DialogMgr";

    public DialogMgr(Activity context) {
        //Context MUST be an activity, because we reload it if purchase was a success!!
        this.setContext(context);
        this.setRes(context.getResources());
        this.setInAppPurchaseMgr(new InAppPurchaseMgr(context));
    }

    public void showDialog_InAppProductPromotion(@NonNull final String skuProductId) {
        //Download skuDetails and put them into the dialog (skudetails method is not necessary we can do it manually by query method)
        this.getInAppPurchaseMgr().queryAllProducts_ASYNC(false, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {
                //resultcode 0 for inapppurchaseactivity and 1 for dialogs
                //getInAppPurchaseMgr().purchaseProduct(skuProductId, 1, null);
                Inventory inventory = InAppPurchaseMgr.getAllInAppProducts();
                if (inventory == null) {
                    Log.e(TAG, "showDialog_InAppProductPromotion: Inventory is null. (Unknown error).");
                    Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure + " (1)", Toast.LENGTH_SHORT).show();
                } else {
                    SkuDetails skuDetails = InAppPurchaseMgr.getAllInAppProducts().getSkuDetails(skuProductId);
                    if (skuDetails == null) {
                        Log.e(TAG, "showDialog_InAppProductPromotion: Queried inventory does not contain product (maybe online deleted or wrong id?): " + skuProductId);
                        Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queriedInventoryDoesNotContainProduct, Toast.LENGTH_SHORT).show();
                    } else {
                        showDialog_Generic(skuDetails.getTitle(), skuDetails.getDescription(), getRes().getString(R.string.dialog_inAppProduct_button_positive_buy), getRes().getString(R.string.dialog_inAppProduct_button_negative_buy), R.drawable.light_appicon_48dp, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                            @Override
                            public void success_is_true(@Nullable Object... args) {
                                //resultcode 0 for inapppurchaseactivity and 1 for dialogs
                                Log.d(TAG, "showDialog_InAppProductPromotion: Trying to launch purchase flow by Dialog buy button.");
                                getInAppPurchaseMgr().purchaseProduct(skuProductId, 1, null);
                            }

                            @Override
                            public void failure_is_false(@Nullable Object... args) {
                                Log.d(TAG, "showDialog_InAppProductPromotion: Dialog buy cancel btn clicked. Aborting purchase flow.");
                            }
                        });
                    }
                }
            }

            @Override
            public void failure_is_false(@Nullable Object... args) {
                //if failure downloading all products (not showing dialog for usability)
                Log.e(TAG, "showDialog_InAppProductPromotion: Could not download inventory. Not showing product dialog.");
                Toast.makeText(getContext(), R.string.inAppPurchaseManager_error_queryAllProductsFailure + " (2)", Toast.LENGTH_SHORT).show();
            }
        });


    }

    /**
     * @param lblNegativeBtn: By providing an empty string ("") [not null!] there will only the OK button added
     */
    public void showDialog_Generic(@Nullable String title, @Nullable String msg, @Nullable String lblPositiveBtn, @Nullable String lblNegativeBtn, int icon, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) { //to nullable icon just put a negative value in it
        showDialog_Generic(createDialog(title,msg,lblPositiveBtn,lblNegativeBtn,icon,executeIfTrueSuccess_or_ifFalseFailure_afterCompletation));
    }

    /** Overloaded method for showing already crafted dialog. [use always this method for avoiding TokenException!] */
    public void showDialog_Generic(@NonNull Dialog dialog) {
        if (!getContext().isFinishing()) { //really important
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            Log.d(TAG, "showDialog_Generic: Activity/Context is finishing. Did not show dialog. Prevented bad token exception.");
        }
    }

    public Dialog createDialog(@Nullable String title, @Nullable String msg, @Nullable String lblPositiveBtn, @Nullable String lblNegativeBtn, int icon, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        builder.setTitle((title == null) ? this.getRes().getString(R.string.dialog_generic_error_title) : title)
                .setMessage((msg == null) ? this.getRes().getString(R.string.dialog_generic_error_msg) : msg)
                .setIcon((icon < 0) ? R.drawable.light_appicon_48dp : icon) //ids have to be positive
                .setPositiveButton((lblPositiveBtn == null) ? this.getRes().getString(R.string.dialog_generic_button_positive) : lblPositiveBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "showDialog_InAppProductPromotion: Closing dialog (Positive Button).");
                        dialog.dismiss();
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();
                        }
                    }
                });

        //use provided listener if not null otherwise use default one below, but if empty string then use no negative btn
        if (lblNegativeBtn != null) {
            if (lblNegativeBtn.equals("")) {
                return builder.create(); //return it before hand, bc. already done
            }
        } //not else!!
        builder.setNegativeButton((lblNegativeBtn == null) ? this.getRes().getString(R.string.dialog_generic_button_negative) : lblNegativeBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "showDialog_InAppProductPromotion: Closing dialog (Negative Button).");
                dialog.dismiss();
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                }
            }
        }); //if btnLabelNegative was not an empty string, but null OR another string then we add a negativeButton

        return builder.create(); //create crafted dialog
    }


    //GETTER/SETTER ----------------------------------
    public InAppPurchaseMgr getInAppPurchaseMgr() {
        return inAppPurchaseMgr;
    }

    public void setInAppPurchaseMgr(InAppPurchaseMgr inAppPurchaseMgr) {
        this.inAppPurchaseMgr = inAppPurchaseMgr;
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
