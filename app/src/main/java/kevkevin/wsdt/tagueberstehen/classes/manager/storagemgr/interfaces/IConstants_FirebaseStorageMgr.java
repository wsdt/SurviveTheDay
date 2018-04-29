package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces;

public interface IConstants_FirebaseStorageMgr {
    /** Important: By changing the versionCode folder you should also UPDATE the upload-procedure Json of
     * the FirebaseStorageMgr.class, so the newly saved Userlibrary gets saved correctly for the specific version code! */
    String LIB_JSON_VERSION_FOLDER = "v1"; //versioning, so we can change the firebaseJson over time without any problems
    String LIB_FILEEXTENSION = "json";

    interface INDEX_FILES {
        String FILENAME = "__index"; //all index files on Firebase need to be named like this!
        String FILETYPE = "json";
    }
}
