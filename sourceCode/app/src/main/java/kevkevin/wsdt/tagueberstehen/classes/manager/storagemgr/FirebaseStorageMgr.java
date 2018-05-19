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
import kevkevin.wsdt.tagueberstehen.annotations.Test;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.entities.UserLibrary;
import kevkevin.wsdt.tagueberstehen.classes.manager.DialogMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IFirebaseStorageMgr;
import kevkevin.wsdt.tagueberstehen.interfaces.IGlobal;

@Test(message = "Improved cohesion, which resulted in many methods. Please test them.", priority = Test.Priority.HIGH,
    byDeveloper = IGlobal.DEVELOPERS.WSDT)
public class FirebaseStorageMgr {
    private static final String TAG = "FirebaseStorageMgr";
    /**
     * both get declared by calling getter (when they are null)
     */
    private static FirebaseStorage firebaseStorage;
    private static StorageReference storageReference;

    /**
     * Helpermethod for extracting downloadedJsonObj from var args.
     */
    private static JSONObject parseJsonObjFromArgs(@Nullable Object... args) {
        JSONObject dowloadedIndexFile = null;
        if (args != null && args[0] instanceof JSONObject) {
            //if not null at least one param has to be supplied, so no length validation
            dowloadedIndexFile = (JSONObject) args[0]; //if you want to use indexFile then you can do it here (otherwise it will be used in nested successTrue().
        } else {
            Log.e(TAG, "downloadIndexFile: Provided args might be null or wrong parsed-->" + args); //Implicit toString call to avoid nullPointer
        }
        return dowloadedIndexFile;
    }

    /* #########################################################################################################
     * TODO: ########## UPLOAD PROCEDURES ###########################################################################
     * ##########################################################################################################*/

    /** TODO: https://stackoverflow.com/questions/33033418/android-get-user-id-without-requiring-scary-for-user-permissions
     * --> FOR NEW USER LIBRARIES */


    /**
     * IMPORTANT: This method should be uptodate, with the current used json version code!
     * Json-Versioncode (/v1/ or /v2/ as folder on Firebase, which can have completely different structures).
     */
    private static JSONObject mapUserLibraryObjToJson(@NonNull UserLibrary userLibrary) {
        Log.d(TAG, "mapUserLibraryObjToJson: Is current version of json correct?");
        //TODO:
        return null;
    }


    /* #########################################################################################################
     * ########## DOWNLOAD PROCEDURES ###########################################################################
     * ##########################################################################################################*/
    private static void downloadFile(@NonNull Context context, @NonNull String storageReference, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        getStorageReference(context).child(storageReference)
                .getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "downloadFile:onSuccess: Got valid stream from firebase.");
                final InputStream fis = taskSnapshot.getStream();

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

                            /** Execute successMethod with downloadedJson. (don't forget to validate whether it is null or valid) */
                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(new JSONObject(jsonStr.toString()));
                            }

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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "downloadFile:onFailure: Could not download new package->\n* " + e.getLocalizedMessage() + "\n* " + e.getMessage() + "\n* " + e.getCause());
                e.printStackTrace();
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                }
            }
        });
    }


    /**
     * For listing multiple packages without downloading huge files, we need to craft some index-files.
     * I decided to make for each language one specific index file, so they also don't get too big. So
     * users can the language filtering causes also a performance improvement bc. less index-files need
     * to be downloaded.
     *
     * @param languageCode:                                             e.g. "en", "de" or similar [needs to be equal to the folder-name on Firebase!
     * @param executeIfTrueSuccess_or_ifFalseFailure_afterCompletation: After file has be downloaded what do you want to do additionally to regular index procedures?
     */
    public static void downloadIndexFile(@NonNull Context context, @NonNull String languageCode, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        /** Nesting provided ExecuteIfTrueSuccess_OR_IfFalseFailure into new interface, bc. so we can merge custom indexFile-Procedures with provided ones.*/
        downloadFile(context,
                languageCode + IFirebaseStorageMgr.INDEX_FILES.FILENAME + IFirebaseStorageMgr.RES_FILE_EXTENSION, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true(@Nullable Object... args) {
                        /** You can provide here a procedure for all index files. If you just want to execute procedures for specific indexFiles just
                         * provide it to following success-Method (at the bottom of this method). Same principle for failure_is_false(). */

                        /** What to do with indexFile (showing or similar) */
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(parseJsonObjFromArgs(args));
                        }
                    }

                    @Override
                    public void failure_is_false(@Nullable Object... args) {
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                        }
                    }
                });
    }

    @Bug(problem = "We cannot download here multiple packages at the same time (maybe because of the thread " +
            "in saveNewPackage() --> but necessary [networkonmainthreadexception]). Both packages are successfully " +
            "saved, but the second one does not contain any libraryLines :(",
            priority = Bug.Priority.HIGH, byDeveloper = IGlobal.DEVELOPERS.WSDT)
    @Enhance(message = "To prevent unneccessary errors bc. NO userlibrary is installed (bad usability), just keep" +
            "a local copy of one default library, so the app can work and then SAVE IT AS JSON HERE INTO Db INSTEAD OF " +
            "DOWNLOADING.")
    public static void downloadUserLibrary(@NonNull final Context context, @NonNull final String relChildPath, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        downloadFile(context, relChildPath, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {

                /** Custom procedure for specific UserLibrary. */
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(parseJsonObjFromArgs(args));
                }
            }

            @Override
            public void failure_is_false(@Nullable Object... args) {
                Log.e(TAG, "downloadUserLibrary:failure_is_false: Could not download new package->\n* " + relChildPath);

                /** Custom error procedure for specific userLibrary. */
                if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                    executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                }
            }
        });
    }

    public static void saveUserLibrary(@NonNull final Context context, @NonNull final String relChildPath, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        downloadUserLibrary(context, relChildPath, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {
                UserLibrary userLibrary = mapJsonToUserLibraryObj(parseJsonObjFromArgs(args));
                if (userLibrary != null) {
                    userLibrary.save(context);
                    Log.d(TAG, "saveUserLibrary: Library has been saved.");
                } else {
                    /** Download failed */
                    if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                        executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                    }
                }
            }

            @Override
            public void failure_is_false(@Nullable Object... args) {
                /** Show following error msgs for all failures for all userLibraries.*/
                //Show error dialog, if context is not an activity we will only show a toast as error msg.
                Resources res = context.getResources();
                String failureMsgDescription = String.format(res.getString(R.string.firebaseStorageMgr_install_userlibrary_failuremsg_description), relChildPath);
                if (context instanceof Activity) {
                    (new DialogMgr((Activity) context)).showDialog_Generic(res.getString(R.string.firebaseStorageMgr_install_userlibrary_failuremsg_title),
                            failureMsgDescription, null, "", -1, null);
                } else {
                    Log.w(TAG, "downloadNewPackage:onFailure: Could not show failure dialog, because context is not an activity. Showing toast instead.");
                    Toast.makeText(context, failureMsgDescription, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private static UserLibrary mapJsonToUserLibraryObj(@Nullable JSONObject userLibraryJSONObject) {
        Log.d(TAG, "mapJsonToUserLibraryObj: Trying to save userLibrary.");
        if (userLibraryJSONObject == null) {
            return null;
        }

        UserLibrary userLibrary = null;

        try {
            userLibrary = new UserLibrary(
                    userLibraryJSONObject.getString("libId"),
                    userLibraryJSONObject.getString("libDescription"),
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


    @Deprecated
    public static UserLibrary saveDefaultUserLibrary(@NonNull Context context) {
        //This method has no validation whether default user lib was already downloaded!
        UserLibrary userLibrary = null;
        try {
            userLibrary = FirebaseStorageMgr.mapJsonToUserLibraryObj(new JSONObject(IFirebaseStorageMgr.DEFAULT.LIB_JSON_DEFAULT));
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
