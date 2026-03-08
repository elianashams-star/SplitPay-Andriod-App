package com.example.splitpay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText titleInput, amountInput, groupInput, splitNamesInput;
    private Button statusUnsettledBtn, statusSettledBtn, saveExpenseBtn;

    private String selectedStatus = "Unsettled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        titleInput = findViewById(R.id.titleInput);
        amountInput = findViewById(R.id.amountInput);
        groupInput = findViewById(R.id.groupInput);
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

            // Normalize names input: "Ori,Naomi" -> "Ori, Naomi"
            String splitNames = splitNamesRaw.replaceAll("\\s*,\\s*", ", ").trim();

            // Count people from names (Ori, Naomi -> 2). If blank, treat as 1.
            int peopleCount = 1;
            if (!splitNames.isEmpty()) {
                String[] parts = splitNames.split(",");
                int count = 0;
                for (String p : parts) {
                    if (!p.trim().isEmpty()) count++;
                }
                peopleCount = Math.max(1, count);
            }

            // Build the line that shows under the title on the main list
            String merchant;
            int splitCount;
            if (splitNames.isEmpty()) {
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

            // Optional extras
            data.putExtra("splitCount", splitCount);
            data.putExtra("status", selectedStatus);

            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void setStatusSelected(boolean unsettledSelected) {

        statusUnsettledBtn.setBackgroundTintList(null);
        statusSettledBtn.setBackgroundTintList(null);

        if (unsettledSelected) {
            // Outgoing selected: RED background
            statusUnsettledBtn.setBackgroundResource(R.drawable.bg_tab_selected_red);
            statusSettledBtn.setBackgroundResource(R.drawable.bg_tab_unselected);

            statusUnsettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            statusSettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            // Incoming selected: GREEN background
            statusSettledBtn.setBackgroundResource(R.drawable.bg_tab_selected);
            statusUnsettledBtn.setBackgroundResource(R.drawable.bg_tab_unselected);

            statusSettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            statusUnsettledBtn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }
}