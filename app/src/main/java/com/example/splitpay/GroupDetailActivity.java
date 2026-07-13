package com.example.splitpay;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_NAME = "group_name";

    private RecyclerView recyclerView;
    private PersonBalanceAdapter adapter;
    private List<PersonBalance> peopleList;
    private FirebaseFirestore db;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthGuard.requireSignIn(this)) return;

        setContentView(R.layout.activity_group_detail);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.GROUPS);

        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        if (groupName == null) groupName = "General";

        TextView titleText = findViewById(R.id.groupDetailTitle);
        titleText.setText(groupName);

        recyclerView = findViewById(R.id.personBalanceRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        peopleList = new ArrayList<>();
        adapter = new PersonBalanceAdapter(peopleList, person -> {
            Intent intent = new Intent(GroupDetailActivity.this, PayActivity.class);
            intent.putExtra(PayActivity.EXTRA_PERSON_NAME, person.getName());
            intent.putExtra(PayActivity.EXTRA_GROUP_NAME, groupName);
            intent.putExtra(PayActivity.EXTRA_OWED_AMOUNT, Math.abs(person.getBalance()));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadGroupDetail();
    }

    private void loadGroupDetail() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("expenses")
                .whereEqualTo("group", groupName)
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    Map<String, Double> balances = new LinkedHashMap<>();
                    Map<String, PersonBalance> personMap = new LinkedHashMap<>();
                    double totalSpent = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Boolean isSettlement = doc.getBoolean("isSettlement");
                        Double amount = doc.getDouble("amount");
                        double amt = amount != null ? amount : 0;

                        if (isSettlement != null && isSettlement) {
                            String settlementTo = doc.getString("settlementTo");
                            String title = doc.getString("title");
                            if (title == null) title = "Payment";
                            if (settlementTo == null) continue;

                            balances.putIfAbsent(settlementTo, 0.0);
                            personMap.putIfAbsent(settlementTo, new PersonBalance(settlementTo, 0));

                            double current = balances.getOrDefault(settlementTo, 0.0);
                            balances.put(settlementTo, current + amt);
                            personMap.get(settlementTo).addContribution(title, amt);
                            continue;
                        }

                        totalSpent += amt;

                        String title = doc.getString("title");
                        if (title == null || title.isEmpty()) title = "Expense";

                        String paidBy = doc.getString("paidBy");
                        if (paidBy == null || paidBy.isEmpty()) paidBy = "You";

                        @SuppressWarnings("unchecked")
                        List<String> splitNames = (List<String>) doc.get("splitNames");
                        if (splitNames == null || splitNames.isEmpty()) continue;

                        for (String person : splitNames) {
                            balances.putIfAbsent(person, 0.0);
                            personMap.putIfAbsent(person, new PersonBalance(person, 0));
                        }

                        double share = amt / (splitNames.size() + 1);

                        if (paidBy.equalsIgnoreCase("You")) {
                            for (String person : splitNames) {
                                double current = balances.getOrDefault(person, 0.0);
                                balances.put(person, current + share);
                                personMap.get(person).addContribution(title, share);
                            }
                        } else {
                            double current = balances.getOrDefault(paidBy, 0.0);
                            balances.put(paidBy, current - share);
                            personMap.get(paidBy).addContribution(title, -share);
                        }
                    }

                    peopleList.clear();
                    double netTotal = 0;
                    for (Map.Entry<String, Double> entry : balances.entrySet()) {
                        String person = entry.getKey();
                        double finalBalance = entry.getValue();

                        PersonBalance pb = new PersonBalance(person, finalBalance);
                        for (PersonBalance.Contribution c : personMap.get(person).getContributions()) {
                            pb.addContribution(c.getExpenseTitle(), c.getShareAmount());
                        }
                        peopleList.add(pb);
                        netTotal += finalBalance;
                    }
                    adapter.notifyDataSetChanged();

                    TextView groupTotalText = findViewById(R.id.groupTotalText);
                    groupTotalText.setText(String.format(Locale.US, "$%.2f", totalSpent));

                    TextView groupNetText = findViewById(R.id.groupNetText);
                    String netLabel = String.format(Locale.US, "%s$%.2f", netTotal >= 0 ? "+" : "-", Math.abs(netTotal));
                    groupNetText.setText(netLabel);
                    groupNetText.setTextColor(ContextCompat.getColor(GroupDetailActivity.this,
                            netTotal >= 0 ? R.color.accent_green : R.color.accent_red));
                });
    }
}