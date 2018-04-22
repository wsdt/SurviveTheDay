package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

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

import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.UserLibrary;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_FirebaseStorageMgr;

//IMPORTANT: FirebaseObjs TO JavaObjs TO SQL and reverse
public class FirebaseStorageMgr {
    private static final String TAG = "FirebaseStorageMgr";
    /**
     * both get declared by calling getter (when they are null)
     */
    private static FirebaseStorage firebaseStorage;
    private static StorageReference storageReference;

    //TODO: create method for downloading (later if it works well implement uploading (after testing downloading) --> so just upload both language packs)


    /**
     * Download default userLibs (e.g.), but do this only once at the first time the app is called (versionized)
     * Could be also called, if lastEditOn is not the same as the downloadedLibs. (do not use a separate version in Json!)
     */
    public static void downloadDefaultData(@NonNull Context context) {
        GlobalAppSettingsMgr globalAppSettingsMgr = new GlobalAppSettingsMgr(context);

        if (!globalAppSettingsMgr.isFirebaseDefaultDataAlreadyDownloaded() && HelperClass.isNetworkAvailable(context)) {
            //By default only following data (so users have to download desired firebase libs)
            //FirebaseStorageMgr.downloadNewPackage(context, "quotes_en." + IConstants_FirebaseStorageMgr.LIB_FILEEXTENSION);
            FirebaseStorageMgr.downloadNewPackage(context, "quotes_de." + IConstants_FirebaseStorageMgr.LIB_FILEEXTENSION);
            //TODO (BUG): We cannot download here multiple packages at the same time (maybe because of the thread in saveNewPackage() --> but necessary [networkonmainthreadexception])
            //TODO (BUG-Explanation): Both packages are successfully saved, but the second one does not contain any libraryLines :(

            globalAppSettingsMgr.setFirebaseDefaultDataAlreadyDownloaded(true); //is only set if internet was available and not already set
            Log.d(TAG, "downloadDefaultData: Downloaded default data.");
        }
    }

    public static void downloadNewPackage(@NonNull final Context context, @NonNull String relChildPath) {
        StorageReference childFileReference = getStorageReference(context).child(relChildPath);

        childFileReference.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "downloadNewPackage:onSuccess: Got valid stream from firebase.");
                saveNewPackage(context, taskSnapshot.getStream());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "downloadNewPackage:onFailure: Could not download new package->\n* " + e.getLocalizedMessage() + "\n* " + e.getMessage() + "\n* " + e.getCause() + "\n *" + e.getStackTrace().toString());
            }
        });
    }

    private static UserLibrary mapFileToUserLibraryObj(@NonNull JSONObject userLibraryJSONObject) {
        Log.d(TAG, "mapFileToUserLibraryObj: Trying to save userLibrary.");
        UserLibrary userLibrary = null;

        try {
            userLibrary = new UserLibrary(
                    userLibraryJSONObject.getInt("libId"),
                    userLibraryJSONObject.getString("libName"),
                    userLibraryJSONObject.getString("libLanguageCode"),
                    userLibraryJSONObject.getString("createdBy"),
                    userLibraryJSONObject.getString("createdOn"),
                    userLibraryJSONObject.getString("lastEditOn"),
                    userLibraryJSONObject.getJSONArray("lines"));
        } catch (JSONException e) {
            Log.e(TAG, "mapFileToUserLibraryObj: Could not extract Userlibrary from json. Json malformed!");
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
                    UserLibrary userLibrary = mapFileToUserLibraryObj(new JSONObject(jsonStr.toString()));
                    DatabaseMgr.getSingletonInstance(context).saveUserLibrary(context, userLibrary); //save to db

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
