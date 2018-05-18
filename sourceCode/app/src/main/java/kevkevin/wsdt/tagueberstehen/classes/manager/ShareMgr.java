package kevkevin.wsdt.tagueberstehen.classes.manager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import kevkevin.wsdt.tagueberstehen.annotations.Enhance;

@Enhance (message = "Add functionality (also facebook, etc.) also add possibility to share more specific etc., Efficiency")
public class ShareMgr {
    /** If we want to enhance our performance by avoiding an object allocation (useful when refreshingShare intent in loops) */
    public static Intent getSimpleShareIntent(@Nullable Intent shareIntent, @NonNull String shareSubject, @NonNull String shareText) {
        if (shareIntent == null) {shareIntent = new Intent();}
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return shareIntent;
    }

}
