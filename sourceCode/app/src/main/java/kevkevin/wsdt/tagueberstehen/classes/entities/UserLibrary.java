package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.DaoApp;
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

    @ToMany
    @JoinEntity(entity = ZT_UserLibraryLanguagePack.class,
            sourceProperty = "libId", targetProperty = "lpKuerzel") //for N:M
    private List<LanguagePack> libLanguagePacks;
    private String libCreator;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1814500198)
    private transient UserLibraryDao myDao;
    //For mapping from FirebaseStorMgr to Obj
    @Keep
    public UserLibrary(@NonNull String libId, String libName, String libDescription, List<LanguagePack> libLanguagePacks, String libCreator) {
        this.setLibId(libId);
        this.setLibDescription(libDescription);
        this.setLibName(libName);
        this.setLibLanguagePacks(libLanguagePacks);
        this.setLibCreator(libCreator);
    }


    @Generated(hash = 559680945)
    public UserLibrary() {
    }


    @Generated(hash = 670779116)
    public UserLibrary(String libId, String libName, String libDescription, String libCreator) {
        this.libId = libId;
        this.libName = libName;
        this.libDescription = libDescription;
        this.libCreator = libCreator;
    }

    //GETTER/SETTER -------------------------
    public String getLibName() {
        return libName;
    }

    public void setLibName(String libName) {
        this.libName = libName;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1455986525)
    public List<LanguagePack> getLibLanguagePacks() {
        if (libLanguagePacks == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LanguagePackDao targetDao = daoSession.getLanguagePackDao();
            List<LanguagePack> libLanguagePacksNew = targetDao._queryUserLibrary_LibLanguagePacks(libId);
            synchronized (this) {
                if (libLanguagePacks == null) {
                    libLanguagePacks = libLanguagePacksNew;
                }
            }
        }
        return libLanguagePacks;
    }


    public void setLibLanguagePacks(List<LanguagePack> libLanguagePacks) {
        this.libLanguagePacks = libLanguagePacks;
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

    public String getLibDescription() {
        return libDescription;
    }

    public void setLibDescription(String libDescription) {
        this.libDescription = libDescription;
    }

    // ########################## GREEN DAO METHODS ####################################

    /** Helper method for getting allLines of userLibrary (all languagePacks)
     * If you want a specific languagePack just use the query-Method of ZT_UserLibraryLanguagePack itself.*/
    public List<String> getAllLines(@NonNull Context context) {
        List<ZT_UserLibraryLanguagePack> allLanguagePacks = ZT_UserLibraryLanguagePack.query(context,this.getLibId());

        //TODO: Maybe make to member, so we do not have to do this all the time.
        List<String> allLines = new ArrayList<>();
        for (ZT_UserLibraryLanguagePack zt_userLibraryLanguagePack : allLanguagePacks) {
            allLines.addAll(zt_userLibraryLanguagePack.getLines());
        }
        return allLines;
    }


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


    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 855975047)
    public synchronized void resetLibLanguagePacks() {
        libLanguagePacks = null;
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
