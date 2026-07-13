package com.example.splitpay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class LockActivity extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                BiometricLockHelper.setSessionUnlocked(true);
                startActivity(new Intent(LockActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(LockActivity.this, "Authentication cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LockActivity.this, "Fingerprint not recognized.", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock SplitPay")
                .setSubtitle("Confirm your fingerprint to continue")
                .setNegativeButtonText("Cancel")
                .build();

        Button unlockBtn = findViewById(R.id.unlockBtn);
        unlockBtn.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

        // Prompt automatically on open too, not just on button tap
        biometricPrompt.authenticate(promptInfo);
    }
}
