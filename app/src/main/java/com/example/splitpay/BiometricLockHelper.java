package com.example.splitpay;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.biometric.BiometricManager;

public class BiometricLockHelper {

    private static final String PREFS_NAME = "splitpay_security";
    private static final String KEY_LOCK_ENABLED = "biometric_lock_enabled";

    // Resets to false each time the app process starts fresh — this makes
    // the lock screen appear once per app session, not on every screen.
    private static boolean sessionUnlocked = false;

    public static boolean isSessionUnlocked() {
        return sessionUnlocked;
    }

    public static void setSessionUnlocked(boolean unlocked) {
        sessionUnlocked = unlocked;
    }

    public static boolean isLockEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOCK_ENABLED, false);
    }

    public static void setLockEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply();
    }

    // Checks whether this device even has fingerprint/biometric hardware
    // set up — if not, we shouldn't let the user turn the toggle on.
    public static boolean canUseBiometrics(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }
}