package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONArray;

import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.DaoApp;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.GreenDaoConverter;
import kevkevin.wsdt.tagueberstehen.classes.services.ServiceMgr;

import org.greenrobot.greendao.DaoException;

@Entity //active = true for getting generated methods
public class UserLibrary {
    @Transient
    private static final String TAG = "UserLibrary";
    @Id
    private String libId; //hashwert etc.
    /** Important for libName, libDescription*/
    private String libName;
    private String libDescription;

    /** No own object, bc. we can get of this languageCode (e.g. en, de) a Locale-Obj which as many
     * available options. e.g. getLanguageName or similar for English, German etc. */
    @Convert(converter = GreenDaoConverter.class,columnType = String.class)
    private List<String> libLanguageCodes;
    private String libCreator;
    //For mapping from FirebaseStorMgr to Obj
    @Keep
    public UserLibrary(@NonNull String libId, String libName, String libDescription, List<String> libLanguageCodes, String libCreator) {
        this.setLibId(libId);
        this.setLibDescription(libDescription);
        this.setLibName(libName);
        this.setLibLanguageCode(libLanguageCodes);
        this.setLibCreator(libCreator);
    }


    @Generated(hash = 559680945)
    public UserLibrary() {
    }

    //GETTER/SETTER -------------------------
    public String getLibName() {
        return libName;
    }

    public void setLibName(String libName) {
        this.libName = libName;
    }

    public List<String> getLibLanguageCodes() {
        return this.libLanguageCodes;
    }

    public void setLibLanguageCode(List<String> libLanguageCodes) {
        this.libLanguageCodes = libLanguageCodes;
    }

    public String getLibCreator() {
        return libCreator;
    }

    public void setLibCreator(String libCreator) {
        this.libCreator = libCreator;
    }

    public String getLibId() {
        return libId;
    }

    public void setLibId(String libId) {
        this.libId = libId;
    }

    // ########################## GREEN DAO METHODS ####################################

    /** Deletes userLib */
    public void delete(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getUserLibraryDao().delete(this);

        //Reload all countdowns from db bc. userLibs are cached
        Countdown.refreshAll(context);

        //Restart notificationservice
        ServiceMgr.restartNotificationService(context);
    }


    /** Saves new/Updates userLib */
    public void save(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getUserLibraryDao().insertOrReplace(this);
        //don't restart notification service, bc. new userLibs are not automatically assigned to a countdown
    }

    /** Queries all userLibs in Db. */
    public static List<UserLibrary> queryAll(@NonNull Context context) {
        return ((DaoApp) context.getApplicationContext()).getDaoSession().getUserLibraryDao().queryBuilder().list();
    }

    /** Queries userLib. */
    public static UserLibrary query(@NonNull Context context, @NonNull String libId) {
        UserLibraryDao userLibraryDao = ((DaoApp) context.getApplicationContext()).getDaoSession().getUserLibraryDao();
        List<UserLibrary> userLibraryList = userLibraryDao.queryBuilder()
                .where(UserLibraryDao.Properties.LibId.eq(libId))
                .list();

        if (userLibraryList.size() <= 0) {
            return null;
        } else {
            return userLibraryList.get(0); //bc. of primary key only one element
        }
    }

    public String getLibDescription() {
        return libDescription;
    }

    public void setLibDescription(String libDescription) {
        this.libDescription = libDescription;
    }



    public void setLibLanguageCodes(List<String> libLanguageCodes) {
        this.libLanguageCodes = libLanguageCodes;
    }
}
