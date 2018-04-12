package kevkevin.wsdt.tagueberstehen.classes;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

//TODO: Make to Libpack (transform!)
@Deprecated
public class UserLibrary_depr {
    private static final String TAG = "UserLibrary";
    private static HashMap<String,UserLibrary_depr> allUserLibraries = new HashMap<>(); //important that here declared
    private static boolean currentlyLoadingAllUserLibraries = false; //to avoid multiple cursor allocations
    private SparseArray<UserLibrarySaying_depr> userLibrarySayings;
    private String userLibraryId;

    public UserLibrary_depr(@NonNull Context context, @NonNull String UserLibraryId) {
        this.setUserLibraryId(UserLibraryId);
        //getAllUserLibraries().put(this.getUserLibraryId(),this); //add new instance to hashmap (with own id, by hashmap no double values possible)
        extractAllUserLibrariesFromDb(context); //download/extract all lpacks if not already done
    }

    public static void extractAllUserLibrariesFromDb(@NonNull Context context) { //if no quotes in db, this method might run every time!
        if (UserLibrary_depr.allUserLibraries.size() <= 0 && !currentlyLoadingAllUserLibraries) {
            Log.d(TAG, "extractAllUserLibrariesFromDb: Trying to extract all userlibs from db.");
            //Quotes not extracted now, doing it now.
            currentlyLoadingAllUserLibraries = true; //to block other method calls
            UserLibrary_depr.setAllUserLibraries(DatabaseMgr.getSingletonInstance(context).getAllUserLibraries(context,false));
            currentlyLoadingAllUserLibraries = false; //now allowing it again
        }
    }

    public String getLabelString(@NonNull Activity activity) {
        Resources res = activity.getResources();
        String labelStr = "";
        try {
            labelStr = String.format(res.getString(res.getIdentifier("customNotification_random_generic_texts_allArrays_Lbls_"+this.getUserLibraryId(), "string", activity.getPackageName())), getUserLibrarySayings(activity).size());
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getLabelString: Could not find userlib resource! Returned empty string.");
            e.printStackTrace();
        }
        return labelStr;
    }

    //GETTER/SETTER
    public String getUserLibraryId() {
        return userLibraryId;
    }

    public void setUserLibraryId(String userLibraryId) {
        this.userLibraryId = userLibraryId;
    }


    public static HashMap<String,UserLibrary_depr> getAllUserLibraries(@NonNull Context context) {
        extractAllUserLibrariesFromDb(context); //extract from db if not already done
        return allUserLibraries;
    }

    public static void setAllUserLibraries(HashMap<String,UserLibrary_depr> allUserLibraries) {
        UserLibrary_depr.allUserLibraries = allUserLibraries;
    }

    public void setUserLibrarySayings(SparseArray<UserLibrarySaying_depr> userLibrarySayings) {
        this.userLibrarySayings = userLibrarySayings;
    }

    public SparseArray<UserLibrarySaying_depr> getUserLibrarySayings(@NonNull Context context) {
        if (this.userLibrarySayings == null) {
            SparseArray<UserLibrarySaying_depr> quoteSparseArray = UserLibrarySaying_depr.getAllQuotes(context);
            SparseArray<UserLibrarySaying_depr> filteredQuoteList = new SparseArray<>();
            for (int i = 0; i < quoteSparseArray.size(); i++) {
                UserLibrarySaying_depr tmpQuote = quoteSparseArray.get(i);
                if (tmpQuote.getUserLibraryLbl().equals(this.getUserLibraryId())) {
                    filteredQuoteList.put(tmpQuote.getSayingId(), tmpQuote); //gaps might occur!!
                }
            }
            this.setUserLibrarySayings(filteredQuoteList); //set it now
        } //so if already not null, we dont have to do this again
        return this.userLibrarySayings;
    }
}
