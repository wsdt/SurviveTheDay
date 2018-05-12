package kevkevin.wsdt.tagueberstehen.classes.manager;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import nl.dionsegijn.konfetti.models.Size;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;

/** Used for funny/entertaining animations etc. (e.g. konfetti) */
public class GamificationMgr {
    private static final String TAG = "GamificationMgr";

    /** @param viewGroup: Provide viewGroup (most of the time parentViewgroup) which hosts the konfettiView.*/
    public static void showKonfetti(@NonNull ViewGroup viewGroup, @NonNull Countdown countdown) {
        KonfettiView konfettiView = new KonfettiView(viewGroup.getContext());
        konfettiView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //Add view to provided viewGroup
        viewGroup.addView(konfettiView);

        /** Calculate corresponding colors (to use category color, but multiple variations of it)*/
        int hexCategoryColor = Color.parseColor(countdown.getCategory());

        konfettiView.build()
                .addColors(hexCategoryColor,hexCategoryColor+0xFFFFFFFF,hexCategoryColor-0xFFAAAAAA,hexCategoryColor-0xAA222222) //KonfettiColors are dependent from CategoryColor :)
                .setDirection(0.0,359.0)
                .setSpeed(1f,5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(4000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12,20))
                .setPosition(-50f, viewGroup.getWidth()+50f,-50f,-50f)
                .stream(300,5000L);
        Log.d(TAG, "showKonfetti: Tried to show konfetti.");
    }

}
