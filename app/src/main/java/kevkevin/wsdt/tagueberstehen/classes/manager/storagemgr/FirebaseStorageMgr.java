package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;

import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageMgr {
    private static final String TAG = "FirebaseStorageMgr";
    /** both get declared by calling getter (when they are null) */
    private static FirebaseStorage firebaseStorage;
    private static StorageReference storageReference;

    //TODO: create method for downloading (later if it works well implement uploading (after testing downloading) --> so just upload both language packs)


    //GETTER/SETTER --------------------
    public static FirebaseStorage getFirebaseStorage() {
        if (firebaseStorage == null) {
            setFirebaseStorage(FirebaseStorage.getInstance());
            Log.d(TAG, "getFirebaseStorage: Firebasestorage was null. Created new one.");
        }
        return firebaseStorage;
    }

    public static void setFirebaseStorage(FirebaseStorage firebaseStorage) {
        FirebaseStorageMgr.firebaseStorage = firebaseStorage;
    }

    public static StorageReference getStorageReference() {
        if (storageReference == null) {
            setStorageReference(getFirebaseStorage().getReference());
            Log.d(TAG, "getStorageReference: StorageReference was null. Created new one.");
        }
        return storageReference;
    }

    public static void setStorageReference(StorageReference storageReference) {
        FirebaseStorageMgr.storageReference = storageReference;
    }
}
