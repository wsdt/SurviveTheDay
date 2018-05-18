package kevkevin.wsdt.tagueberstehen.classes.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.CountdownActivity;
import kevkevin.wsdt.tagueberstehen.annotations.Enhance;
import kevkevin.wsdt.tagueberstehen.classes.entities.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.manager.NotificationMgr;

import static android.content.Context.NOTIFICATION_SERVICE;

/** Superior manager for all services (broadcast receiver, foreground services etc.) */
public class ServiceMgr {
    private static final String TAG = "ServiceMgr";

    @Enhance(message = "Improve performance and cohesion, see comments")
    public static void restartNotificationService(@NonNull Context context) { //also called by Kickstarter_BootAndGeneralReceiver
        //TODO: BEST: ONLY EXECUTE this method, IF especially a relevant setting got changed (but hard to implement, because shared prefs get overwritten) --> would solve comment below with only st
        Log.d(TAG, "restartNofificationService: Did not restart service (not necessary). Tried broadcast receiver.");
        //would not be necessary because on broadcastreceiver the current countdown gets automatically loaded!
        //except if countdown was created, then we have to reload it! (only changes/deletes do not require a reload) [but for bgservice mode it is necessary]

        if (Countdown.queryAll(context).size() > 0) {
            //Only start broadcast receivers or service when at least one countdown acc. to criteria found
            //TODO: only do this when not already active (otherwise intervals will get restarted)
            (new NotificationMgr(context, CountdownActivity.class, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))).scheduleAllActiveCountdownNotifications();
            Log.d(TAG, "restartNotificationService: Rescheduled all broadcast receivers.");
        }

        //ALSO RESTART FOREGROUND SERVICE
        Intent foregroundServiceIntent = new Intent(context, LiveCountdown_ForegroundService.class);
        try {
            Log.d(TAG, "restartNotificationService: Trying to restart foregroundService.");
            LiveCountdown_ForegroundService.stopRefreshAll();
            context.stopService(foregroundServiceIntent);
            context.startService(foregroundServiceIntent);
        } catch (NullPointerException e) {
            Log.e(TAG, "restartNotificationService: foregroundServiceIntent equals null! Could not restart foregroundService.");
        }
        Log.d(TAG, "restartNotficationService: Tried to restart foregroundService.");
    }

}
