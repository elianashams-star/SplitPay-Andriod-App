package com.example.splitpay;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BottomNavHelper {

    public enum Tab { HOME, GROUPS, ACCOUNT }

    public static void setup(AppCompatActivity activity, Tab currentTab) {

        LinearLayout navHome = activity.findViewById(R.id.navHome);
        LinearLayout navGroups = activity.findViewById(R.id.navGroups);
        LinearLayout navAdd = activity.findViewById(R.id.navAdd);
        LinearLayout navAccount = activity.findViewById(R.id.navAccount);

        TextView navHomeText = activity.findViewById(R.id.navHomeText);
        TextView navGroupsText = activity.findViewById(R.id.navGroupsText);
        TextView navAccountText = activity.findViewById(R.id.navAccountText);

        ImageView navHomeIcon = activity.findViewById(R.id.navHomeIcon);
        ImageView navGroupsIcon = activity.findViewById(R.id.navGroupsIcon);
        ImageView navAccountIcon = activity.findViewById(R.id.navAccountIcon);

        int activeColor = ContextCompat.getColor(activity, R.color.text_primary);
        int inactiveColor = ContextCompat.getColor(activity, R.color.text_secondary);

        navHomeText.setTextColor(currentTab == Tab.HOME ? activeColor : inactiveColor);
        navGroupsText.setTextColor(currentTab == Tab.GROUPS ? activeColor : inactiveColor);
        navAccountText.setTextColor(currentTab == Tab.ACCOUNT ? activeColor : inactiveColor);

        navHomeIcon.setColorFilter(currentTab == Tab.HOME ? activeColor : inactiveColor);
        navGroupsIcon.setColorFilter(currentTab == Tab.GROUPS ? activeColor : inactiveColor);
        navAccountIcon.setColorFilter(currentTab == Tab.ACCOUNT ? activeColor : inactiveColor);

        navHome.setOnClickListener(v -> {
            if (currentTab != Tab.HOME) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }
        });

        navGroups.setOnClickListener(v -> {
            if (currentTab != Tab.GROUPS) {
                activity.startActivity(new Intent(activity, GroupsActivity.class));
                activity.finish();
            }
        });

        navAdd.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, AddExpenseActivity.class));
        });

        navAccount.setOnClickListener(v -> {
            if (currentTab != Tab.ACCOUNT) {
                activity.startActivity(new Intent(activity, AccountActivity.class));
                activity.finish();
            }
        });
    }
}