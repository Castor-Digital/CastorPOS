package com.castorpos;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SavedResult.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ResultsDao resultsDao();
}
