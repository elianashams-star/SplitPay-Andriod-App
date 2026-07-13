package com.example.splitpay;

public class GroupSummary {

    private String groupName;
    private double netBalance;
    private int expenseCount;

    public GroupSummary(String groupName, double netBalance, int expenseCount) {
        this.groupName = groupName;
        this.netBalance = netBalance;
        this.expenseCount = expenseCount;
    }

    public String getGroupName() { return groupName; }
    public double getNetBalance() { return netBalance; }
    public int getExpenseCount() { return expenseCount; }
}
