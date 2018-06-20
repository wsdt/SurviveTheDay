package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

import kevkevin.wsdt.tagueberstehen.annotations.Test;
import kevkevin.wsdt.tagueberstehen.classes.entities.LanguagePack;
import kevkevin.wsdt.tagueberstehen.classes.entities.UserLibrary;
import kevkevin.wsdt.tagueberstehen.classes.entities.ZT_UserLibraryLanguagePack;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IFirebaseStorageMgr;
import kevkevin.wsdt.tagueberstehen.interfaces.IGlobal;

@Test(message = "Improved cohesion, which resulted in many methods. Please test them.", priority = Test.Priority.HIGH,
        byDeveloper = IGlobal.DEVELOPERS.WSDT)
public class FirebaseStorageMgr {
    private static final String TAG = "FirebaseStorageMgr";

    /* COMMENTED, to make app smaller (to remove library of firebase)

    /**
     * Do not put FirebaseStorage obj as class members (not as static nor non-static)
     * otherwise we can't download multiple files at the same time.
     *

    /**
     * Helpermethod for extracting downloadedJsonObj from var args.
     * When you want to extract multiple indizes, then write one by yourself.
     *
     * DownloadFile() typically returns StorageReference in args[0] and JsonObj in args[1]
     *
     * @param jsonClass: Should be either JsonArray.class or JsonObject.class to tell the method
     *      how the string is formatted.
     *
    private static JSONArray parseJsonArrFromArg(@Nullable Object arg, @NonNull Class jsonClass) {
        JSONArray dowloadedFile = new JSONArray();
        if (arg != null) {
            try {
                if (jsonClass == JSONObject.class) {
                    //Just nest jsonObj into JsonArray [{}] so we can use it
                    dowloadedFile.put(new JSONObject(arg.toString())); //if you want to use indexFile then you can do it here (otherwise it will be used in nested successTrue().
                } else if (jsonClass == JSONArray.class) {
                    dowloadedFile = new JSONArray(arg.toString());
                } else {
                    Log.d(TAG, "parseJsonArrFromArg: Provided class is not json. ->" + arg.toString()+"/ Desired Class: "+jsonClass);
                }
            } catch (JSONException e) {
                Log.e(TAG, "parseJsonArrFromArg: Could not parse json-> "+arg.toString());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "parseJsonArrFromArg: Provided arg is null."); //Implicit toString call to avoid nullPointer
        }
        return dowloadedFile;
    }


    /* #########################################################################################################
     * ########## DOWNLOAD PROCEDURES ###########################################################################
     * ##########################################################################################################*

    /**
     * Generic Method for downloading files from firebase.
     *
    private static void downloadFile(@NonNull Context context, @NonNull String strStorageReference, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        final StorageReference storageReference = getStorageReference(context);
        Log.d(TAG, "downloadFile: Trying to download file -> "+strStorageReference);

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

                            /* Execute successMethod with downloadedJson. (don't forget to validate whether it is null or valid) *
                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(storageReference, jsonStr.toString());
                            }
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
     * @param creationMM_YYYY:                                          In which month and year the userLibraries were created. (MM_YYYY --> 02_2018)
     * @param executeIfTrueSuccess_or_ifFalseFailure_afterCompletation: After file has be downloaded what do you want to do additionally to regular index procedures?
     *
    public static void downloadIndexFile(@NonNull Context context, @NonNull String creationMM_YYYY, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        /** Nesting provided ExecuteIfTrueSuccess_OR_IfFalseFailure into new interface, bc. so we can merge custom indexFile-Procedures with provided ones.*
        downloadFile(context,
                IFirebaseStorageMgr.LIB_JSON_VERSION_FOLDER + "/" +
                        IFirebaseStorageMgr.INDEX_FILES.FILENAME + creationMM_YYYY + "." + /* e.g. __index_02_2018 *
                        IFirebaseStorageMgr.RES_FILE_EXTENSION, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true(@Nullable Object... args) {
                        /** You can provide here a procedure for all index files. If you just want to execute procedures for specific indexFiles just
                         * provide it to following success-Method (at the bottom of this method). Same principle for failure_is_false(). *

                        /** What to do with indexFile (showing or similar) *
                        if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                            /** Get jsonObj from args [args[0] = StorageReference; args[1] = downloadedFile]
                             * Do not supply StorageReference of IndexFile as it is not needed. *
                            executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.success_is_true(
                                    parseJsonArrFromArg((args != null && args.length > 1) ? args[1] : null, JSONObject.class) //IndexFile as JsonStr
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

    /**
     * Download userLibrary from firebase
     *
     * @param creationMM_YYYY: e.g. '02_2018' etc. to determine indexFile.
     * @param libId:           e.g. '45sfd65sdf654sfd654' (Hash) -> Filename of Userlibrary, which is also the ID in the index-File.
     *
    private static void downloadUserLibrary(@NonNull final Context context,
                                            @NonNull final String creationMM_YYYY,
                                            @NonNull final List<LanguagePack> languagePacks,
                                            @NonNull final String libId,
                                            @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation saveUserLibrary,
                                            @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation saveLanguagePack) {

        /** Now download meta data of userLib by downloading specific index-File*
        downloadIndexFile(context, creationMM_YYYY, new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true(@Nullable Object... args) {
                /** Args[0] = JsonObject of IndexFile *
                Log.d(TAG, "downloadUserLibrary:success_is_true: Starting to download indexFile.");

                /** Save UserLibrary into Db if it does not exist yet. *
                if (saveUserLibrary != null) {
                    saveUserLibrary.success_is_true(args);
                }
                //LanguagePacks are provided by saveUserLib() so user can decide which ones to download
                for (final LanguagePack languagePack : languagePacks) {
                    Log.d(TAG, "downloadUserLibrary:success_is_true: Starting to download languagePack-> "+languagePack.getLpKuerzel());
                    downloadFile(context,
                            /** e.g. v1/en/4sd65fs45df45sdf465s.json *
                            IFirebaseStorageMgr.LIB_JSON_VERSION_FOLDER + "/" + languagePack.toString() + "/" + libId + "." + IFirebaseStorageMgr.RES_FILE_EXTENSION,
                            new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                                @Override
                                public void success_is_true(@Nullable final Object... args) {
                                    Log.d(TAG, "downloadUserLibrary:success_is_true: Starting to save languagepack-> "+args);

                                    /** Args[0] = StorageReference of File
                                     * Args[1] = JsonStr of UserLibrary of languageCode *

                                    //Save languagepackLines to UserLibrary
                                    if (saveLanguagePack != null) {
                                        saveLanguagePack.success_is_true(
                                                languagePack,
                                                (args != null && args.length > 0) ? args[0] : null,
                                                (args != null && args.length > 1) ? args[1] : null);
                                    }
                                }


                                @Override
                                public void failure_is_false(@Nullable Object... args) {
                                    Log.e(TAG, "downloadUserLibrary:failure_is_false: Could not download new package->\n* " + libId + "->" + languagePack.toString());

                                    /** Custom error procedure for specific userLibrary. *
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

    /**
     * Save a new userLibrary by downloading it from Firebase.
     * @param libId: Hash of desired userLibrary.
     * @param creationMM_YYYY: e.g. 08_2018 for determining which index file to download (has to be correct).
     * @param languageCodes: Which languagePacks does the user want to download. They have to exist!
     *
    @Enhance (message = {"Add exception handling for non-existing language codes.",
        "Add exception handling for providing a malformed, non-existing creationMM_YYYY OR add handling " +
                "when index file does not contain preferred userLibrary."})
    public static void saveUserLibrary(@NonNull final Context context,
                                       @NonNull final String libId,
                                       @NonNull final String creationMM_YYYY,
                                       @NonNull final List<LanguagePack> languageCodes,
                                       @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation executeIfTrueSuccess_or_ifFalseFailure_afterCompletation) {
        downloadUserLibrary(context, creationMM_YYYY, languageCodes, libId,
                /** Will be executed once for this UserLibrary. *
                new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true(@Nullable Object... args) {
                        /** args[0]: JsonStr of indexFile
                         *
                         * IMPORTANT: This success_is_true() is called only once for saving the userLibrary. *

                        UserLibrary userLibrary = extractUserLibraryFromIndexJsonArr(libId, parseJsonArrFromArg((args != null && args.length > 0) ? args[0] : null, JSONArray.class));
                        if (userLibrary != null) {
                            userLibrary.save(context);
                            Log.d(TAG, "saveUserLibrary: Library has been saved.");
                        } else {
                            /** Download failed *
                            if (executeIfTrueSuccess_or_ifFalseFailure_afterCompletation != null) {
                                executeIfTrueSuccess_or_ifFalseFailure_afterCompletation.failure_is_false();
                            }
                        }
                    }

                    @Override
                    public void failure_is_false(@Nullable Object... args) {
                        /** Show following error msgs for all failures for all userLibraries.*
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
                /** Will be executed for every languagePack of UserLibrary *
                new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
                    @Override
                    public void success_is_true(@Nullable Object... args) {
                        Log.d(TAG, "saveUserLibrary->Languagepack: Trying to map languagePackJson and save it.");

                        /* Args[0] = LanguagePack
                         * Args[1] = StorageReference of File
                         * Args[2] = JsonArray of UserLibrary of languageCode*

                        /* When this method is called a new languagePack should be inserted into db. *
                        extractULibLanguagePackFromLibJsonObj(context,
                                (LanguagePack) ((args != null && args.length > 0) ? args[0] : null),
                                (StorageReference) ((args != null && args.length > 1) ? args[1] : null),
                                parseJsonArrFromArg((args != null && args.length > 2) ? args[2] : null,JSONArray.class)
                        );
                    }

                    @Override
                    public void failure_is_false(@Nullable Object... args) {
                        //TODO: Maybe display here also a dialogue? But what if multiple failed? Would be annoying. Maybe just a toast.
                        Log.e(TAG, "downloadNewPackage:onFailure: Could not download languagePack for userlibrary.");
                    }
                });
    }

    @Bug(message = "Metadata won't be downloaded, developer credentials error shown")
    private static void extractULibLanguagePackFromLibJsonObj(@NonNull final Context context, @Nullable final LanguagePack languageCode, @Nullable final StorageReference storageReferenceOfLibFile, @Nullable final JSONArray libLanguageLines) {
        if (storageReferenceOfLibFile != null && libLanguageLines != null && languageCode != null) {
            Log.d(TAG, "extractULibLanguagePackFromLibJsonObj: Trying to start saving new languagepack to userLibrary.");


            storageReferenceOfLibFile.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    new ZT_UserLibraryLanguagePack(
                            storageReferenceOfLibFile.getName(),
                            languageCode.getLpKuerzel(),
                            libLanguageLines,
                            storageMetadata.getCreationTimeMillis(),
                            storageMetadata.getUpdatedTimeMillis()
                    ).save(context);

                    Log.d(TAG, "extractULibLanguagePackFromLibJsonObj: Saved new languagepack to userLibrary.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "extractULibLanguagePackFromLibJsonObj: Could not save new languagepack.");
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "extractULibLanguagePackFromLibJsonObj: A provided value is null! Could not map Languagepack");
        }
    }

    /**
     * Maps downloaded indexFile onto a UserLibrary by using the provided libId, otherwise we would have to map
     * all downloaded UserLibs.
     *
    private static UserLibrary extractUserLibraryFromIndexJsonArr(@NonNull String libId, @Nullable JSONArray indexFile) {
        if (indexFile != null) {
            try {
                /* Extract available languageCodes from indexFile (which does not mean that they
                 * are installed!!)
                 *
                 * We have to get Index 0 before, bc. we parsed our JsonObj to a JsonArr (bc. of the
                 * userLibFile which is a JsonArr. So we have the same objects. So there should/is
                 * never another index than 0 :)*
                Iterator<?> languageCodes = indexFile.getJSONObject(0).getJSONObject(libId).keys();
                List<LanguagePack> languageCodeList = new ArrayList<>();
                while (languageCodes.hasNext()) {
                    languageCodeList.add(new LanguagePack(languageCodes.next().toString())); //TODO: Why libCreator given?
                }


                /* Extract relevant part from indexFile to own JsonObj.
                 * As we only need here our desired UserLibrary we use the libId to sort all userLibEntries out,
                 * additionally we use our standard configured language (IGlobal) to decide in which language
                 *
                 * TODO: What if language does not exist? Currently we have English by default. *
                JSONObject userLibEntry = indexFile.getJSONObject(0).getJSONObject(libId).getJSONObject(IGlobal.GLOBAL.LOCALE.getLanguage());
                return new UserLibrary(
                        libId,
                        userLibEntry.getString("libName"),
                        userLibEntry.getString("libDescription"),
                        languageCodeList,
                        indexFile.getJSONObject(0).getJSONObject(libId).getString("libCreator")
                );
            } catch (JSONException e) {
                Log.e(TAG, "extractUserLibraryFromIndexJsonArr: Could not extract Userlibrary from json. Json malformed!");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Install default userLibrary with default languagePacks.
     */
    public static UserLibrary installDefaultData(@NonNull Context context) {
        final String EN = "en", DE = "de";

        //Save language packs
        new LanguagePack(DE).save(context);
        new LanguagePack(EN).save(context);

        //TODO: In future also save default library with Locale-Language (not only english like here and in IGlobal defined)
        UserLibrary installedUserLib = new UserLibrary(
                IFirebaseStorageMgr.DEFAULT.DEFAULT_LIB_ID, /*Normally a hash, but for default library we can make an exception. */
                "Default quotes",
                "This library contains motivating quotes in different languages.",
                new ArrayList<>(Arrays.asList(new LanguagePack("en"), new LanguagePack("de"))),
                IGlobal.DEVELOPERS.WSDT);
        installedUserLib.save(context);

        //Save also Lines of languagePacks into ZT
        try {
            new ZT_UserLibraryLanguagePack(
                    IFirebaseStorageMgr.DEFAULT.DEFAULT_LIB_ID, DE,
                    new JSONArray(IFirebaseStorageMgr.DEFAULT.JSONARR_DE_LINES),
                    0, 0
            ).save(context);

            new ZT_UserLibraryLanguagePack(
                    IFirebaseStorageMgr.DEFAULT.DEFAULT_LIB_ID, EN,
                    new JSONArray(IFirebaseStorageMgr.DEFAULT.JSONARR_EN_LINES),
                    0, 0
            ).save(context);
        } catch (JSONException e) {
            Log.e(TAG, "installDefaultData: Could not parse lines.");
            e.printStackTrace();
        }

        Log.d(TAG, "installDefaultData: Tried to install default data.");
        return installedUserLib;
    }
}
/*
    //FIREBASE RELATED METHODS --------------------

    /**
     * Do not put storageReference or/and FireabaseStorage as class members (and surely NOT as static).
     * Otherwise we cannot download multiple files at the same time.
     *
    private static StorageReference getStorageReference(@NonNull Context context) {
        FirebaseApp.initializeApp(context);
        return FirebaseStorage.getInstance().getReference();
    }
}*/
