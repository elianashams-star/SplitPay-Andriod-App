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
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private ArrayList<Expense> allExpenses;
    private ArrayList<Expense> displayedExpenses;

    private ExpenseAdapter adapter;
    private ActivityResultLauncher<Intent> addExpenseLauncher;

    private RecyclerView recyclerView;

    private Button tabAll, tabIncoming, tabOutgoing, tabAutoPay;

    private TextView owedAmountText, oweAmountText, netAmountText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        db = FirebaseFirestore.getInstance();
        loadExpenses();

        recyclerView = findViewById(R.id.expenseRecycler);
        Button addExpenseBtn = findViewById(R.id.addExpenseBtn);

        tabAll = findViewById(R.id.tabAll);
        tabIncoming = findViewById(R.id.tabIncoming);
        tabOutgoing = findViewById(R.id.tabOutgoing);
        tabAutoPay = findViewById(R.id.tabAutoPay);

        owedAmountText = findViewById(R.id.owedAmountText);
        oweAmountText = findViewById(R.id.oweAmountText);
        netAmountText = findViewById(R.id.netAmountText);

        allExpenses = new ArrayList<>();
        displayedExpenses = new ArrayList<>();

        allExpenses.add(new Expense(
                "Dinner at La Bella",
                "La Bella Restaurant",
                42.50,
                "Europe Trip 2026",
                "Jan 19, 2026 • 8:45 PM",
                "SplitPay Card ••4829",
                "SP-2026-01-19-8492",
                false
        ));

        allExpenses.add(new Expense(
                "Team lunch",
                "Michael Torres",
                125.00,
                "Office Lunches",
                "Jan 19, 2026 • 2:15 PM",
                "Bank Transfer",
                "SP-2026-01-19-7231",
                true
        ));

        allExpenses.add(new Expense(
                "Groceries",
                "Market City",
                78.20,
                "Home",
                "Jan 18, 2026 • 6:10 PM",
                "Visa ••1032",
                "SP-2026-01-18-3108",
                false
        ));

        displayedExpenses.addAll(allExpenses);

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

        tabIncoming.setOnClickListener(v -> {
            setSelectedTab(tabIncoming);
            showIncomingExpenses();
            logFilterEvent("incoming");
        });

        tabOutgoing.setOnClickListener(v -> {
            setSelectedTab(tabOutgoing);
            showOutgoingExpenses();
            logFilterEvent("outgoing");
        });

        tabAutoPay.setOnClickListener(v -> {
            setSelectedTab(tabAutoPay);
            showAutoPayExpenses();
            logFilterEvent("autopay");
        });

        addExpenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        String title = safeString(result.getData().getStringExtra("title"));
                        double amount = result.getData().getDoubleExtra("amount", 0);
                        String group = safeString(result.getData().getStringExtra("group"));
                        String merchant = safeString(result.getData().getStringExtra("merchant"));
                        String status = safeString(result.getData().getStringExtra("status"));

                        boolean isIncoming = "Settled".equalsIgnoreCase(status);

                        if (title.isEmpty()) title = "New expense";
                        if (group.isEmpty()) group = "General";
                        if (merchant.isEmpty()) merchant = "Manual entry";

                        String date = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(new Date());
                        String paymentMethod = "Manual";
                        String referenceId = "SP-" + System.currentTimeMillis();

                        Expense newExpense = new Expense(
                                title,
                                merchant,
                                amount,
                                group,
                                date,
                                paymentMethod,
                                referenceId,
                                isIncoming
                        );

                        allExpenses.add(0, newExpense);
                        updateBalanceSummary();

                        logAddExpenseEvent(group, amount);

                        if (tabAll.isSelected()) {
                            showAllExpenses();
                        } else if (tabIncoming.isSelected()) {
                            showIncomingExpenses();
                        } else if (tabOutgoing.isSelected()) {
                            showOutgoingExpenses();
                        } else if (tabAutoPay.isSelected()) {
                            showAutoPayExpenses();
                        }

                        recyclerView.scrollToPosition(0);
                    }
                }
        );

        addExpenseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            addExpenseLauncher.launch(intent);
        });
    }

    private void loadExpenses() {db.collection("expenses")
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
                    allExpenses.add(new Expense(title, merchant, amount, group, date, paymentMethod, referenceId, isIncoming));
                }
                updateBalanceSummary();
                showAllExpenses();
            });
    }

    private void logAddExpenseEvent(String category, double amount) {
        Bundle params = new Bundle();
        params.putString("expense_category", category);
        params.putDouble("expense_amount", amount);
        mFirebaseAnalytics.logEvent("add_expense", params);
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

    private void showIncomingExpenses() {
        displayedExpenses.clear();
        for (Expense expense : allExpenses) {
            if (expense.isIncoming()) {
                displayedExpenses.add(expense);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showOutgoingExpenses() {
        displayedExpenses.clear();
        for (Expense expense : allExpenses) {
            if (!expense.isIncoming()) {
                displayedExpenses.add(expense);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAutoPayExpenses() {
        displayedExpenses.clear();
        for (Expense expense : allExpenses) {
            if (expense.getPaymentMethod() != null &&
                    expense.getPaymentMethod().toLowerCase().contains("card")) {
                displayedExpenses.add(expense);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setSelectedTab(Button selectedTab) {
        Button[] tabs = {tabAll, tabIncoming, tabOutgoing, tabAutoPay};

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
            if (expense.isIncoming()) {
                owed += expense.getAmount();
            } else {
                owe += expense.getAmount();
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

    private String safeString(String s) {
        return s == null ? "" : s.trim();
    }
}