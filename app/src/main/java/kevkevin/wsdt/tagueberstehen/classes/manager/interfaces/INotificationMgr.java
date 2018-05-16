package kevkevin.wsdt.tagueberstehen.classes.manager.interfaces;

import kevkevin.wsdt.tagueberstehen.classes.services.interfaces.ILiveCountdown_ForegroundService;

public interface INotificationMgr {
    //MUST BE LOWER than NOTIFICATION_ID of COUNTDOWNCOUNTERSERVICE! Below this no. a motivational notification gets its random notification id
    int NOTIFICATION_ID = (ILiveCountdown_ForegroundService.NOTIFICATION_ID-1); //currently 999999949
    String NOTIFICATION_CHANNEL_DEFAULT_ID = "SURVIVE THE_DAY_GENERAL"; //now obligatory for android o etc.
    String NOTIFICATION_CHANNEL_DEFAULT_NAME = "General Notifications";
    String NOTIFICATION_CHANNEL_LIVECOUNTDOWN_ID = "SURVIVE_THE_DAY_LIVECOUNTDOWN";
    String NOTIFICATION_CHANNEL_LIVECOUNTDOWN_NAME = "Live Countdown";
    String NOTIFICATION_CHANNEL_MOTIVATION_ID = "SURVIVE_THE_DAY_MOTIVATION";
    String NOTIFICATION_CHANNEL_MOTIVATION_NAME = "Motivation";

    //LED light of notification (how long to blink on/off)
    int NOTIFICATION_BLINK_OFF_TIME_IN_MS = 1000;
    int NOTIFICATION_BLINK_ON_TIME_IN_MS = 1000;

    //CREATE NOTIFICATION ITSELF (IDENTIFIERS for PUTEXTRA in intents etc.)
    String IDENTIFIER_COUNTDOWN_ID = "COUNTDOWN_ID";
    String IDENTIFIER_CONTENT_TITLE = "CONTENT_TITLE";
    String IDENTIFIER_CONTENT_TEXT = "CONTENT_TEXT";
    String IDENTIFIER_SMALL_ICON = "SMALL_ICON";
}
