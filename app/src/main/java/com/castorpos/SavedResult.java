package com.castorpos;

import java.util.Objects;

public class SavedResult {
    private String resultText;
    private String serverName;
    private int customers;

    public SavedResult(String resultText, String serverName, int customers) {
        this.resultText = resultText;
        this.serverName = serverName;
        this.customers = customers;
    }

    public String getResultText() {
        return resultText;
    }

    public String getServerName() {
        return serverName;
    }

    public int getCustomers() {
        return customers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedResult that = (SavedResult) o;
        return customers == that.customers &&
                Objects.equals(resultText, that.resultText) &&
                Objects.equals(serverName, that.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultText, serverName, customers);
    }
}
