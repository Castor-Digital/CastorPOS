package com.castorpos;

import androidx.room.ColumnInfo;

public class ServerResults {
    @ColumnInfo(name = "serverName")
    public String serverName;

    @ColumnInfo(name = "totalRevenue")
    public double totalRevenue;

    @ColumnInfo(name = "totalCustomers")
    public int totalCustomers;
}
