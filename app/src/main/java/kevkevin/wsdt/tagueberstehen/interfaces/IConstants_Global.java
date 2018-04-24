package kevkevin.wsdt.tagueberstehen.interfaces;

import java.util.Locale;

public interface IConstants_Global {
    interface GLOBAL {
        //FORMATTING PERCENTAGE and BIG NUMBERS or DATES
        Locale LOCALE = Locale.ENGLISH;
        Character THOUSAND_GROUPING_SEPERATOR = ',';
        String DATETIME_FORMAT = "dd.MM.yyyy hh:mm:ss";
        String DATETIME_FORMAT_REGEX = "\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}";
    }

    interface DEVELOPERS {
        String WSDT = "WSDT (Kevin Riedl)";
    }

    //TODO: Instead of language pack lib packs etc.!! (one installed by default)
    interface LANGUAGE_PACK {
        //String DEFAULT_LANGUAGE_PACK = STORAGE_MANAGERS.DATABASE_STR_MGR.TABLES.QUOTELANGUAGEPACKAGES.LANGUAGE_PACKS[0];
    }
}
