package com.example.splitpay;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private ArrayList<Expense> allExpenses;
    private ArrayList<Expense> displayedExpenses;

    private ExpenseAdapter adapter;
    private ActivityResultLauncher<Intent> addExpenseLauncher;

    private RecyclerView recyclerView;

    private Button tabAll, tabYouOwe, tabYoureOwed;

    private TextView owedAmountText, oweAmountText, netAmountText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthGuard.requireSignIn(this)) return;

        if (BiometricLockHelper.isLockEnabled(this) && !BiometricLockHelper.isSessionUnlocked()) {
            startActivity(new Intent(this, LockActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        db = FirebaseFirestore.getInstance();
        loadExpenses();

        recyclerView = findViewById(R.id.expenseRecycler);
        Button addExpenseBtn = findViewById(R.id.addExpenseBtn);

        tabAll = findViewById(R.id.tabAll);
        tabYouOwe = findViewById(R.id.tabYouOwe);
        tabYoureOwed = findViewById(R.id.tabYoureOwed);

        owedAmountText = findViewById(R.id.owedAmountText);
        oweAmountText = findViewById(R.id.oweAmountText);
        netAmountText = findViewById(R.id.netAmountText);

        allExpenses = new ArrayList<>();
        displayedExpenses = new ArrayList<>();

        adapter = new ExpenseAdapter(displayedExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateBalanceSummary();

        setSelectedTab(tabAll);
        showAllExpenses();

        tabAll.setOnClickListener(v -> {
            setSelectedTab(tabAll);
            showAllExpenses();
        });

        tabYouOwe.setOnClickListener(v -> {
            setSelectedTab(tabYouOwe);
            showYouOweExpenses();
            logFilterEvent("you_owe");
        });

        tabYoureOwed.setOnClickListener(v -> {
            setSelectedTab(tabYoureOwed);
            showYoureOwedExpenses();
            logFilterEvent("youre_owed");
        });

        addExpenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Firestore's live listener (loadExpenses) picks up new
                    // expenses automatically — no local list update needed.
                }
        );

        addExpenseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            addExpenseLauncher.launch(intent);
        });
    }

    private void loadExpenses() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("expenses")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        FirebaseCrashlytics.getInstance().recordException(error);
                        return;
                    }
                    allExpenses.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String title = doc.getString("title");
                        String merchant = doc.getString("merchant");
                        double amount = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0;
                        String group = doc.getString("group");
                        String date = doc.getString("date");
                        String paymentMethod = doc.getString("paymentMethod");
                        String referenceId = doc.getString("referenceId");
                        boolean isIncoming = doc.getBoolean("isIncoming") != null ? doc.getBoolean("isIncoming") : false;

                        @SuppressWarnings("unchecked")
                        List<String> splitNames = (List<String>) doc.get("splitNames");

                        String paidBy = doc.getString("paidBy");

                        allExpenses.add(new Expense(title, merchant, amount, group, date, paymentMethod, referenceId, isIncoming, splitNames, paidBy));
                    }
                    updateBalanceSummary();
                    showAllExpenses();
                });
    }

    private void logFilterEvent(String filterType) {
        Bundle params = new Bundle();
        params.putString("filter_type", filterType);
        mFirebaseAnalytics.logEvent("transaction_filter", params);
    }

    private void showAllExpenses() {
        displayedExpenses.clear();
        displayedExpenses.addAll(allExpenses);
        adapter.notifyDataSetChanged();
    }

    private void showYouOweExpenses() {
        displayedExpenses.clear();
        for (Expense expense : allExpenses) {
            if (isYouOweExpense(expense)) {
                displayedExpenses.add(expense);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showYoureOwedExpenses() {
        displayedExpenses.clear();
        for (Expense expense : allExpenses) {
            if (!isYouOweExpense(expense)) {
                displayedExpenses.add(expense);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean isYouOweExpense(Expense expense) {
        List<String> splitNames = expense.getSplitNames();
        String paidBy = expense.getPaidBy();

        if (splitNames == null || splitNames.isEmpty()) {
            return !expense.isIncoming();
        }
        return !"You".equalsIgnoreCase(paidBy);
    }

    private void setSelectedTab(Button selectedTab) {
        Button[] tabs = {tabAll, tabYouOwe, tabYoureOwed};

        for (Button tab : tabs) {
            tab.setSelected(false);
            tab.setBackgroundResource(R.drawable.bg_tab_unselected);
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }

        selectedTab.setSelected(true);
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
    }

    private void updateBalanceSummary() {
        double owed = 0;
        double owe = 0;

        for (Expense expense : allExpenses) {
            List<String> splitNames = expense.getSplitNames();
            String paidBy = expense.getPaidBy();
            double amount = expense.getAmount();

            if (splitNames == null || splitNames.isEmpty()) {
                if (expense.isIncoming()) {
                    owed += amount;
                } else {
                    owe += amount;
                }
            } else {
                double share = amount / (splitNames.size() + 1);
                if ("You".equalsIgnoreCase(paidBy)) {
                    owed += share * splitNames.size();
                } else {
                    owe += share;
                }
            }
        }

        double net = owed - owe;

        owedAmountText.setText(String.format(Locale.US, "$%.2f", owed));
        oweAmountText.setText(String.format(Locale.US, "$%.2f", owe));

        String netText = String.format(Locale.US, "%s$%.2f", net >= 0 ? "+" : "-", Math.abs(net));
        netAmountText.setText(netText);

        if (net >= 0) {
            netAmountText.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
        } else {
            netAmountText.setTextColor(ContextCompat.getColor(this, R.color.accent_red));
        }
    }
}