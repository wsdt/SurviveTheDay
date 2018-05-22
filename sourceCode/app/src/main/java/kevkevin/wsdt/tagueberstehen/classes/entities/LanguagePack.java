package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Used to determine which languageCodes should be used for userLibrary.
 * <p>
 * Obj. mainly needed to make a N:M with all Lines etc.
 */
@Entity
public class LanguagePack {
    @Transient
    private static final String TAG = "LanguagePack";

    @Id
    /** E.g. EN, DE, etc. */
    private String lpKuerzel;

    /**
     * Normally no need for more members, bc. we can get Locale-Obj from this short string,
     * which has many methods (also getlanguageName or similar).
     */

    @Keep
    public LanguagePack(@NonNull String lpKuerzel) {
        this.setLpKuerzel(lpKuerzel);
    }

    @Generated(hash = 813268507)
    public LanguagePack() {
    }

    @Override
    public String toString() {
        /** Only return kuerzel. */
        return this.getLpKuerzel();
    }

    public String getLpKuerzel() {
        return lpKuerzel;
    }

    public void setLpKuerzel(@NonNull String lpKuerzel) {
        if (lpKuerzel.length() != 2) {
            Log.e(TAG, "setLpKuerzel: LanguageId has been set, BUT it is INVALID. Please check, errors might occur.");
        }
        this.lpKuerzel = lpKuerzel.toLowerCase(); //save everything in lowerCase.
    }

    // GETTER/SETTER -----------------------------------------------------------

}