package kevkevin.wsdt.tagueberstehen.classes.manager;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kevkevin.wsdt.tagueberstehen.annotations.Bug;
import kevkevin.wsdt.tagueberstehen.annotations.Test;

public class FirebaseAuthMgr {
    private static final String TAG = "FirebaseAuthMgr";
    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseUser firebaseUser;

    /**
     * Login anonymously to read firebase data.
     * Later we need to add fireBase Login UI for uploading data.
     */
    @Bug(message = "Developer credentials required.")
    public static void anonymousLogin(@NonNull Activity activity) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            firebaseAuth.signInAnonymously()
                    .addOnSuccessListener(activity, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Log.d(TAG, "anonymousLogin: User logged in anonymously.");
                            firebaseUser = authResult.getUser();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "anonymousLogin: Could not sign in anonymously-> " + e);
                        }
                    });
        } else {
            firebaseUser = user;
            Log.w(TAG, "anonymousLogin: User already logged in-> "+firebaseUser.getDisplayName());
        }
    }
}
