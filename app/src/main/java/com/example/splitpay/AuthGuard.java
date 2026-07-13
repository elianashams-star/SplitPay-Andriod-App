package com.example.splitpay;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

public class AuthGuard {

    // Returns true if signed in. If not signed in, redirects to the Account
    // screen and returns false — calling Activity should immediately return
    // from onCreate when this returns false.
    public static boolean requireSignIn(Activity activity) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(activity, AccountActivity.class);
            activity.startActivity(intent);
            activity.finish();
            return false;
        }
        return true;
    }
}
