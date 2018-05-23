package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;

import kevkevin.wsdt.tagueberstehen.annotations.Enhance;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.DaoApp;

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


    // GETTER/SETTER -----------------------------------------------------------
    public String getLpKuerzel() {
        return lpKuerzel;
    }

    public void setLpKuerzel(@NonNull String lpKuerzel) {
        if (lpKuerzel.length() != 2) {
            Log.e(TAG, "setLpKuerzel: LanguageId has been set, BUT it is INVALID. Please check, errors might occur-> "+lpKuerzel);
        }
        this.lpKuerzel = lpKuerzel.toLowerCase(); //save everything in lowerCase.
    }

    // DB OPERATIONS ----------------------------------------------------------------------
    /**
     * Saves new/Updates languagePack
     */
    public void save(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getLanguagePackDao().insertOrReplace(this);
    }

    public void delete(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getLanguagePackDao().delete(this);
    }

    public void deleteAll(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getLanguagePackDao().deleteAll();
    }

    /**
     * Queries languagePacks.
     */
    public static LanguagePack query(@NonNull Context context, @NonNull String lpKuerzel) {
        LanguagePackDao dao = ((DaoApp) context.getApplicationContext()).getDaoSession().getLanguagePackDao();
        List<LanguagePack> lpList = dao.queryBuilder()
                .where(LanguagePackDao.Properties.LpKuerzel.eq(lpKuerzel))
                .list();

        if (lpList.size() <= 0) {
            return null;
        } else {
            return lpList.get(0); //not primary key, but nevertheless libId + languagePack should be also unique!
        }
    }
}