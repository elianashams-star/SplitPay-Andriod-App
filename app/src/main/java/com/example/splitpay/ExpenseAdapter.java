package com.example.splitpay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private final List<Expense> expenseList;

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense e = expenseList.get(position);

        holder.titleText.setText(e.getTitle());
        holder.merchantText.setText(e.getMerchant());
        holder.groupValueText.setText(e.getGroup());
        holder.dateValueText.setText(e.getDate());
        holder.methodValueText.setText(e.getPaymentMethod());
        holder.refValueText.setText(e.getReferenceId());

        boolean incoming = e.isIncoming();

        holder.amountText.setText(String.format(Locale.US, "%s$%.2f", incoming ? "+" : "-", e.getAmount()));

        int amountColor = ContextCompat.getColor(holder.itemView.getContext(),
                incoming ? R.color.accent_green : R.color.accent_red);
        holder.amountText.setTextColor(amountColor);

        holder.statusText.setText(incoming ? "Incoming" : "Outgoing");
        holder.statusText.setTextColor(amountColor);

        // Glide - load avatar image
        String avatarUrl = "https://i.pravatar.cc/150?u=" + e.getReferenceId();
        Glide.with(holder.itemView.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.bg_icon_circle)
                .error(R.drawable.bg_icon_circle)
                .circleCrop()
                .into(holder.iconCircle);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iconCircle;
        TextView titleText;
        TextView merchantText;
        TextView amountText;
        TextView groupValueText;
        TextView dateValueText;
        TextView methodValueText;
        TextView refValueText;
        TextView statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iconCircle = itemView.findViewById(R.id.iconCircle);
            titleText = itemView.findViewById(R.id.titleText);
            merchantText = itemView.findViewById(R.id.merchantText);
            amountText = itemView.findViewById(R.id.amountText);
            groupValueText = itemView.findViewById(R.id.groupValueText);
            dateValueText = itemView.findViewById(R.id.dateValueText);
            methodValueText = itemView.findViewById(R.id.methodValueText);
            refValueText = itemView.findViewById(R.id.refValueText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}