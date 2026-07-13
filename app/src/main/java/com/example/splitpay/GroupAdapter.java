package com.example.splitpay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(GroupSummary group);
    }

    private final List<GroupSummary> groupList;
    private final OnGroupClickListener listener;

    public GroupAdapter(List<GroupSummary> groupList, OnGroupClickListener listener) {
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupSummary g = groupList.get(position);

        holder.groupNameText.setText(g.getGroupName());
        holder.expenseCountText.setText(g.getExpenseCount() + (g.getExpenseCount() == 1 ? " expense" : " expenses"));

        double net = g.getNetBalance();
        String sign = net >= 0 ? "+" : "-";
        holder.groupBalanceText.setText(String.format(Locale.US, "%s$%.2f", sign, Math.abs(net)));

        int color = ContextCompat.getColor(holder.itemView.getContext(),
                net >= 0 ? R.color.accent_green : R.color.accent_red);
        holder.groupBalanceText.setTextColor(color);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(g);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameText, expenseCountText, groupBalanceText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameText = itemView.findViewById(R.id.groupNameText);
            expenseCountText = itemView.findViewById(R.id.expenseCountText);
            groupBalanceText = itemView.findViewById(R.id.groupBalanceText);
        }
    }
}
