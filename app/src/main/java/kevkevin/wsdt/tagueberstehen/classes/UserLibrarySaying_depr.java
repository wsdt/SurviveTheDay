package kevkevin.wsdt.tagueberstehen.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

@Deprecated
public class UserLibrarySaying_depr {
    private static final String TAG = "UserLibrarySaying";
    private static SparseArray<UserLibrarySaying_depr> allUserSayings = new SparseArray<>();
    private static boolean currentlyLoadingAllQuotes = false; //to avoid multiple cursor allocations
    private int sayingId; //primary key
    private String sayingText;
    private String UserLibraryLbl; //foreign key

    public UserLibrarySaying_depr(@NonNull Context context, int sayingId, @NonNull String sayingText, @NonNull String languagePack) {
        this.setSayingId(sayingId);
        this.setSayingText(sayingText);
        this.setUserLibraryLbl(languagePack);
        //Quote.getAllUserLibraryLines().put(sayingId,this); //add to global static sayinglist
        UserLibrarySaying_depr.extractAllSayingsFromDb(context); //only downloads/extracts all sayings from db, if they are not already loaded
    }

    public static void extractAllSayingsFromDb(@NonNull Context context) { //if no quotes in db, this method might run every time!
        if (UserLibrarySaying_depr.allUserSayings.size() <= 0 && !currentlyLoadingAllQuotes) {
            Log.d(TAG, "extractAllSayingsFromDb: Trying to extract all sayings from db.");
            //Quotes not extracted now, doing it now.
            currentlyLoadingAllQuotes = true; //blocking other method calls
            UserLibrarySaying_depr.setAllUserSayings(DatabaseMgr.getSingletonInstance(context).getAllUserLibraryLines(context,false));
            currentlyLoadingAllQuotes = false; //now allowing it again
        }
    }

    public static UserLibrarySaying_depr getRandomSayingFromAll(@NonNull Context context) { //might return null if no quotes!
        return UserLibrarySaying_depr.getAllQuotes(context).get(HelperClass.getRandomInt(0, UserLibrarySaying_depr.getAllQuotes(context).size()-1));
    }

    @Override
    public String toString() {
        return "SAYING->"+this.getSayingId()+";"+this.getSayingText()+";"+this.getUserLibraryLbl();
    }

    //GETTER/SETTER ------------------------------------------
    public int getSayingId() {
        return sayingId;
    }

    public void setSayingId(int sayingId) {
        this.sayingId = sayingId;
    }

    public String getSayingText() {
        return sayingText;
    }

    public void setSayingText(String sayingText) {
        this.sayingText = sayingText;
    }

    public String getUserLibraryLbl() {
        return UserLibraryLbl;
    }

    public void setUserLibraryLbl(String userLibraryLbl) {
        this.UserLibraryLbl = userLibraryLbl;
    }


    public static SparseArray<UserLibrarySaying_depr> getAllQuotes(@NonNull Context context) {
        extractAllSayingsFromDb(context); //extract from db if not already done
        return allUserSayings;
    }

    public static void setAllUserSayings(SparseArray<UserLibrarySaying_depr> allUserSayings) {
        UserLibrarySaying_depr.allUserSayings = allUserSayings;
    }
}
