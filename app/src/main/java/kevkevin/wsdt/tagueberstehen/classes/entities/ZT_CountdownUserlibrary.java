package kevkevin.wsdt.tagueberstehen.classes.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

/** Zwischentabelle (javaBean) for Countdown and UserLibrary since Greendao does not support N:M yet. */
@Entity
public class ZT_CountdownUserlibrary {
    @Id //Bc. greenDao does not support multiple column primaryKeys
    private long zcuPk;

    @Index
    private int couId;
    @Index
    private String libId; //will be a hash or similar

    @Generated(hash = 273848882)
    public ZT_CountdownUserlibrary(long zcuPk, int couId, String libId) {
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

    public int getCouId() {
        return this.couId;
    }

    public void setCouId(int couId) {
        this.couId = couId;
    }

    public long getZcuPk() {
        return this.zcuPk;
    }

    public void setZcuPk(long zcuPk) {
        this.zcuPk = zcuPk;
    }
}
