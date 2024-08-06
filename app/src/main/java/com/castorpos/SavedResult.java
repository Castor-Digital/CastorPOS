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

    public SavedResult(String resultText, String serverName, int customers, boolean isCredit) {
        this.resultText = resultText;
        this.serverName = serverName;
        this.customers = customers;
        this.isCredit = isCredit;
        this.amount = parseAmount(resultText);
    }

    private double parseAmount(String resultText) {
        // TODO: (Not yet functional) Remove commas from the string before parsing to double
        String cleanedResultText = resultText.replaceAll("[^\\d.]", "");
        return Double.parseDouble(cleanedResultText);
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
}