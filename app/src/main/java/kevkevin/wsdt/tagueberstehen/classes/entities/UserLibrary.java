package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONArray;

import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.DaoApp;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.GreenDaoConverter;
import kevkevin.wsdt.tagueberstehen.classes.services.ServiceMgr;

import org.greenrobot.greendao.DaoException;

@Entity (active = true) //active = true for getting generated methods
public class UserLibrary {
    @Transient
    private static final String TAG = "UserLibrary";
    @Id
    private String libId; //hashwert etc.
    private String libName;
    private String libLanguageCode;
    private String libCreator;
    private String libCreatedDateTime;
    private String libLastEditDateTime;

    //Contains e.g. all quotes/jokes etc.
    @Convert(converter = GreenDaoConverter.class,columnType = String.class)
    private List<String> lines; //for null if in dbmgr
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1814500198)
    private transient UserLibraryDao myDao;


    //For mapping from FirebaseStorMgr to Obj
    public UserLibrary(String libId, String libName, String libLanguageCode, String libCreator, String libCreatedDateTime, String libLastEditDateTime, JSONArray lines) {
        this.setLibId(libId);
        this.setLibName(libName);
        this.setLibLanguageCode(libLanguageCode);
        this.setLibCreator(libCreator);
        this.setLibCreatedDateTime(libCreatedDateTime);
        this.setLibLastEditDateTime(libLastEditDateTime);
        this.setLines(HelperClass.convertJsonArrayToList(lines));
    }

    @Generated(hash = 2088625479)
    public UserLibrary(String libId, String libName, String libLanguageCode, String libCreator, String libCreatedDateTime, String libLastEditDateTime, List<String> lines) {
        this.libId = libId;
        this.libName = libName;
        this.libLanguageCode = libLanguageCode;
        this.libCreator = libCreator;
        this.libCreatedDateTime = libCreatedDateTime;
        this.libLastEditDateTime = libLastEditDateTime;
        this.lines = lines;
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

    public String getLibLanguageCode() {
        return libLanguageCode;
    }

    public void setLibLanguageCode(String libLanguageCode) {
        this.libLanguageCode = libLanguageCode;
    }

    public String getLibCreator() {
        return libCreator;
    }

    public void setLibCreator(String libCreator) {
        this.libCreator = libCreator;
    }

    public String getLibCreatedDateTime() {
        return libCreatedDateTime;
    }

    public void setLibCreatedDateTime(String libCreatedDateTime) {
        this.libCreatedDateTime = libCreatedDateTime;
    }

    public String getLibLastEditDateTime() {
        return libLastEditDateTime;
    }

    public void setLibLastEditDateTime(String libLastEditDateTime) {
        this.libLastEditDateTime = libLastEditDateTime;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
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

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

   /** Updates userlib */
    public void update(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getUserLibraryDao().update(this);

        //Restart notification service
        ServiceMgr.restartNotificationService(context);
    }

    /** Saves new userLib */
    public void save(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getUserLibraryDao().save(this);
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

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1654970760)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserLibraryDao() : null;
    }
}
