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
    private TextView resultsByStaffTextView;  // New TextView for results by server
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
        resultsByStaffTextView = findViewById(R.id.resultsByStaffTextView);  // Initialize the new TextView
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

            // Get results grouped by server
            List<ServerResult> serverResults = database.resultsDao().getResultsByServer();

            // Build the string to display per-server results
            StringBuilder resultsByServerBuilder = new StringBuilder();

            if (serverResults.isEmpty()) {
                resultsByServerBuilder.append("No results yet.");
            } else {
                for (ServerResult serverResult : serverResults) {
                    String serverName = serverResult.serverName;
                    int customers = serverResult.totalCustomers;
                    double revenue = serverResult.totalRevenue;

                    if ("Gift Certificate".equalsIgnoreCase(serverName)) {
                        resultsByServerBuilder.append(String.format("Gift Certificates Sold: %d, Total: $%.2f\n", customers, revenue));
                    } else if ("To-Go".equalsIgnoreCase(serverName)) {
                        resultsByServerBuilder.append(String.format("To-Go Orders: %d, Total: $%.2f\n", customers, revenue));
                    } else {
                        resultsByServerBuilder.append(String.format("%s: %d customers | Total: $%.2f\n", serverName, customers, revenue));
                    }
                }
            }

            for (SavedResult result : allResults) {
                double amount = result.getAmount();
                if (result.isCredit()) {
                    totalCreditRevenue += amount;
                } else {
                    totalCashRevenue += amount;
                }
                totalRevenue = totalCashRevenue + totalCreditRevenue;  // Ensure total revenue is calculated correctly
            }

            final double finalTotalCashRevenue = totalCashRevenue;
            final double finalTotalCreditRevenue = totalCreditRevenue;
            final double finalTotalRevenue = totalRevenue;  // Capture the total revenue
            final String finalResultsByServer = resultsByServerBuilder.toString();

            runOnUiThread(() -> {
                totalCashRevenueTextView.setText(String.format("Cash Revenue: $%.2f", finalTotalCashRevenue));
                totalCreditRevenueTextView.setText(String.format("Credit Card Revenue: $%.2f", finalTotalCreditRevenue));
                totalRevenueTextView.setText(String.format("Total Revenue: $%.2f", finalTotalRevenue));  // Make sure this is updated

                // Display results by server or "No results yet."
                resultsByStaffTextView.setText(finalResultsByServer);
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
