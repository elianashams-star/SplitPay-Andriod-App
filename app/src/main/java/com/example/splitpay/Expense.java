package com.example.splitpay;

public class Expense {

    private String title;
    private String merchant;
    private double amount;
    private String group;
    private String date;
    private String paymentMethod;
    private String referenceId;
    private boolean isIncoming;

    public Expense(String title,
                   String merchant,
                   double amount,
                   String group,
                   String date,
                   String paymentMethod,
                   String referenceId,
                   boolean isIncoming) {

        this.title = title;
        this.merchant = merchant;
        this.amount = amount;
        this.group = group;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.referenceId = referenceId;
        this.isIncoming = isIncoming;
    }

    public String getTitle() { return title; }
    public String getMerchant() { return merchant; }
    public double getAmount() { return amount; }
    public String getGroup() { return group; }
    public String getDate() { return date; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReferenceId() { return referenceId; }
    public boolean isIncoming() { return isIncoming; }
}