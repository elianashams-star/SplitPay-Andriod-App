package com.example.splitpay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private TextView profileInitialText, profileNameText, profileEmailText;
    private ImageView profilePhotoImage;
    private Button googleSignInBtn;
    private LinearLayout signOutBtn, changePhotoBtn;

    private View paymentMethodsSection;
    private RecyclerView paymentMethodsRecycler;
    private PaymentMethodAdapter paymentMethodAdapter;
    private List<PaymentMethod> paymentMethodList;

    private Button addPaymentMethodBtn, savePaymentMethodBtn;
    private View addMethodForm;
    private Button typeCreditBtn, typeDebitBtn, typeBankBtn;
    private EditText methodLast4Input;
    private String selectedType = "Credit Card";

    private Switch biometricLockSwitch;

    private ActivityResultLauncher<Intent> signInLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.ACCOUNT);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        profileInitialText = findViewById(R.id.profileInitialText);
        profilePhotoImage = findViewById(R.id.profilePhotoImage);
        profileNameText = findViewById(R.id.profileNameText);
        profileEmailText = findViewById(R.id.profileEmailText);
        googleSignInBtn = findViewById(R.id.googleSignInBtn);
        signOutBtn = findViewById(R.id.signOutBtn);
        changePhotoBtn = findViewById(R.id.changePhotoBtn);

        paymentMethodsSection = findViewById(R.id.paymentMethodsSection);
        paymentMethodsRecycler = findViewById(R.id.paymentMethodsRecycler);
        addPaymentMethodBtn = findViewById(R.id.addPaymentMethodBtn);
        addMethodForm = findViewById(R.id.addMethodForm);
        typeCreditBtn = findViewById(R.id.typeCreditBtn);
        typeDebitBtn = findViewById(R.id.typeDebitBtn);
        typeBankBtn = findViewById(R.id.typeBankBtn);
        methodLast4Input = findViewById(R.id.methodLast4Input);
        savePaymentMethodBtn = findViewById(R.id.savePaymentMethodBtn);

        biometricLockSwitch = findViewById(R.id.biometricLockSwitch);
        biometricLockSwitch.setChecked(BiometricLockHelper.isLockEnabled(this));
        biometricLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !BiometricLockHelper.canUseBiometrics(this)) {
                Toast.makeText(this, "No fingerprint set up on this device.", Toast.LENGTH_SHORT).show();
                biometricLockSwitch.setChecked(false);
                return;
            }
            BiometricLockHelper.setLockEnabled(this, isChecked);
        });

        paymentMethodList = new ArrayList<>();
        paymentMethodAdapter = new PaymentMethodAdapter(paymentMethodList, method -> deletePaymentMethod(method));
        paymentMethodsRecycler.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodsRecycler.setAdapter(paymentMethodAdapter);

        setTypeSelected("Credit Card");

        typeCreditBtn.setOnClickListener(v -> setTypeSelected("Credit Card"));
        typeDebitBtn.setOnClickListener(v -> setTypeSelected("Debit Card"));
        typeBankBtn.setOnClickListener(v -> setTypeSelected("Bank Account"));

        addPaymentMethodBtn.setOnClickListener(v -> {
            boolean showing = addMethodForm.getVisibility() == View.VISIBLE;
            addMethodForm.setVisibility(showing ? View.GONE : View.VISIBLE);
        });

        savePaymentMethodBtn.setOnClickListener(v -> savePaymentMethod());

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data == null) return;
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(this, "Sign-in failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null
                            && result.getData().getExtras() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo != null) {
                            saveProfilePhoto(photo);
                        }
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is needed to change your photo.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        googleSignInBtn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });

        signOutBtn.setOnClickListener(v -> {
            firebaseAuth.signOut();
            googleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
        });

        changePhotoBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Sign in first to add a photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(firebaseAuth.getCurrentUser());
    }

    private void setTypeSelected(String type) {
        selectedType = type;

        Button[] buttons = {typeCreditBtn, typeDebitBtn, typeBankBtn};
        String[] types = {"Credit Card", "Debit Card", "Bank Account"};

        for (int i = 0; i < buttons.length; i++) {
            if (types[i].equals(type)) {
                buttons[i].setBackgroundResource(R.drawable.bg_tab_selected);
                buttons[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            } else {
                buttons[i].setBackgroundResource(R.drawable.bg_tab_unselected);
                buttons[i].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    private void savePaymentMethod() {
        String last4 = methodLast4Input.getText().toString().trim();
        if (last4.length() != 4) {
            methodLast4Input.setError("Enter exactly 4 digits");
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String uid = user.getUid();
        String holderName = user.getDisplayName() != null ? user.getDisplayName() : "SplitPay User";

        Map<String, Object> methodDoc = new HashMap<>();
        methodDoc.put("type", selectedType);
        methodDoc.put("last4", last4);
        methodDoc.put("holderName", holderName);

        db.collection("users").document(uid)
                .collection("paymentMethods").add(methodDoc)
                .addOnSuccessListener(ref -> {
                    methodLast4Input.setText("");
                    addMethodForm.setVisibility(View.GONE);
                    Toast.makeText(this, "Payment method added.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Couldn't save. Try again.", Toast.LENGTH_SHORT).show());
    }

    private void deletePaymentMethod(PaymentMethod method) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .collection("paymentMethods").document(method.getId())
                .delete();
    }

    private void loadPaymentMethods(String uid) {
        db.collection("users").document(uid)
                .collection("paymentMethods")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    paymentMethodList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String type = doc.getString("type");
                        String last4 = doc.getString("last4");
                        String holder = doc.getString("holderName");
                        paymentMethodList.add(new PaymentMethod(doc.getId(), type, last4, holder));
                    }
                    paymentMethodAdapter.notifyDataSetChanged();
                });
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No camera app available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUI(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void saveProfilePhoto(Bitmap original) {
        int size = 200;
        Bitmap scaled = Bitmap.createScaledBitmap(original, size, size, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] bytes = stream.toByteArray();
        String base64Photo = Base64.encodeToString(bytes, Base64.DEFAULT);

        String uid = firebaseAuth.getCurrentUser().getUid();

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("profilePhotoBase64", base64Photo);

        db.collection("users").document(uid)
                .set(userDoc, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    profilePhotoImage.setImageBitmap(scaled);
                    profilePhotoImage.setVisibility(View.VISIBLE);
                    profileInitialText.setVisibility(View.GONE);
                    Toast.makeText(this, "Photo updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Couldn't save photo. Try again.", Toast.LENGTH_SHORT).show());
    }

    private void loadProfilePhoto(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("profilePhotoBase64")) {
                        String base64Photo = doc.getString("profilePhotoBase64");
                        if (base64Photo != null && !base64Photo.isEmpty()) {
                            byte[] bytes = Base64.decode(base64Photo, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profilePhotoImage.setImageBitmap(bitmap);
                            profilePhotoImage.setVisibility(View.VISIBLE);
                            profileInitialText.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "SplitPay User";
            profileNameText.setText(name);
            profileEmailText.setText(user.getEmail() != null ? user.getEmail() : "");
            profileInitialText.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());

            profilePhotoImage.setVisibility(View.GONE);
            profileInitialText.setVisibility(View.VISIBLE);
            loadProfilePhoto(user.getUid());
            loadPaymentMethods(user.getUid());

            googleSignInBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);
            changePhotoBtn.setVisibility(View.VISIBLE);
            paymentMethodsSection.setVisibility(View.VISIBLE);
        } else {
            profileNameText.setText("Not signed in");
            profileEmailText.setText("");
            profileInitialText.setText("?");
            profileInitialText.setVisibility(View.VISIBLE);
            profilePhotoImage.setVisibility(View.GONE);

            googleSignInBtn.setVisibility(View.VISIBLE);
            signOutBtn.setVisibility(View.GONE);
            changePhotoBtn.setVisibility(View.GONE);
            paymentMethodsSection.setVisibility(View.GONE);
        }
    }
}