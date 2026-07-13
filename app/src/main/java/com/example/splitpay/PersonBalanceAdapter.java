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

public class PersonBalanceAdapter extends RecyclerView.Adapter<PersonBalanceAdapter.ViewHolder> {

    public interface OnPayClickListener {
        void onPayClick(PersonBalance person);
    }

    private final List<PersonBalance> people;
    private final OnPayClickListener payListener;

    private final int[] avatarColors = {
            R.color.avatar_blue,
            R.color.avatar_purple,
            R.color.avatar_orange,
            R.color.avatar_teal,
            R.color.avatar_pink
    };

    public PersonBalanceAdapter(List<PersonBalance> people, OnPayClickListener payListener) {
        this.people = people;
        this.payListener = payListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_balance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonBalance p = people.get(position);
        double bal = p.getBalance();
        String name = p.getName() == null ? "" : p.getName();

        holder.personNameText.setText(name);

        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        holder.personInitialText.setText(initial);

        int colorIndex = Math.abs(name.hashCode()) % avatarColors.length;
        int avatarColor = ContextCompat.getColor(holder.itemView.getContext(), avatarColors[colorIndex]);
        holder.personInitialText.getBackground().setTint(avatarColor);

        String amountLabel;
        String statusLabel;
        int amountColor;

        if (bal > 0.005) {
            amountLabel = String.format(Locale.US, "+$%.2f", bal);
            statusLabel = "OWES YOU";
            amountColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_green);
            holder.personStatusText.setBackgroundResource(R.drawable.bg_pill_green);
            holder.personStatusText.setTextColor(amountColor);
            holder.payButton.setVisibility(View.GONE);
        } else if (bal < -0.005) {
            amountLabel = String.format(Locale.US, "-$%.2f", Math.abs(bal));
            statusLabel = "YOU OWE";
            amountColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_red);
            holder.personStatusText.setBackgroundResource(R.drawable.bg_pill_red);
            holder.personStatusText.setTextColor(amountColor);
            holder.payButton.setVisibility(View.VISIBLE);
            holder.payButton.setOnClickListener(v -> {
                if (payListener != null) payListener.onPayClick(p);
            });
        } else {
            amountLabel = "$0.00";
            statusLabel = "SETTLED";
            amountColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);
            holder.personStatusText.setBackgroundResource(R.drawable.bg_pill_green);
            holder.personStatusText.setTextColor(amountColor);
            holder.payButton.setVisibility(View.GONE);
        }

        holder.personBalanceText.setText(amountLabel);
        holder.personBalanceText.setTextColor(amountColor);
        holder.personStatusText.setText(statusLabel);
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView personInitialText, personNameText, personStatusText, personBalanceText, payButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personInitialText = itemView.findViewById(R.id.personInitialText);
            personNameText = itemView.findViewById(R.id.personNameText);
            personStatusText = itemView.findViewById(R.id.personStatusText);
            personBalanceText = itemView.findViewById(R.id.personBalanceText);
            payButton = itemView.findViewById(R.id.payButton);
        }
    }
}