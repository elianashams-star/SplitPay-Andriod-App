package com.example.splitpay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(PaymentMethod method);
    }

    private final List<PaymentMethod> methods;
    private final OnDeleteListener listener;

    public PaymentMethodAdapter(List<PaymentMethod> methods, OnDeleteListener listener) {
        this.methods = methods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod method = methods.get(position);
        String type = method.getType() == null ? "Credit Card" : method.getType();
        String last4 = method.getLast4() == null ? "0000" : method.getLast4();
        String holderName = method.getHolderName() == null ? "SPLITPAY USER" : method.getHolderName().toUpperCase();

        holder.cardHolderText.setText(holderName);
        holder.mastercardDots.setVisibility(View.GONE);

        if (type.equals("Credit Card")) {
            holder.cardBackground.setBackgroundResource(R.drawable.bg_card_credit);
            holder.cardTypeLabel.setText("CREDIT");
            holder.cardNumberText.setText("•••• •••• •••• " + last4);
            holder.cardBrandText.setVisibility(View.VISIBLE);
            holder.cardBrandText.setText("Visa");
        } else if (type.equals("Debit Card")) {
            holder.cardBackground.setBackgroundResource(R.drawable.bg_card_debit);
            holder.cardTypeLabel.setText("DEBIT");
            holder.cardNumberText.setText("•••• •••• •••• " + last4);
            holder.cardBrandText.setVisibility(View.GONE);
            holder.mastercardDots.setVisibility(View.VISIBLE);
        } else {
            holder.cardBackground.setBackgroundResource(R.drawable.bg_card_bank);
            holder.cardTypeLabel.setText("BANK ACCOUNT");
            holder.cardNumberText.setText("Account •••• " + last4);
            holder.cardBrandText.setVisibility(View.VISIBLE);
            holder.cardBrandText.setText("Bank");
        }

        holder.deleteMethodBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(method);
        });
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardBackground;
        TextView cardTypeLabel, cardNumberText, cardHolderText, cardBrandText;
        LinearLayout mastercardDots;
        ImageButton deleteMethodBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBackground = itemView.findViewById(R.id.cardBackground);
            cardTypeLabel = itemView.findViewById(R.id.cardTypeLabel);
            cardNumberText = itemView.findViewById(R.id.cardNumberText);
            cardHolderText = itemView.findViewById(R.id.cardHolderText);
            cardBrandText = itemView.findViewById(R.id.cardBrandText);
            mastercardDots = itemView.findViewById(R.id.mastercardDots);
            deleteMethodBtn = itemView.findViewById(R.id.deleteMethodBtn);
        }
    }
}