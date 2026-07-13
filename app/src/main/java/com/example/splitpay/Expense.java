package com.example.splitpay;

import java.util.ArrayList;
import java.util.List;

public class Expense {

    private String title;
    private String merchant;
    private double amount;
    private String group;
    private String date;
    private String paymentMethod;
    private String referenceId;
    private boolean isIncoming;
    private List<String> splitNames;
    private String paidBy;

    public Expense(String title,
                   String merchant,
                   double amount,
                   String group,
                   String date,
                   String paymentMethod,
                   String referenceId,
                   boolean isIncoming,
                   List<String> splitNames,
                   String paidBy) {

        this.title = title;
        this.merchant = merchant;
        this.amount = amount;
        this.group = group;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.referenceId = referenceId;
        this.isIncoming = isIncoming;
        this.splitNames = splitNames != null ? splitNames : new ArrayList<>();
        this.paidBy = (paidBy == null || paidBy.isEmpty()) ? "You" : paidBy;
    }

    public String getTitle() { return title; }
    public String getMerchant() { return merchant; }
    public double getAmount() { return amount; }
    public String getGroup() { return group; }
    public String getDate() { return date; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReferenceId() { return referenceId; }
    public boolean isIncoming() { return isIncoming; }
    public List<String> getSplitNames() { return splitNames; }
    public String getPaidBy() { return paidBy; }
}