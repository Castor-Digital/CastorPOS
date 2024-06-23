package com.castorpos;

// SavedResult object
class SavedResult {
    private String result;
    private String server;
    private int customers;

    public SavedResult(String result, String server, int customers) {
        this.result = result;
        this.server = server;
        this.customers = customers;
    }

    public String getResult() {
        return result;
    }

    public String getServer() {
        return server;
    }

    public int getCustomers() {
        return customers;
    }
}
