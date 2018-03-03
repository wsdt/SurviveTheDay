package kevkevin.wsdt.tagueberstehen.classes.manager;

import android.content.Intent;
import android.support.annotation.NonNull;

public class ShareMgr {
    public static Intent getSimpleShareIntent(@NonNull String shareSubject, @NonNull String shareText) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return shareIntent;
    }
}
