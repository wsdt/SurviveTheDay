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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import kevkevin.wsdt.tagueberstehen.R;
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
     * Do not put FirebaseStorage obj as class members (not as static nor non-static)
     * otherwise we can't download multiple files at the same time.
     */

    /**
     * Helpermethod for extracting downloadedJsonObj from var args.
     * When you want to extract multiple indizes, then write one by yourself.
     *
     * DownloadFile() typically returns StorageReference in args[0] and JsonObj in args[1]
     */
    private static JSONObject parseJsonObjFromArgs(@Nullable Object arg) {
        JSONObject dowloadedIndexFile = null;
        if (arg != null && arg instanceof JSONObject) {
            //if not null at least one param has to be supplied, so no length validation
            dowloadedIndexFile = (JSONObject) arg; //if you want to use indexFile then you can do it here (otherwise it will be used in nested successTrue().
        } else {
            Log.e(TAG, "downloadIndexFile: Provided args might be null or wrong parsed-->" + arg); //Implicit toString call to avoid nullPointer
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

    /** Generic Method for downloading files from firebase. */
    private static void downloadFile(@NonNull Context context, @NonNull String strStorageReference, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        final StorageReference storageReference = getStorageReference(context);

        storageReference.child(strStorageReference)
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
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(storageReference, new JSONObject(jsonStr.toString()));
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
     * @param creationMM_YYYY: In which month and year the userLibraries were created. (MM_YYYY --> 02_2018)
     * @param executeIfTrueSuccess_or_ifFalseFailure_afterCompletation: After file has be downloaded what do you want to do additionally to regular index procedures?
     */
    public static void downloadIndexFile(@NonNull Context context, @NonNull String creationMM_YYYY, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        /** Nesting provided ExecuteIfTrueSuccess_OR_IfFalseFailure into new interface, bc. so we can merge custom indexFile-Procedures with provided ones.*/
        downloadFile(context,
                IFirebaseStorageMgr.LIB_JSON_VERSION_FOLDER +"\\"+
				IFirebaseStorageMgr.INDEX_FILES.FILENAME + creationMM_YYYY+"\\"+ /* e.g. __index_02_2018 */
				IFirebaseStorageMgr.RES_FILE_EXTENSION, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true(@Nullable Object... args) {
                        /** You can provide here a procedure for all index files. If you just want to execute procedures for specific indexFiles just
                         * provide it to following success-Method (at the bottom of this method). Same principle for failure_is_false(). */

                        /** What to do with indexFile (showing or similar) */
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                            /** Get jsonObj from args [args[0] = StorageReference; args[1] = downloadedFile]
                             * Do not supply StorageReference of IndexFile as it is not needed. */
                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(
                                    parseJsonObjFromArgs((args != null && args.length > 1) ? args[1] : null) //IndexFile as JsonObj
                            );
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

    /** Download userLibrary from firebase
     * @param creationMM_YYYY: e.g. '02_2018' etc. to determine indexFile.
     * @param libId: e.g. '45sfd65sdf654sfd654' (Hash) -> Filename of Userlibrary, which is also the ID in the index-File. */
    private static void downloadUserLibrary(@NonNull final Context context,
                                            @NonNull final String creationMM_YYYY,
                                            @NonNull final List<String> languageCodes,
                                            @NonNull final String libId,
                                            @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation saveUserLibrary,
                                            @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation saveLanguagePack) {

        /** Now download meta data of userLib by downloading specific index-File*/
        downloadIndexFile(context, creationMM_YYYY, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {
                /** Args[0] = JsonObject of IndexFile */

                /** Save UserLibrary into Db if it does not exist yet. */
                if (saveUserLibrary != null) {saveUserLibrary.success_is_true(args);}

                for (final String languageCode : languageCodes) {
                    downloadFile(context,
                            /** e.g. v1/en/4sd65fs45df45sdf465s.json */
                            IFirebaseStorageMgr.LIB_JSON_VERSION_FOLDER + "\\" + languageCode + "\\" + libId + "." + IFirebaseStorageMgr.RES_FILE_EXTENSION,
                            new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                                @Override
                                public void success_is_true(@Nullable final Object... args) {
                                    /** Args[0] = StorageReference of File
                                     * Args[1] = JsonArray of UserLibrary of languageCode */

                                    //Save languagepackLines to UserLibrary
                                    if (saveLanguagePack != null) {
                                        saveLanguagePack.success_is_true(args);
                                    }
                                }


                                @Override
                                public void failure_is_false(@Nullable Object... args) {
                                    Log.e(TAG, "downloadUserLibrary:failure_is_false: Could not download new package->\n* " + libId + "->" + languageCode.toString());

                                    /** Custom error procedure for specific userLibrary. */
                                    if (saveLanguagePack != null) {
                                        saveLanguagePack.failure_is_false();
                                    }
                                }
                            });
                }
            }

            @Override
            public void failure_is_false(@Nullable Object... args) {
                Log.e(TAG, "downloadUserLibrary: Could not download index of userLibrary: " + libId);
                if (saveUserLibrary != null) {
                    saveUserLibrary.failure_is_false();
                }
            }
        });
    }

    /** Save already downloaded Userlibrary. */
    public static void saveUserLibrary(@NonNull final Context context,
                                       @NonNull final String creationMM_YYYY,
                                       @NonNull final List<String> languageCodes,
                                       @NonNull final String libId,
                                       @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        downloadUserLibrary(context, creationMM_YYYY, languageCodes, libId,
            /** Will be executed once for this UserLibrary. */
            new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {
                /** args[0]: JsonObj of indexFile
                 *
                 * IMPORTANT: This success_is_true() is called only once for saving the userLibrary. */

                /***/
                UserLibrary userLibrary = extractUserLibraryFromIndexJsonObj(libId, parseJsonObjFromArgs((args != null && args.length > 0) ? args[0] : null));
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
                String failureMsgDescription = String.format(res.getString(R.string.firebaseStorageMgr_install_userlibrary_failuremsg_description), libId);
                if (context instanceof Activity) {
                    (new DialogMgr((Activity) context)).showDialog_Generic(res.getString(R.string.firebaseStorageMgr_install_userlibrary_failuremsg_title),
                            failureMsgDescription, null, "", -1, null);
                } else {
                    Log.w(TAG, "downloadNewPackage:onFailure: Could not show failure dialog, because context is not an activity. Showing toast instead.");
                    Toast.makeText(context, failureMsgDescription, Toast.LENGTH_SHORT).show();
                }
            }
        },
        /** Will be executed for every languagePack of UserLibrary */
        new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {
                
            }

            @Override
            public void failure_is_false(@Nullable Object... args) {
                //TODO: Maybe display here also a dialogue? But what if multiple failed? Would be annoying. Maybe just a toast.
                Log.e(TAG, "downloadNewPackage:onFailure: Could not download languagePack for userlibrary.");
            }
        });
    }

    /** Maps downloaded indexFile onto a UserLibrary by using the provided libId, otherwise we would have to map
     * all downloaded UserLibs. */
    private static UserLibrary extractUserLibraryFromIndexJsonObj(@NonNull String libId, @Nullable JSONObject indexFile) {
        if (indexFile != null) {
            try {
                /* Extract available languageCodes from indexFile (which does not mean that they
                * are installed!!) */
                Iterator<?> languageCodes = indexFile.getJSONObject(libId).keys();
                List<String> languageCodeList = new ArrayList<>();
                while (languageCodes.hasNext()) {
                    languageCodeList.add(languageCodes.next().toString());
                }


                /* Extract relevant part from indexFile to own JsonObj.
                 * As we only need here our desired UserLibrary we use the libId to sort all userLibEntries out,
                 * additionally we use our standard configured language (IGlobal) to decide in which language
                 *
                 * TODO: What if language does not exist? Currently we have English by default. */
                JSONObject userLibEntry = indexFile.getJSONObject(libId).getJSONObject(IGlobal.GLOBAL.LOCALE.getLanguage());
                return new UserLibrary(
                       libId,
                        userLibEntry.getString("libName"),
                        userLibEntry.getString("libDescription"),
                        languageCodeList,
                        userLibEntry.getString("libCreator")
                );
            } catch (JSONException e) {
                Log.e(TAG, "extractUserLibraryFromIndexJsonObj: Could not extract Userlibrary from json. Json malformed!");
                e.printStackTrace();
            }
        }
        return null;
    }


    @Deprecated
    public static UserLibrary saveDefaultUserLibrary(@NonNull Context context) {
        //This method has no validation whether default user lib was already downloaded!
        UserLibrary userLibrary = null;
        try {
            userLibrary = null; //TODO: FirebaseStorageMgr.mapJsonToUserLibDEPRECATED(new JSONObject(IFirebaseStorageMgr.DEFAULT.LIB_JSON_DEFAULT));
            if (userLibrary != null) {
                userLibrary.save(context);
            } else {
                //TODO: Maybe inform user
                Log.e(TAG, "saveDefaultUserLibrary: Could not save default user library, bc. library is null.");
            }
            Log.d(TAG, "saveDefaultUserLibrary: Tried to save default user lib.");
        } catch (Exception e) {
            //THIS SHOULD NEVER HAPPEN
            //TODO: Maybe inform user
            Log.e(TAG, "saveDefaultUserLibrary: Could not save default user library.");
            e.printStackTrace();
        }
        return userLibrary;
    }


    //FIREBASE RELATED METHODS --------------------
    /** Do not put storageReference or/and FireabaseStorage as class members (and surely NOT as static).
     * Otherwise we cannot download multiple files at the same time. */
    private static StorageReference getStorageReference(@NonNull Context context) {
        FirebaseApp.initializeApp(context);
        return FirebaseStorage.getInstance().getReference();
    }
}
