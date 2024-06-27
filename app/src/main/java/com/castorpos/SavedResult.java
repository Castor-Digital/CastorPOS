package com.castorpos;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_results")
public class SavedResult {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "result_text")
    public String resultText;

    @ColumnInfo(name = "server_name")
    public String serverName;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "customers")
    public int customers;

    // Constructor, getters, and setters...
    public SavedResult(String resultText, String serverName, int customers) {
        this.resultText = resultText;
        this.serverName = serverName;
        this.customers = customers;
        this.amount = parseAmount(resultText);
    }

    private double parseAmount(String resultText) {
        // Add logic to parse amount from resultText, e.g., if resultText is "$10.50", extract 10.50
        // Assuming resultText is in the format "$10.50"
        return Double.parseDouble(resultText.replaceAll("[^0-9.]", ""));
    }

    public String getResultText() {
        return resultText;
    }

    public String getServerName() {
        return serverName;
    }

    public double getAmount() {
        return amount;
    }

    public int getCustomers() {
        return customers;
    }
}
