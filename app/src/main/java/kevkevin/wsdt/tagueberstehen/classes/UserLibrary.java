package kevkevin.wsdt.tagueberstehen.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

public class UserLibrary {
    private static final String TAG = "UserLibrary";

    private int libId;
    private String libName;
    private String libLanguageCode;
    private String createdBy;
    private String createdOn;
    private String lastEditOn;

    //Contains e.g. all quotes/jokes etc.
    private List<String> lines; //for null if in dbmgr


    //For mapping from FirebaseStorMgr to Obj
    public UserLibrary(int libId, String libName, String libLanguageCode, String createdBy, String createdOn, String lastEditOn, JSONArray lines) {
        this.setLibId(libId);
        this.setLibName(libName);
        this.setLibLanguageCode(libLanguageCode);
        this.setCreatedBy(createdBy);
        this.setCreatedOn(createdOn);
        this.setLastEditOn(lastEditOn);
        this.setLines(HelperClass.convertJsonArrayToList(lines));
    }

    public UserLibrary(int libId, String libName, String libLanguageCode, String createdBy, String createdOn, String lastEditOn, List<String> lines) {
        this.setLibId(libId);
        this.setLibName(libName);
        this.setLibLanguageCode(libLanguageCode);
        this.setCreatedBy(createdBy);
        this.setCreatedOn(createdOn);
        this.setLastEditOn(lastEditOn);
        this.setLines(lines);
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getLastEditOn() {
        return lastEditOn;
    }

    public void setLastEditOn(String lastEditOn) {
        this.lastEditOn = lastEditOn;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public int getLibId() {
        return libId;
    }

    public void setLibId(int libId) {
        this.libId = libId;
    }
}
