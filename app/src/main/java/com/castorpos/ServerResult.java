package com.castorpos;

public class ServerResult {
    public String serverName;  // Make sure this field exists and matches the alias in the query
    public double totalRevenue;
    public int totalCustomers;

    // Ensure you provide a constructor if needed, but public fields should work
    public ServerResult(String serverName, double totalRevenue, int totalCustomers) {
        this.serverName = serverName;
        this.totalRevenue = totalRevenue;
        this.totalCustomers = totalCustomers;
    }
}



