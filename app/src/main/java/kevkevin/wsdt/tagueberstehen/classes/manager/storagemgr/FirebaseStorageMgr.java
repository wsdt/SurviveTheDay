package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class FirebaseStorageMgr {
    private static final String TAG = "FirebaseStorageMgr";
    /**
     * both get declared by calling getter (when they are null)
     */
    private static FirebaseStorage firebaseStorage;
    private static StorageReference storageReference;

    //TODO: create method for downloading (later if it works well implement uploading (after testing downloading) --> so just upload both language packs)


    public static void downloadNewPackage(@NonNull String relChildPath) {
        StorageReference childFileReference = getStorageReference().child(relChildPath);
        final String libName = childFileReference.getName().replace(".std.lib",""); //better only this final to free up storage reference as soon as possible

        childFileReference.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "downloadNewPackage:onSuccess: Got valid stream from firebase.");
                readFile(libName,taskSnapshot.getStream());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "downloadNewPackage:onFailure: Could not download new package->\n* " + e.getLocalizedMessage() + "\n* " + e.getMessage() + "\n* " + e.getCause() + "\n *" + e.getStackTrace().toString());
            }
        });
    }

    /**
     * @param libName: Libname will be the sql table name and is at the same time the filename (without fileextensions [.std.lib]).
     * @param strLine: Text to save.
     */
    private static boolean saveFileLine(@NonNull String libName, @NonNull String strLine) {
        boolean savingSuccessful = false;
        Log.d(TAG, "saveFileLine: Trying to save new line (" + strLine + ") -> " + libName);
        return savingSuccessful;
    }

    private static void readFile(@NonNull final String libName, final InputStream fis) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner sc = null;

                try {
                    sc = new Scanner(fis, "UTF-8");
                    while (sc.hasNextLine()) {
                        // convert to char and display it
                        if (!saveFileLine(libName, sc.nextLine())) {
                            Log.e(TAG, "readFile: Could not save following line->" + sc.nextLine() + "\n");
                        }
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

    private static boolean readFile(@NonNull File file) throws IOException {
        //return new String(Files.readAllBytes(file.toPath()),charset);

        FileInputStream fis = null;
        Scanner sc = null;
        boolean readingSuccessfully = false;

        try {
            fis = new FileInputStream(file);
            sc = new Scanner(fis, "UTF-8");
            while (sc.hasNextLine()) {
                // convert to char and display it
                if (!saveFileLine(file.getName(), sc.nextLine())) {
                    Log.e(TAG, "readFile: Could not save following line->" + sc.nextLine() + "\n");
                }
            }

            readingSuccessfully = true;

        } catch (IOException e) {
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
        return readingSuccessfully;
    }

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
