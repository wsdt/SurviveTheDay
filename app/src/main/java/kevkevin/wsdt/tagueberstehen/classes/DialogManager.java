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

import kevkevin.wsdt.tagueberstehen.R;

public class DialogManager {
    private Activity context;
    private InAppPurchaseManager inAppPurchaseManager;
    private static final String TAG = "DialogManager";

    public DialogManager(Activity context) {
        this.setContext(context);
        this.setInAppPurchaseManager(new InAppPurchaseManager(context));
    }

    public void showDialog_InAppProductPromotion(@NonNull final String skuProductId) {

        //Download skuDetails and put them into the dialog (skudetails method is not necessary we can do it manually by query method)
        this.getInAppPurchaseManager().queryAllProducts_ASYNC(false, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {

            }

            @Override
            public void failure_is_false() {
                Resources res = getContext().getResources();
                showDialog_Generic(res.getString(R.string.dialog_generic_error_title),
                        res.getString(R.string.dialog_generic_error_msg),
                        (-1), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //POSITIVE BUTTON
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //NEGATIVE BUTTON
                            }
                        });
            }
        });



    }

    public void showDialog_Generic(@Nullable String title, @Nullable String msg, int icon, @Nullable DialogInterface.OnClickListener positiveButton, @Nullable DialogInterface.OnClickListener negativeButton) { //to nullable icon just put a negative value in it
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        Resources res = getContext().getResources();
        builder.setTitle((title == null) ? res.getString(R.string.dialog_generic_error_title) : title)
                .setMessage((msg == null) ? res.getString(R.string.dialog_generic_error_msg) : msg)
                .setIcon((icon < 0) ? R.drawable.app_icon : icon) //ids have to be positive
                .setPositiveButton(R.string.dialog_inAppProduct_button_positive_buy, (positiveButton != null) ? positiveButton : new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "showDialog_InAppProductPromotion: Closing dialog (user launched purchasing flow).");
                        dialog.dismiss();

                        //resultcode 0 for inapppurchaseactivity and 1 for dialogs
                        //getInAppPurchaseManager().purchaseProduct(skuProductId, 1, null);
                    }
                })
                //use provided listener if not null otherwise use default one below
                .setNegativeButton(R.string.dialog_inAppProduct_button_negative_buy, (negativeButton != null) ? negativeButton : new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "showDialog_InAppProductPromotion: Closing dialog (user cancelled dialog).");
                        dialog.dismiss();
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
}
