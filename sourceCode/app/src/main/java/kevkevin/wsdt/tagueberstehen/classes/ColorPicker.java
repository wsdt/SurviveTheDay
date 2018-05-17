package kevkevin.wsdt.tagueberstehen.classes;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ColorPicker {
    private static final String TAG = "ColorPicker";

    public static void openColorPickerDialog(Context context, final View viewToChangeBackground, int categoryColor, boolean supportsAlpha) {
        AmbilWarnaDialog colorDialog = new AmbilWarnaDialog(context, categoryColor, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Log.d(TAG, "openColorPickerDialog:onCancel(): Canceled action.");
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                viewToChangeBackground.setBackgroundColor(color);
            }
        });
        colorDialog.show();
    }

    public static int getBackgroundColor(View view) {
        ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
        return colorDrawable.getColor();
    }

    public static String getBackgroundColorHexString(View view) {
        return String.format("#%06X", (0xFFFFFF & getBackgroundColor(view)));
    }
}
