package kevkevin.wsdt.tagueberstehen.interfaces;

import java.util.Locale;

public interface IGlobal {
    interface GLOBAL {
        //FORMATTING PERCENTAGE and BIG NUMBERS or DATES
        Locale LOCALE = Locale.ENGLISH;
        Character THOUSAND_GROUPING_SEPERATOR = ',';
        String DATETIME_FORMAT = "dd.MM.yyyy hh:mm:ss";
        String DATETIME_FORMAT_REGEX = "\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}";
    }

    interface DEVELOPERS {
        String WSDT = "WSDT (Kevin Riedl)";
        String SOLUTION = "SOLUTION (Christof Jori)";
    }
}
