package kevkevin.wsdt.tagueberstehen.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

public class UserLibrary {
    private static final String TAG = "UserLibrary";
    private static List<UserLibrary> allDownloadedUserLibraries = new ArrayList<>(); //should only contain userLibObjs where data is saved into local db

    private int libId;
    private String libName;
    private String libLanguageCode;
    private String createdBy;
    private String createdOn;
    private String lastEditOn;

    //Contains e.g. all quotes/jokes etc.
    private List<UserLibraryLine> lines = new ArrayList<>(); //prevent nullpointer

    public UserLibrary(int libId, String libName, String libLanguageCode, String createdBy, String createdOn, String lastEditOn) {
        this.setLibId(libId);
        this.setLibName(libName);
        this.setLibLanguageCode(libLanguageCode);
        this.setCreatedBy(createdBy);
        this.setCreatedOn(createdOn);
        this.setLastEditOn(lastEditOn);
    }

    public static void extractAllUserLibrariesFromDb(@NonNull Context context) { //if no quotes in db, this method might run every time!
        if (UserLibrary.getAllDownloadedUserLibraries().size() <= 0 && !currentlyLoadingAllUserLibraries) {
            Log.d(TAG, "extractAllUserLibrariesFromDb: Trying to extract all userlibs from db.");
            //userLibs not extracted now, doing it now.
            UserLibrary.setAllDownloadedUserLibraries(DatabaseMgr.getSingletonInstance(context).getAllUserLibraries(context,false));
        }
    }


    //GETTER/SETTER -------------------------
    public static List<UserLibrary> getAllDownloadedUserLibraries() {
        return allDownloadedUserLibraries;
    }

    public static void setAllDownloadedUserLibraries(List<UserLibrary> allDownloadedUserLibraries) {
        UserLibrary.allDownloadedUserLibraries = allDownloadedUserLibraries;
    }

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
