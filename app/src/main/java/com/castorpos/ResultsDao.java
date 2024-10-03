package com.castorpos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ResultsDao {

    @Query("SELECT * FROM saved_results")
    List<SavedResult> getAllResultsDirect();

    @Query("SELECT * FROM saved_results WHERE is_credit = 0")
    List<SavedResult> getCashResults();

    @Insert
    void insert(SavedResult result);

    @Delete
    void delete(SavedResult result);

    @Query("DELETE FROM saved_results")
    void deleteAll();

    @Query("SELECT server_name AS serverName, SUM(amount) AS totalRevenue, SUM(customers) AS totalCustomers FROM saved_results GROUP BY server_name")
    List<ServerResults> getServerResults();

    @Query("SELECT * FROM saved_results")
    List<SavedResult> getAllResults();

    @Query("SELECT * FROM saved_results WHERE is_credit = :isCredit")
    List<SavedResult> getResultsByType(boolean isCredit);
}
