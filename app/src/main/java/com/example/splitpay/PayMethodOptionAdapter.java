package com.example.splitpay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PayMethodOptionAdapter extends RecyclerView.Adapter<PayMethodOptionAdapter.ViewHolder> {

    public interface OnSelectListener {
        void onSelect(PaymentMethod method);
    }

    private final List<PaymentMethod> methods;
    private final OnSelectListener listener;
    private String selectedId = null;

    public PayMethodOptionAdapter(List<PaymentMethod> methods, OnSelectListener listener) {
        this.methods = methods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pay_method_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod method = methods.get(position);
        String type = method.getType() == null ? "Credit Card" : method.getType();
        String last4 = method.getLast4() == null ? "0000" : method.getLast4();

        if (type.equals("Credit Card")) {
            holder.optionBackground.setBackgroundResource(R.drawable.bg_card_credit);
            holder.optionTypeText.setText("CREDIT");
        } else if (type.equals("Debit Card")) {
            holder.optionBackground.setBackgroundResource(R.drawable.bg_card_debit);
            holder.optionTypeText.setText("DEBIT");
        } else {
            holder.optionBackground.setBackgroundResource(R.drawable.bg_card_bank);
            holder.optionTypeText.setText("BANK ACCOUNT");
        }

        holder.optionNumberText.setText("•••• " + last4);

        boolean selected = method.getId().equals(selectedId);

        if (selected) {
            holder.optionIndicator.setBackgroundResource(R.drawable.bg_indicator_selected);
            holder.optionCheckText.setVisibility(View.VISIBLE);
            holder.optionCard.setAlpha(1f);
        } else {
            holder.optionIndicator.setBackgroundResource(R.drawable.bg_indicator_unselected);
            holder.optionCheckText.setVisibility(View.GONE);
            holder.optionCard.setAlpha(0.75f);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedId = method.getId();
            notifyDataSetChanged();
            if (listener != null) listener.onSelect(method);
        });
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout optionCard, optionIndicator;
        LinearLayout optionBackground;
        TextView optionTypeText, optionNumberText, optionCheckText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            optionCard = itemView.findViewById(R.id.optionCard);
            optionBackground = itemView.findViewById(R.id.optionBackground);
            optionIndicator = itemView.findViewById(R.id.optionIndicator);
            optionTypeText = itemView.findViewById(R.id.optionTypeText);
            optionNumberText = itemView.findViewById(R.id.optionNumberText);
            optionCheckText = itemView.findViewById(R.id.optionCheckText);
        }
    }
}
