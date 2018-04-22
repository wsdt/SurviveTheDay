package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces;

public interface IConstants_GlobalAppSettingsMgr {
    String SHAREDPREFERENCES_DBNAME = "APP_SETTINGS";
    String SPIDENTIFIER_SPOTLIGHTHELP_MODIFYCOUNTDOWNACTIVITY = "SPOTLIGHT_MODIFY_ACTIVITY_ALREADYSHOWN_v1"; /**if spotlight updated just increment v{nr}*/
    String SPIDENTIFIER_FIREBASESTORAGEMGR_DEFAULTDATADOWNLOADED = "FIREBASE_DEFAULTDATA_DOWNLOADED_v1"; /**if default libs (e.g.) already downloaded v{nr}*/
    String SPIDENTIFIER_BG_SERVICE_PID = "BG_SERVICE_PID";
    String SPIDENTIFIER_SAVE_BATTERY = "SAVE_BATTERY";
    String NO_INTERNET_CONNECTION_COUNTER = "NO_INTERNET_CONNECTION_COUNTER";
    String REMOVE_ADS_TEMPORARLY_IN_MINUTES = "REMOVE_ADS_TEMPORARLY_IN_MINUTES";
}
