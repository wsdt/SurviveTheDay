package kevkevin.wsdt.tagueberstehen.classes;


public class Constants {
    public interface COUNTDOWNCOUNTERSERVICE {
        /* 99 as base so each countdown can have its own notification (but within the same foreground service be changed
        * notification id for a countdown: (1000+countdownid) so..: countdown 0 = 1000, countdown 1 = 1001 etc. */
        int NOTIFICATION_ID = 1000;
    }
}
