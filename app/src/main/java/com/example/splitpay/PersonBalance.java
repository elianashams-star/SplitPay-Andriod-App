package com.example.splitpay;

import java.util.ArrayList;
import java.util.List;

public class PersonBalance {

    public static class Contribution {
        private String expenseTitle;
        private double shareAmount; // positive = they owe you, negative = you owe them

        public Contribution(String expenseTitle, double shareAmount) {
            this.expenseTitle = expenseTitle;
            this.shareAmount = shareAmount;
        }

        public String getExpenseTitle() { return expenseTitle; }
        public double getShareAmount() { return shareAmount; }
    }

    private String name;
    private double balance;
    private List<Contribution> contributions;

    public PersonBalance(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.contributions = new ArrayList<>();
    }

    public String getName() { return name; }
    public double getBalance() { return balance; }
    public List<Contribution> getContributions() { return contributions; }

    public void addContribution(String expenseTitle, double shareAmount) {
        contributions.add(new Contribution(expenseTitle, shareAmount));
    }
}