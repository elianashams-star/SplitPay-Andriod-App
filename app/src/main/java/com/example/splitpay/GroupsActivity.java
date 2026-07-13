package com.example.splitpay;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupSummary> groupList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthGuard.requireSignIn(this)) return;

        setContentView(R.layout.activity_groups);

        BottomNavHelper.setup(this, BottomNavHelper.Tab.GROUPS);

        recyclerView = findViewById(R.id.groupsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupList = new ArrayList<>();
        adapter = new GroupAdapter(groupList, group -> {
            Intent intent = new Intent(GroupsActivity.this, GroupDetailActivity.class);
            intent.putExtra(GroupDetailActivity.EXTRA_GROUP_NAME, group.getGroupName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadGroups();
    }

    private void loadGroups() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("expenses")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    Map<String, Double> totals = new LinkedHashMap<>();
                    Map<String, Integer> counts = new LinkedHashMap<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String group = doc.getString("group");
                        if (group == null || group.isEmpty()) group = "General";

                        Double amount = doc.getDouble("amount");
                        double amt = amount != null ? amount : 0;

                        Boolean isIncoming = doc.getBoolean("isIncoming");
                        boolean incoming = isIncoming != null && isIncoming;

                        @SuppressWarnings("unchecked")
                        List<String> splitNames = (List<String>) doc.get("splitNames");

                        String paidBy = doc.getString("paidBy");
                        if (paidBy == null || paidBy.isEmpty()) paidBy = "You";

                        double signedAmount;
                        if (splitNames == null || splitNames.isEmpty()) {
                            signedAmount = incoming ? amt : -amt;
                        } else {
                            double share = amt / (splitNames.size() + 1);
                            if ("You".equalsIgnoreCase(paidBy)) {
                                signedAmount = share * splitNames.size();
                            } else {
                                signedAmount = -share;
                            }
                        }

                        double current = totals.getOrDefault(group, 0.0);
                        totals.put(group, current + signedAmount);

                        counts.put(group, counts.getOrDefault(group, 0) + 1);
                    }

                    groupList.clear();
                    for (String group : totals.keySet()) {
                        groupList.add(new GroupSummary(group, totals.get(group), counts.get(group)));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}