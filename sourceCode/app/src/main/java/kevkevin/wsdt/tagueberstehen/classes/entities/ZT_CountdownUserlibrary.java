package kevkevin.wsdt.tagueberstehen.classes.entities;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.DaoApp;

/** Zwischentabelle (javaBean) for Countdown and UserLibrary since Greendao does not support N:M yet. */
@Entity
public class ZT_CountdownUserlibrary {
    @Id(autoincrement = true) //Bc. greenDao does not support multiple column primaryKeys
    private Long zcuPk;

    @Index
    private Long couId;
    @Index
    private String libId; //will be a hash or similar

    public ZT_CountdownUserlibrary(Long couId, String libId) {
        this.setCouId(couId);
        this.setLibId(libId);
    }

    @Generated(hash = 993426623)
    public ZT_CountdownUserlibrary(Long zcuPk, Long couId, String libId) {
        this.zcuPk = zcuPk;
        this.couId = couId;
        this.libId = libId;
    }

    @Generated(hash = 1280865593)
    public ZT_CountdownUserlibrary() {
    }

    //GETTER/SETTER --------------------------------
    public String getLibId() {
        return libId;
    }

    public void setLibId(String libId) {
        this.libId = libId;
    }

    public Long getCouId() {
        return this.couId;
    }

    public void setCouId(Long couId) {
        this.couId = couId;
    }

    public Long getZcuPk() {
        return this.zcuPk;
    }

    public void setZcuPk(Long zcuPk) {
        this.zcuPk = zcuPk;
    }

    // DB OPERATIONS ----------------------------------------------------------------
    /**
     * Saves new/Updates zt_entry
     */
    public void save(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_CountdownUserlibraryDao().insertOrReplace(this);
    }

    public void delete(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_CountdownUserlibraryDao().delete(this);
    }

    public void deleteAll(@NonNull Context context) {
        ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_CountdownUserlibraryDao().deleteAll();
    }

    /**
     * Queries userLibPackage.
     */
    public static List<ZT_CountdownUserlibrary> query(@NonNull Context context, @NonNull Long couId) {
        ZT_CountdownUserlibraryDao dao = ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_CountdownUserlibraryDao();
        return dao.queryBuilder()
                .where(ZT_CountdownUserlibraryDao.Properties.CouId.eq(couId))
                .list();
    }

    public static List<ZT_CountdownUserlibrary> query(@NonNull Context context, @NonNull String libId) {
        ZT_CountdownUserlibraryDao dao = ((DaoApp) context.getApplicationContext()).getDaoSession().getZT_CountdownUserlibraryDao();
        return dao.queryBuilder()
                .where(ZT_CountdownUserlibraryDao.Properties.LibId.eq(libId))
                .list();
    }
}
