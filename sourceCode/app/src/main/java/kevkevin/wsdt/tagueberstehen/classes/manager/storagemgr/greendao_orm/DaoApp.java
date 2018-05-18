package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm;

import android.app.Application;

import org.greenrobot.greendao.database.Database;

import kevkevin.wsdt.tagueberstehen.classes.entities.DaoMaster;
import kevkevin.wsdt.tagueberstehen.classes.entities.DaoSession;

/** Needed for initializing greendaoOrm and database. */
public class DaoApp extends Application implements IDaoApp {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? DB_NAME_ENCRYPTED : DB_NAME);
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb(DB_PWD) : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
