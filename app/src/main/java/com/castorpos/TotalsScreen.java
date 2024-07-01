package com.castorpos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;
import java.util.List;

public class TotalsScreen extends AppCompatActivity {

    private AppDatabase database;
    private TextView totalRevenueTextView;
    private LinearLayout serverResultsContainer;
    private BroadcastReceiver totalRevenueUpdateReceiver;
    private Button clearDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals);

        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        serverResultsContainer = findViewById(R.id.serverResultsContainer);
        clearDataButton = findViewById(R.id.clearDataButton);

        // Initialize the database
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        // Calculate and display the total revenue
        new CalculateTotalRevenueTask().execute();

        // Register the broadcast receiver
        totalRevenueUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new CalculateTotalRevenueTask().execute();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(totalRevenueUpdateReceiver,
                new IntentFilter("update-total-revenue"));

        // Set the clear data button click listener
        clearDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(totalRevenueUpdateReceiver);
        super.onDestroy();
    }

    private void showConfirmationDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_clear_data, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button negativeButton = dialogView.findViewById(R.id.negativeButton);
        Button positiveButton = dialogView.findViewById(R.id.positiveButton);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClearAllDataTask().execute();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private class CalculateTotalRevenueTask extends AsyncTask<Void, Void, Double> {
        @Override
        protected Double doInBackground(Void... voids) {
            List<SavedResult> savedResults = database.resultsDao().getAllResultsDirect();
            double totalRevenue = 0;
            for (SavedResult result : savedResults) {
                totalRevenue += result.getAmount();
            }
            return totalRevenue;
        }

        @Override
        protected void onPostExecute(Double totalRevenue) {
            totalRevenueTextView.setText("Total Revenue: $" + String.format("%.2f", totalRevenue));
            new CalculateServerResultsTask().execute();
        }
    }

    private class CalculateServerResultsTask extends AsyncTask<Void, Void, List<ServerResults>> {
        @Override
        protected List<ServerResults> doInBackground(Void... voids) {
            return database.resultsDao().getServerResults();
        }

        @Override
        protected void onPostExecute(List<ServerResults> serverResults) {
            serverResultsContainer.removeAllViews();
            for (ServerResults result : serverResults) {
                TextView serverResultView = new TextView(TotalsScreen.this);
                serverResultView.setText(result.serverName + " - $" + String.format("%.2f", result.totalRevenue) + ", " + result.totalCustomers + " customers");
                serverResultView.setTextColor(0xFF222222); // Set text color to #222222
                serverResultsContainer.addView(serverResultView);
            }
        }
    }

    private class ClearAllDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            database.resultsDao().deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Recalculate total revenue after clearing data
            new CalculateTotalRevenueTask().execute();
            // Broadcast that the total revenue needs to be updated
            Intent updateIntent = new Intent("update-total-revenue");
            LocalBroadcastManager.getInstance(TotalsScreen.this).sendBroadcast(updateIntent);
            // Broadcast to clear all data from the sidebar
            Intent clearIntent = new Intent("clear-all-data");
            LocalBroadcastManager.getInstance(TotalsScreen.this).sendBroadcast(clearIntent);
        }
    }
}
