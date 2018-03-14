package kevkevin.wsdt.tagueberstehen.classes.manager;

import android.content.Intent;
import android.support.annotation.NonNull;

public class ShareMgr {
    public static Intent getSimpleShareIntent(@NonNull String shareSubject, @NonNull String shareText) {
        return refreshExistingShareIntent(new Intent(), shareSubject, shareText);
    }

    /** If we want to enhance our performance by avoiding an object allocation (useful when refreshingShare intent in loops) */
    public static Intent refreshExistingShareIntent(@NonNull Intent shareIntent, @NonNull String shareSubject, @NonNull String shareText) {
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return shareIntent;
    }
}
