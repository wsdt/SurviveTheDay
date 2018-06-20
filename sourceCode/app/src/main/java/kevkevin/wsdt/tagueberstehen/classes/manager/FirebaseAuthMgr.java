package kevkevin.wsdt.tagueberstehen.classes.manager;


public class FirebaseAuthMgr {}

//COMMENTED, bc. aborted for current release
    /*
    private static final String TAG = "FirebaseAuthMgr";
    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseUser firebaseUser;

     * Login anonymously to read firebase data.
     * Later we need to add fireBase Login UI for uploading data.
     *
    @Bug(message = "Developer credentials required. API has to be enabled: --> IS ENABLED NOW" +
            "https://console.developers.google.com/apis/api/identitytoolkit.googleapis.com/overview?project=survivetheday-12862")
    public static void anonymousLogin(@NonNull Activity activity, @Nullable final HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation afterLogin) {
        FirebaseUser user = getFirebaseAuth().getCurrentUser();
        if (user == null) {
            getFirebaseAuth().signInAnonymously()
                    .addOnSuccessListener(activity, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            setFirebaseUser(authResult.getUser());
                            if (afterLogin != null) {afterLogin.success_is_true();}
                            Log.d(TAG, "anonymousLogin: User logged in anonymously.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (afterLogin != null) {afterLogin.failure_is_false();}
                            Log.e(TAG, "anonymousLogin: Could not sign in anonymously-> " + e);
                        }
                    });
        } else {
            setFirebaseUser(user);
            if (afterLogin != null) {afterLogin.success_is_true();}
            Log.w(TAG, "anonymousLogin: User already logged in-> "+ getFirebaseUser().getDisplayName());
        }
    }


    //GETTER / SETTER -------------------------------------------------------------
    public static FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static void setFirebaseAuth(FirebaseAuth firebaseAuth) {
        FirebaseAuthMgr.firebaseAuth = firebaseAuth;
    }

    public static FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public static void setFirebaseUser(FirebaseUser firebaseUser) {
        FirebaseAuthMgr.firebaseUser = firebaseUser;
    }
}
*/