package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm;

import android.app.Application;

import org.greenrobot.greendao.database.Database;

import kevkevin.wsdt.tagueberstehen.classes.entities.DaoMaster;
import kevkevin.wsdt.tagueberstehen.classes.entities.DaoSession;

/** Needed for initializing greendaoOrm and database. */
public class DaoApp extends Application {
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher */
    public static final boolean ENCRYPTED = true;
    private static final String DB_PWD = "H6#Ä!u0q2R$qB5@";

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "std-db-encrypted" : "std-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb(DB_PWD) : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
