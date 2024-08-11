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
    private Button clearDataButton;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals);

        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        totalCreditRevenueTextView = findViewById(R.id.totalCCRevenueTextView);
        clearDataButton = findViewById(R.id.clearDataButton);

        database = AppDatabase.getDatabase(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        loadTotals();
        clearDataButton.setOnClickListener(v -> clearAllData());
    }

    private void loadTotals() {
        executorService.execute(() -> {
            List<SavedResult> allResults = database.resultsDao().getAllResults();
            double totalRevenue = 0.0;
            double totalCreditRevenue = 0.0;

            for (SavedResult result : allResults) {
                String resultText = result.getResultText().replace("$", "").replace(",", "");
                double amount = Double.parseDouble(resultText);
                if (result.isCredit()) {
                    totalCreditRevenue += amount;
                } else {
                    totalRevenue += amount;
                }
            }

            final double finalTotalRevenue = totalRevenue;
            final double finalTotalCreditRevenue = totalCreditRevenue;

            runOnUiThread(() -> {
                totalRevenueTextView.setText(String.format("Total Revenue: $%.2f", finalTotalRevenue));
                totalCreditRevenueTextView.setText(String.format("Total Credit Card Revenue: $%.2f", finalTotalCreditRevenue));
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
