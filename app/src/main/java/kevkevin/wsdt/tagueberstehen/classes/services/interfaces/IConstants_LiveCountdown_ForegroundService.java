package kevkevin.wsdt.tagueberstehen.classes.services.interfaces;

public interface IConstants_LiveCountdown_ForegroundService {
    /* 999999950 as base so each countdown can have its own notification (but within the same foreground service be changed
     * notification id for a countdown: (999999950+countdownid) so..: countdown 0 = 999999950, countdown 1 = 999999951 etc.
     * --> High no. because notificationIds of motivational notifications are generated randomly up to this no.!*/
    int NOTIFICATION_ID = 999999950; //IMPORTANT: 999999950 - 999999999 reserved for FOREGROUNDCOUNTERSERVICE [999999950+countdownId = foregroundNotificationID, etc.]
}
