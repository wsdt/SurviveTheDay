package kevkevin.wsdt.tagueberstehen.classes.entities;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm.GreenDaoConverter;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ZT_UserLibraryLanguageCode {
    @Id //Bc. greenDao does not support multiple column primaryKeys
    private long zullcPk;

    @Index
    private String libId; //hash
    @Index
    private String lcKuerzel; //e.g. for

    //Contains e.g. all quotes/jokes etc.
    @Convert(converter = GreenDaoConverter.class,columnType = String.class)
    private List<String> lines; //for null if in dbmgr
    private long libCreatedDateTime;
    private long libLastEditDateTime;

    @Generated(hash = 1094874426)
    public ZT_UserLibraryLanguageCode(long zullcPk, String libId, String lcKuerzel,
            List<String> lines, long libCreatedDateTime, long libLastEditDateTime) {
        this.zullcPk = zullcPk;
        this.libId = libId;
        this.lcKuerzel = lcKuerzel;
        this.lines = lines;
        this.libCreatedDateTime = libCreatedDateTime;
        this.libLastEditDateTime = libLastEditDateTime;
    }

    @Generated(hash = 420750711)
    public ZT_UserLibraryLanguageCode() {
    }

    //GETTER/SETTER --------------------------------------------------------
    public long getZullcPk() {
        return zullcPk;
    }

    public void setZullcPk(long zullcPk) {
        this.zullcPk = zullcPk;
    }

    public String getLibId() {
        return libId;
    }

    public void setLibId(String libId) {
        this.libId = libId;
    }

    public String getLcKuerzel() {
        return lcKuerzel;
    }

    public void setLcKuerzel(String lcKuerzel) {
        this.lcKuerzel = lcKuerzel;
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
}
