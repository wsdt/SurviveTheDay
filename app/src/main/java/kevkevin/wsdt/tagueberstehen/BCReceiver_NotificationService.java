package kevkevin.wsdt.tagueberstehen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class BCReceiver_NotificationService extends BroadcastReceiver {
    //Notification Service Starter (when time is up) ------------------------------
    @Override
    public void onReceive(Context context, Intent intent) {
        new SchedulePersistCountdown().getNotificationBuilder().issueNotification(SchedulePersistCountdown.getNotificationsSeparatedByCategory().get("WORK").get(0));
    }
}
