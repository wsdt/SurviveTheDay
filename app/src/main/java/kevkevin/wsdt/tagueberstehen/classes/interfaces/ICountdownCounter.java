package kevkevin.wsdt.tagueberstehen.classes.interfaces;

public interface ICountdownCounter {
    //was in early versions the asynctask (now outsourced in own class with threads etc.)
    int REFRESH_UI_EVERY_X_MS = 400;
    //TODO: maybe make multiplikator configurable (but displayed for user in seconds!)
    int REFRESH_RANDOM_QUOTE_MULTIPLIKATOR = 35; //quote gets updated every REFRESH_UI_EVERY_X_MS * times to skip updating quote -> MULTIPLIKATOR
    int PROGRESS_ZERO_VALUE = 100; //so finished
    String TOTAL_TIMEUNIT_ZERO_VALUE = "0.00";
    String BIG_COUNTDOWN_ZERO_VALUE = "0:0:0:0:0:0:0";
}
