package com.castorpos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
<<<<<<< Updated upstream
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
=======

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

>>>>>>> Stashed changes
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public class TotalsScreen extends AppCompatActivity {

    private TextView totalRevenueTextView;
    private TextView totalCreditRevenueTextView;
    private TextView totalCashRevenueTextView;
    private TextView resultsByStaffTextView;  // New TextView for results by server
    private Button clearDataButton;
    private AppDatabase database;
    private ExecutorService executorService;
    private static final String SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY"; // Use your SendGrid API key
    private OkHttpClient httpClient = new OkHttpClient();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals);

        totalCashRevenueTextView = findViewById(R.id.totalCashRevenueTextView);
        totalCreditRevenueTextView = findViewById(R.id.totalCCRevenueTextView);
        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        resultsByStaffTextView = findViewById(R.id.resultsByStaffTextView);  // Initialize the new TextView
<<<<<<< Updated upstream
=======
        //saveDataButton = findViewById(R.id.saveDataButton);
>>>>>>> Stashed changes
        clearDataButton = findViewById(R.id.clearDataButton);

        database = AppDatabase.getDatabase(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        loadTotals();
<<<<<<< Updated upstream
=======
        //saveDataButton.setOnClickListener(v -> saveData());
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream

=======
/*
    private void saveData() {
        String totalCashRevenue = totalCashRevenueTextView.getText().toString();
        String totalCreditRevenue = totalCreditRevenueTextView.getText().toString();
        String totalRevenue = totalRevenueTextView.getText().toString();
        String resultsByServer = resultsByStaffTextView.getText().toString();
>>>>>>> Stashed changes


<<<<<<< Updated upstream
=======
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Summary for ").append(currentDate).append("\n\n");
        reportContent.append(totalCashRevenue).append("\n");
        reportContent.append(totalCreditRevenue).append("\n");
        reportContent.append(totalRevenue).append("\n\n\n");
        reportContent.append("Results by Server:\n");

        String[] lines = resultsByServer.split("\n");
        for (String line : lines) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                String serverInfo = parts[0].trim();
                String total = parts[1].trim().replace("Total: ", "($");
                reportContent.append(serverInfo).append(" ").append(total).append(")\n");
            } else {
                reportContent.append(line).append("\n");
            }
        }

        // Send the email using OkHttp and SendGrid
        executorService.execute(() -> {
            try {
                sendEmailUsingOkHttp(reportContent.toString(), currentDate);
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(TotalsScreen.this, "Failed to send report", Toast.LENGTH_SHORT).show());
            }
        });
    }
>>>>>>> Stashed changes

    private void sendEmailUsingOkHttp(String reportContent, String currentDate) throws IOException {
        // Prepare the JSON payload for the SendGrid API
        JSONObject json = new JSONObject();
        JSONObject personalizations = new JSONObject();
        JSONObject to = new JSONObject();
        try {
            to.put("email", "trevforesta@gmail.com");
            personalizations.put("to", new JSONObject[]{to});
            json.put("personalizations", new JSONObject[]{personalizations});
            json.put("from", new JSONObject().put("email", "contact@castordigital.co"));
            json.put("subject", "Summary for " + currentDate);
            json.put("content", new JSONObject[]{new JSONObject().put("type", "text/plain").put("value", reportContent)});
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Use OkHttp to send the POST request to SendGrid
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url("https://api.sendgrid.com/v3/mail/send")
                .addHeader("Authorization", "Bearer " + SENDGRID_API_KEY)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                runOnUiThread(() -> Toast.makeText(TotalsScreen.this, "Report Sent ✅", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> Toast.makeText(TotalsScreen.this, "Failed to send report: " + response.message(), Toast.LENGTH_SHORT).show());
            }
        }
    }
 */

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
