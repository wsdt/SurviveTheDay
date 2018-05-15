package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.annotations.Bug;
import kevkevin.wsdt.tagueberstehen.annotations.Enhance;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.entities.UserLibrary;
import kevkevin.wsdt.tagueberstehen.classes.manager.DialogMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_FirebaseStorageMgr;
import kevkevin.wsdt.tagueberstehen.interfaces.IConstants_Global;

//IMPORTANT: FirebaseObjs TO JavaObjs TO SQL and reverse
public class FirebaseStorageMgr {
    private static final String TAG = "FirebaseStorageMgr";
    /**
     * both get declared by calling getter (when they are null)
     */
    private static FirebaseStorage firebaseStorage;
    private static StorageReference storageReference;


    /* #########################################################################################################
     * TODO: ########## UPLOAD PROCEDURES ###########################################################################
     * ##########################################################################################################*/

    /** IMPORTANT: This method should be uptodate, with the current used json version code!
     * Json-Versioncode (/v1/ or /v2/ as folder on Firebase, which can have completely different structures). */
    private static JSONObject mapUserLibraryObjToJson(@NonNull UserLibrary userLibrary) {
        Log.d(TAG, "mapUserLibraryObjToJson: Is current version of json correct?");
        //TODO:
        return null;
    }


    /* #########################################################################################################
    * ########## DOWNLOAD PROCEDURES ###########################################################################
    * ##########################################################################################################*/
    /** For listing multiple packages without downloading huge files, we need to craft some index-files.
     * I decided to make for each language one specific index file, so they also don't get too big. So
     * users can the language filtering causes also a performance improvement bc. less index-files need
     * to be downloaded.
     *
     * @param languageCode: e.g. "en", "de" or similar [needs to be equal to the folder-name on Firebase!
     * */
    private static void downloadIndexFile(@NonNull String languageCode) {
        //TODO: maybe reuse downloadNewPackage()
        //TODO: Before making the upload or index methods, please solve bug of downloadDefaultData()!
    }



    /**
     * Download default userLibs (e.g.), but do this only once at the first time the app is called (versionized)
     * Could be also called, if lastEditOn is not the same as the downloadedLibs. (do not use a separate version in Json!)
     */
    @Bug(problem = "We cannot download here multiple packages at the same time (maybe because of the thread " +
            "in saveNewPackage() --> but necessary [networkonmainthreadexception]). Both packages are successfully " +
            "saved, but the second one does not contain any libraryLines :(",
            priority = Bug.Priority.HIGH, byDeveloper = IConstants_Global.DEVELOPERS.WSDT)
    @Enhance (message = "To prevent unneccessary errors bc. NO userlibrary is installed (bad usability), just keep" +
            "a local copy of one default library, so the app can work and then SAVE IT AS JSON HERE INTO Db INSTEAD OF " +
            "DOWNLOADING.")
    /*public static void downloadDefaultData(@NonNull Context context) {
        final GlobalAppSettingsMgr globalAppSettingsMgr = new GlobalAppSettingsMgr(context);
        if (!globalAppSettingsMgr.isFirebaseDefaultDataAlreadyDownloaded() && HelperClass.isNetworkAvailable(context)) {
            Toast.makeText(context, R.string.firebaseStorageMgr_install_userlibrary_defaultdata, Toast.LENGTH_SHORT).show();

            //By default only following data (so users have to download desired firebase libs)
            //FirebaseStorageMgr.downloadNewPackage(context, "quotes_en." + IConstants_FirebaseStorageMgr.LIB_FILEEXTENSION);

            FirebaseStorageMgr.downloadNewPackage(context, IConstants_FirebaseStorageMgr.LIB_JSON_VERSION_FOLDER + "/en/quotes/quotes." + IConstants_FirebaseStorageMgr.LIB_FILEEXTENSION, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                @Override
                public void success_is_true() {
                    //do this only if it was really successful (so also real failure did not happen)
                    globalAppSettingsMgr.setFirebaseDefaultDataAlreadyDownloaded(true); //is only set if internet was available and not already set
                    Log.d(TAG, "downloadDefaultData: Downloaded default data.");
                }

                @Override
                public void failure_is_false() {
                    Log.e(TAG, "downloadDefaultData: Could not download default data!");
                }
            });
        }
    }*/
    public static UserLibrary saveDefaultUserLibrary(@NonNull Context context) {
        //This method has no validation whether default user lib was already downloaded!
        UserLibrary userLibrary = null;
        try {
            userLibrary = FirebaseStorageMgr.mapJsonToUserLibraryObj(context, new JSONObject(IConstants_FirebaseStorageMgr.DEFAULT.LIB_JSON_DEFAULT));
            userLibrary.save(context);
            Log.d(TAG, "saveDefaultUserLibrary: Tried to save default user lib.");
        } catch (JSONException e) {
            //THIS SHOULD NEVER HAPPEN
            //TODO: Maybe inform user
            Log.e(TAG, "saveDefaultUserLibrary: Could not save default user library.");
            e.printStackTrace();
        }
        return userLibrary;
    }

    public static void downloadNewPackage(@NonNull final Context context, @NonNull String relChildPath, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        final StorageReference childFileReference = getStorageReference(context).child(relChildPath);

        childFileReference.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "downloadNewPackage:onSuccess: Got valid stream from firebase.");
                saveNewPackage(context, taskSnapshot.getStream());
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true();}
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "downloadNewPackage:onFailure: Could not download new package->\n* " + e.getLocalizedMessage() + "\n* " + e.getMessage() + "\n* " + e.getCause() + "\n * Library: "+childFileReference.getPath());
                e.printStackTrace();

                //Show error dialog, if context is not an activity we will only show a toast as error msg.
                Resources res = context.getResources();
                String failureMsgDescription = String.format(res.getString(R.string.firebaseStorageMgr_install_userlibrary_failuremsg_description),childFileReference.getName())+" \n\nFehlerbeschreibung: \n'"+e.getLocalizedMessage()+"'";
                if (context instanceof Activity) {
                    (new DialogMgr((Activity) context)).showDialog_Generic(res.getString(R.string.firebaseStorageMgr_install_userlibrary_failuremsg_title),
                            failureMsgDescription,null,"",-1,null);
                } else {
                    Log.w(TAG, "downloadNewPackage:onFailure: Could not show failure dialog, because context is not an activity. Showing toast instead.");
                    Toast.makeText(context, failureMsgDescription, Toast.LENGTH_SHORT).show();
                }
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();}
            }
        });
    }

    private static UserLibrary mapJsonToUserLibraryObj(@NonNull Context context, @NonNull JSONObject userLibraryJSONObject) {
        Log.d(TAG, "mapJsonToUserLibraryObj: Trying to save userLibrary.");
        UserLibrary userLibrary = null;

        try {
            userLibrary = new UserLibrary(
                    userLibraryJSONObject.getString("libId"),
                    userLibraryJSONObject.getString("libName"),
                    userLibraryJSONObject.getString("libLanguageCode"),
                    userLibraryJSONObject.getString("createdBy"),
                    userLibraryJSONObject.getString("createdOn"),
                    userLibraryJSONObject.getString("lastEditOn"),
                    userLibraryJSONObject.getJSONArray("lines"));
        } catch (JSONException e) {
            Log.e(TAG, "mapJsonToUserLibraryObj: Could not extract Userlibrary from json. Json malformed!");
            e.printStackTrace();
        }

        return userLibrary;
    }

    private static void saveNewPackage(@NonNull final Context context, final InputStream fis) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner sc = null;

                try {
                    StringBuilder jsonStr = new StringBuilder();
                    sc = new Scanner(fis, "UTF-8");
                    while (sc.hasNextLine()) {
                        // convert to char and display it
                        jsonStr.append(sc.nextLine());
                    }
                    UserLibrary userLibrary = mapJsonToUserLibraryObj(context, new JSONObject(jsonStr.toString()));

                    //save to db
                    userLibrary.save(context);

                } catch (JSONException e) {
                    Log.e(TAG, "saveNewPackage: Could not parse downloaded userLibrary to JsonObj.");
                    e.printStackTrace();
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                        if (sc != null) {
                            sc.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //GETTER/SETTER --------------------
    public static FirebaseStorage getFirebaseStorage(@NonNull Context context) {
        if (firebaseStorage == null) {
            FirebaseApp.initializeApp(context);
            setFirebaseStorage(FirebaseStorage.getInstance());
            Log.d(TAG, "getFirebaseStorage: Firebasestorage was null. Created new one.");
        }
        return firebaseStorage;
    }

    public static void setFirebaseStorage(FirebaseStorage firebaseStorage) {
        FirebaseStorageMgr.firebaseStorage = firebaseStorage;
    }

    public static StorageReference getStorageReference(@NonNull Context context) {
        if (storageReference == null) {
            setStorageReference(getFirebaseStorage(context).getReference());
            Log.d(TAG, "getStorageReference: StorageReference was null. Created new one.");
        }
        return storageReference;
    }

    public static void setStorageReference(StorageReference storageReference) {
        FirebaseStorageMgr.storageReference = storageReference;
    }
}
