package com.castorpos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TotalsScreen extends AppCompatActivity {

    private TextView totalRevenueTextView;
    private TextView totalCreditRevenueTextView;
    private TextView totalCashRevenueTextView;
    private Button clearDataButton;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals);

        totalCashRevenueTextView = findViewById(R.id.totalCashRevenueTextView);
        totalCreditRevenueTextView = findViewById(R.id.totalCCRevenueTextView);
        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        clearDataButton = findViewById(R.id.clearDataButton);

        database = AppDatabase.getDatabase(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        loadTotals();
        clearDataButton.setOnClickListener(v -> clearAllData());
    }

    private void loadTotals() {
        executorService.execute(() -> {
            List<SavedResult> allResults = database.resultsDao().getAllResults();
            double totalCashRevenue = 0;
            double totalCreditRevenue = 0;
            double totalRevenue = 0;

            for (SavedResult result : allResults) {
                double amount = result.getAmount();
                if (result.isCredit()) {
                    totalCreditRevenue += amount;
                } else {
                    totalCashRevenue += amount;
                }
                totalRevenue += amount;
            }

            final double finalTotalCashRevenue = totalCashRevenue;
            final double finalTotalCreditRevenue = totalCreditRevenue;
            final double finalTotalRevenue = totalRevenue;

            runOnUiThread(() -> {
                totalCashRevenueTextView.setText(String.format("Cash Revenue: $%.2f", finalTotalCashRevenue));
                totalCreditRevenueTextView.setText(String.format("Credit Card Revenue: $%.2f", finalTotalCreditRevenue));
                totalRevenueTextView.setText(String.format("Total Revenue: $%.2f", finalTotalRevenue));
            });
        });
    }


    private void clearAllData() {
        executorService.execute(() -> {
            database.resultsDao().deleteAll();
            runOnUiThread(() -> {
                loadTotals();
                Intent updateIntent = new Intent("update-total-revenue");
                LocalBroadcastManager.getInstance(TotalsScreen.this).sendBroadcast(updateIntent);
                Intent clearIntent = new Intent("clear-all-data");
                LocalBroadcastManager.getInstance(TotalsScreen.this).sendBroadcast(clearIntent);
            });
        });
    }

}
