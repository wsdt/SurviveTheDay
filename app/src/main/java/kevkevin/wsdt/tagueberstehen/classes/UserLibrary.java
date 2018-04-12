package kevkevin.wsdt.tagueberstehen.classes;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class UserLibrary {
    private static final String TAG = "UserLibrary";

    private int libId;
    private String libName;
    private String libLanguageCode;
    private String createdBy;
    private GregorianCalendar createdOn;
    private GregorianCalendar lastEditOn;

    //Contains e.g. all quotes/jokes etc.
    private List<UserLibraryLine> lines = new ArrayList<>(); //prevent nullpointer

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GregorianCalendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(GregorianCalendar createdOn) {
        this.createdOn = createdOn;
    }

    public GregorianCalendar getLastEditOn() {
        return lastEditOn;
    }

    public void setLastEditOn(GregorianCalendar lastEditOn) {
        this.lastEditOn = lastEditOn;
    }

    public List<UserLibraryLine> getLines() {
        return lines;
    }

    public void setLines(List<UserLibraryLine> lines) {
        this.lines = lines;
    }

    public int getLibId() {
        return libId;
    }

    public void setLibId(int libId) {
        this.libId = libId;
    }
}
