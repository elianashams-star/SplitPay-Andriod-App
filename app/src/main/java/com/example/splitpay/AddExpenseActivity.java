package com.example.splitpay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText titleInput, amountInput, groupInput, paidByInput, splitNamesInput;
    private Button statusUnsettledBtn, statusSettledBtn, saveExpenseBtn;

    private String selectedStatus = "Unsettled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthGuard.requireSignIn(this)) return;

        setContentView(R.layout.activity_add_expense);

        titleInput = findViewById(R.id.titleInput);
        amountInput = findViewById(R.id.amountInput);
        groupInput = findViewById(R.id.groupInput);
        paidByInput = findViewById(R.id.paidByInput);
        splitNamesInput = findViewById(R.id.splitNamesInput);

        statusUnsettledBtn = findViewById(R.id.statusUnsettledBtn);
        statusSettledBtn = findViewById(R.id.statusSettledBtn);
        saveExpenseBtn = findViewById(R.id.saveExpenseBtn);

        statusUnsettledBtn.setBackgroundTintList(null);
        statusSettledBtn.setBackgroundTintList(null);

        setStatusSelected(true);

        statusUnsettledBtn.setOnClickListener(v -> {
            selectedStatus = "Unsettled";
            setStatusSelected(true);
        });

        statusSettledBtn.setOnClickListener(v -> {
            selectedStatus = "Settled";
            setStatusSelected(false);
        });

        saveExpenseBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();
            String group = groupInput.getText().toString().trim();
            String paidByRaw = paidByInput.getText().toString().trim();
            String splitNamesRaw = splitNamesInput.getText().toString().trim();

            if (title.isEmpty()) {
                titleInput.setError("Enter a title");
                return;
            }

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

            if (group.isEmpty()) group = "General";

            // "Paid By" defaults to "You" when left blank — this is what
            // lets the split math in Home, Groups, and Group Detail treat
            // an expense as either "you paid, they owe you" or "someone
            // else paid, you owe them" without a separate flag.
            String paidBy = paidByRaw.isEmpty() ? "You" : paidByRaw;

            // Split "Split With" on commas into a real List<String>, not
            // just a display string — this is what Group Detail and
            // Groups actually loop over to compute each person's share.
            String splitNames = splitNamesRaw.replaceAll("\\s*,\\s*", ", ").trim();

            List<String> splitNamesList = new ArrayList<>();
            if (!splitNames.isEmpty()) {
                for (String name : splitNames.split(",")) {
                    String trimmed = name.trim();
                    if (!trimmed.isEmpty()) splitNamesList.add(trimmed);
                }
            }

            int peopleCount = Math.max(1, splitNamesList.size());

            String merchant;
            int splitCount;
            if (splitNamesList.isEmpty()) {
                merchant = "Manual entry";
                splitCount = 1;
            } else {
                merchant = "Split with " + splitNames;
                splitCount = peopleCount;
            }

            String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(new Date());
            String paymentMethod = "Manual";
            String referenceId = "SP-" + System.currentTimeMillis();

            boolean isIncoming = "Settled".equalsIgnoreCase(selectedStatus);

            // Every expense is tagged with the signed-in user's Firebase
            // UID. Firestore's security rules only allow reading or
            // writing a document where this field matches the requesting
            // user, which is what keeps each user's data private from
            // every other signed-in user.
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> expense = new HashMap<>();
            expense.put("title", title);
            expense.put("merchant", merchant);
            expense.put("amount", amount);
            expense.put("group", group);
            expense.put("date", date);
            expense.put("paymentMethod", paymentMethod);
            expense.put("referenceId", referenceId);
            expense.put("isIncoming", isIncoming);
            expense.put("splitNames", splitNamesList);
            expense.put("paidBy", paidBy);
            expense.put("userId", uid);
            expense.put("timestamp", FieldValue.serverTimestamp());
            db.collection("expenses").add(expense);

            Intent data = new Intent();
            data.putExtra("title", title);
            data.putExtra("merchant", merchant);
            data.putExtra("amount", amount);
            data.putExtra("group", group);
            data.putExtra("date", date);
            data.putExtra("paymentMethod", paymentMethod);
            data.putExtra("referenceId", referenceId);
            data.putExtra("isIncoming", isIncoming);
            data.putExtra("splitNames", splitNames);
            data.putExtra("splitCount", splitCount);
            data.putExtra("status", selectedStatus);
            data.putExtra("paidBy", paidBy);

            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void setStatusSelected(boolean unsettledSelected) {

        statusUnsettledBtn.setBackgroundTintList(null);
        statusSettledBtn.setBackgroundTintList(null);

        if (unsettledSelected) {
            statusUnsettledBtn.setBackgroundResource(R.drawable.bg_tab_selected_red);
            statusSettledBtn.setBackgroundResource(R.drawable.bg_tab_unselected);
            statusUnsettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            statusSettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            statusSettledBtn.setBackgroundResource(R.drawable.bg_tab_selected);
            statusUnsettledBtn.setBackgroundResource(R.drawable.bg_tab_unselected);
            statusSettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            statusUnsettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }
}