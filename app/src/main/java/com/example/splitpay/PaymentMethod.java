package com.example.splitpay;

public class PaymentMethod {

    private String id;
    private String type;       // "Credit Card", "Debit Card", or "Bank Account"
    private String last4;      // last 4 digits
    private String holderName;

    public PaymentMethod(String id, String type, String last4, String holderName) {
        this.id = id;
        this.type = type;
        this.last4 = last4;
        this.holderName = holderName;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getLast4() { return last4; }
    public String getHolderName() { return holderName; }
}