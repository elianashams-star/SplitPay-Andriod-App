package com.example.splitpay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PayActivity extends AppCompatActivity {

    public static final String EXTRA_PERSON_NAME = "person_name";
    public static final String EXTRA_GROUP_NAME = "group_name";
    public static final String EXTRA_OWED_AMOUNT = "owed_amount";

    private TextView payTitleText, paySubtitleText, noMethodsText;
    private EditText amountInput;
    private Button confirmPayBtn;
    private RecyclerView payMethodRecycler;

    private FirebaseFirestore db;
    private String personName;
    private String groupName;
    private PaymentMethod selectedMethod = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthGuard.requireSignIn(this)) return;

        setContentView(R.layout.activity_pay);

        personName = getIntent().getStringExtra(EXTRA_PERSON_NAME);
        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        double owedAmount = getIntent().getDoubleExtra(EXTRA_OWED_AMOUNT, 0);

        payTitleText = findViewById(R.id.payTitleText);
        paySubtitleText = findViewById(R.id.paySubtitleText);
        amountInput = findViewById(R.id.amountInput);
        confirmPayBtn = findViewById(R.id.confirmPayBtn);
        payMethodRecycler = findViewById(R.id.payMethodRecycler);
        noMethodsText = findViewById(R.id.noMethodsText);

        payTitleText.setText("Pay " + personName);
        paySubtitleText.setText(String.format(Locale.US, "You owe $%.2f in %s", owedAmount, groupName));
        amountInput.setText(String.format(Locale.US, "%.2f", owedAmount));

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        List<PaymentMethod> methodList = new ArrayList<>();
        PayMethodOptionAdapter adapter = new PayMethodOptionAdapter(methodList, method -> selectedMethod = method);
        payMethodRecycler.setLayoutManager(new LinearLayoutManager(this));
        payMethodRecycler.setAdapter(adapter);

        db.collection("users").document(uid).collection("paymentMethods")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    methodList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String type = doc.getString("type");
                        String last4 = doc.getString("last4");
                        String holder = doc.getString("holderName");
                        methodList.add(new PaymentMethod(doc.getId(), type, last4, holder));
                    }
                    adapter.notifyDataSetChanged();
                    noMethodsText.setVisibility(methodList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    payMethodRecycler.setVisibility(methodList.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
                });

        confirmPayBtn.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (amountStr.isEmpty()) {
                amountInput.setError("Enter an amount");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (Exception e) {
                amountInput.setError("Enter a valid number");
                return;
            }
            if (amount <= 0) {
                amountInput.setError("Enter a positive amount");
                return;
            }
            if (selectedMethod == null) {
                Toast.makeText(this, "Choose a payment method.", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(new Date());
            String referenceId = "SP-" + System.currentTimeMillis();

            Map<String, Object> expense = new HashMap<>();
            expense.put("title", "Payment to " + personName);
            expense.put("merchant", "Settlement");
            expense.put("amount", amount);
            expense.put("group", groupName);
            expense.put("date", date);
            expense.put("paymentMethod", selectedMethod.getType() + " ••" + selectedMethod.getLast4());
            expense.put("referenceId", referenceId);
            expense.put("isIncoming", false);
            expense.put("splitNames", new ArrayList<String>());
            expense.put("paidBy", "You");
            expense.put("userId", uid);
            expense.put("isSettlement", true);
            expense.put("settlementTo", personName);
            expense.put("timestamp", FieldValue.serverTimestamp());

            db.collection("expenses").add(expense)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Payment sent.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Couldn't complete payment.", Toast.LENGTH_SHORT).show());
        });
    }
}
