package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.DaoApp;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.GreenDaoConverter;
import kevkevin.wsdt.tagueberstehen.classes.services.Kickstarter_BootAndGeneralReceiver;
import kevkevin.wsdt.tagueberstehen.classes.services.ServiceMgr;

import org.greenrobot.greendao.annotation.Generated;

import static kevkevin.wsdt.tagueberstehen.classes.services.interfaces.IKickstart_BootAndGeneralReceiver.BROADCASTRECEIVER_ACTION_RESTART_ALL_SERVICES;

@Entity
public class ZT_UserLibraryLanguagePack {
    @Id (autoincrement = true) //Bc. greenDao does not support multiple column primaryKeys
    private Long zullcPk;

    @Index
    private String libId; //hash
    @Index
    private String lpKuerzel; //e.g. for

    //Contains e.g. all quotes/jokes etc.
    @Convert(converter = GreenDaoConverter.class,columnType = String.class)
    private List<String> lines; //for null if in dbmgr
    private long libCreatedDateTime;
    private long libLastEditDateTime;

    public ZT_UserLibraryLanguagePack(String libId, String lpKuerzel, List<String> lines, long libCreatedDateTime, long libLastEditDateTime) {
        this.libId = libId;
        this.lpKuerzel = lpKuerzel;
        this.lines = lines;
        this.libCreatedDateTime = libCreatedDateTime;
        this.libLastEditDateTime = libLastEditDateTime;
    }


    @Generated(hash = 845659874)
    public ZT_UserLibraryLanguagePack() {
    }

    @Generated(hash = 1004911821)
    public ZT_UserLibraryLanguagePack(Long zullcPk, String libId, String lpKuerzel, List<String> lines, long libCreatedDateTime,
            long libLastEditDateTime) {
        this.zullcPk = zullcPk;
        this.libId = libId;
        this.lpKuerzel = lpKuerzel;
        this.lines = lines;
        this.libCreatedDateTime = libCreatedDateTime;
        this.libLastEditDateTime = libLastEditDateTime;
    }

    //GETTER/SETTER --------------------------------------------------------
    public Long getZullcPk() {
        return zullcPk;
    }

    public void setZullcPk(Long zullcPk) {
        this.zullcPk = zullcPk;
    }

    public String getLibId() {
        return libId;
    }

    public void setLibId(String libId) {
        this.libId = libId;
    }

    public String getLpKuerzel() {
        return lpKuerzel;
    }

    public void setLpKuerzel(String lpKuerzel) {
        this.lpKuerzel = lpKuerzel;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public long getLibCreatedDateTime() {
        return libCreatedDateTime;
    }

    public void setLibCreatedDateTime(long libCreatedDateTime) {
        this.libCreatedDateTime = libCreatedDateTime;
    }

    public long getLibLastEditDateTime() {
        return libLastEditDateTime;
    }

    public void setLibLastEditDateTime(long libLastEditDateTime) {
        this.libLastEditDateTime = libLastEditDateTime;
    }

    // DB OPERATIONS
    /**
     * Saves new/Updates userLibPackage
     */
    public void save(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_UserLibraryLanguagePackDao().insertOrReplace(this);
    }

    public void delete(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_UserLibraryLanguagePackDao().delete(this);
    }

    public void deleteAll(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_UserLibraryLanguagePackDao().deleteAll();
    }

    /**
     * Queries userLibPackage.
     */
    public static ZT_UserLibraryLanguagePack query(@NonNull Context context, @NonNull String libId, @NonNull String lpKuerzel) {
        ZT_UserLibraryLanguagePackDao dao = ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_UserLibraryLanguagePackDao();
        List<ZT_UserLibraryLanguagePack> lpList = dao.queryBuilder()
                .where(ZT_UserLibraryLanguagePackDao.Properties.LibId.eq(libId),ZT_UserLibraryLanguagePackDao.Properties.LpKuerzel.eq(lpKuerzel))
                .list();

        if (lpList.size() <= 0) {
            return null;
        } else {
            return lpList.get(0); //not primary key, but nevertheless libId + languagePack should be also unique!
        }
    }

    public static List<ZT_UserLibraryLanguagePack> query(@NonNull Context context, @NonNull String libId) {
        ZT_UserLibraryLanguagePackDao dao = ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_UserLibraryLanguagePackDao();
        return dao.queryBuilder()
                .where(ZT_UserLibraryLanguagePackDao.Properties.LibId.eq(libId))
                .list();
    }


}
