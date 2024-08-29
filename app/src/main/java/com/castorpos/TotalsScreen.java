package com.castorpos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TotalsScreen extends AppCompatActivity {

    private TextView totalCashRevenueTextView;
    private TextView totalCreditRevenueTextView;
    private TextView totalRevenueTextView;

    private Button clearDataButton;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals);

        // Initialize views
        totalCashRevenueTextView = findViewById(R.id.totalCashRevenueTextView);
        totalCreditRevenueTextView = findViewById(R.id.totalCCRevenueTextView);
        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);

        clearDataButton = findViewById(R.id.clearDataButton);

        // Initialize database and executor
        database = AppDatabase.getDatabase(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

        // Load totals initially
        loadTotals();

        // Set clear data button click listener
        clearDataButton.setOnClickListener(v -> clearAllData());
    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh totals every time the screen is resumed
        loadTotals();
    }
    */

    private void loadTotals() {
        executorService.execute(() -> {
            List<SavedResult> allResults = database.resultsDao().getAllResults();
            double totalCashRevenue = 0;
            double totalCreditRevenue = 0;

            // Calculate totals
            for (SavedResult result : allResults) {
                if (result.isCredit()) {
                    totalCreditRevenue += result.getAmount();
                } else {
                    totalCashRevenue += result.getAmount();
                }
            }

            // Update the UI on the main thread
            double finalTotalCashRevenue = totalCashRevenue;
            double finalTotalCreditRevenue = totalCreditRevenue;
            double finalTotalRevenue = (totalCashRevenue + totalCreditRevenue);
            runOnUiThread(() -> {
                totalCashRevenueTextView.setText(String.format("Cash Revenue: $%.2f", finalTotalCashRevenue));
                totalCreditRevenueTextView.setText(String.format("Credit Revenue: $%.2f", finalTotalCreditRevenue));
                totalRevenueTextView.setText(String.format("Total Revenue: $%.2f", finalTotalRevenue));
            });
        });
    }

    private void clearAllData() {
        executorService.execute(() -> {
            database.resultsDao().deleteAll();
            runOnUiThread(this::loadTotals);
        });
    }
}
