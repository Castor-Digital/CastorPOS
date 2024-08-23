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

    @ColumnInfo(name = "is_credit")
    public boolean isCredit; // to differentiate between cash and credit results

    @ColumnInfo(name = "time")
    private String time;

    public SavedResult(String resultText, String serverName, int customers, boolean isCredit, String time) {
        this.resultText = resultText;
        this.serverName = serverName;
        this.customers = customers;
        this.isCredit = isCredit;
        this.amount = parseAmount(resultText);
        this.time = time;
    }

    private double parseAmount(String resultText) {
        // Remove commas and other non-digit characters except for the decimal point
        String cleanedResultText = resultText.replaceAll("[^\\d.]", "");

        // Check if the cleaned text is empty or just a period
        if (cleanedResultText.isEmpty() || cleanedResultText.equals(".")) {
            return 0.0;
        }

        try {
            return Double.parseDouble(cleanedResultText);
        } catch (NumberFormatException e) {
            // Log an error or handle the case where parsing fails
            e.printStackTrace();
            return 0.0;
        }
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

    public boolean isCredit() {
        return isCredit;
    }

    public String getTime() {
        return time;
    }

}