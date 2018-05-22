package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import kevkevin.wsdt.tagueberstehen.classes.exceptions.InvalidLanguageIdentifier;

/** Used to determine which languageCodes should be used for userLibrary.*/
@Entity
public class LanguageCode {
    @Id
    /** E.g. EN, DE, etc. */
    private String lcKuerzel;
    private int lcLanguageName;

    @Keep
    public LanguageCode(@NonNull String kuerzel, int languageName) throws InvalidLanguageIdentifier {
        this.setLcKuerzel(kuerzel);
        this.setLcLanguageName(languageName);
    }

    @Generated(hash = 1636886504)
    public LanguageCode() {
    }

    @Override
    public String toString() {
        /** Only return kuerzel. */
        return this.getLcKuerzel();
    }

    /** Resource id to strings.xml for e.g. German<->Deutsch etc.</-></>*/
    public int getLcLanguageName() {
        return lcLanguageName;
    }

    public void setLcLanguageName(int lcLanguageName) {
        this.lcLanguageName = lcLanguageName;
    }

    public String getLcKuerzel() {
        return lcKuerzel;
    }

    public void setLcKuerzel(String lcKuerzel) throws InvalidLanguageIdentifier {
        if (lcKuerzel.length() != 2) {
            throw new InvalidLanguageIdentifier("Kürzel wrong formatted. Presumably it is too short/long -> "+lcKuerzel);
        }
        this.lcKuerzel = lcKuerzel.toLowerCase(); //save everything in lowerCase.
    }

    // GETTER/SETTER -----------------------------------------------------------

}