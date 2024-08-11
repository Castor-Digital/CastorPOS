package com.castorpos;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

@Database(entities = {SavedResult.class}, version = 3, exportSchema = false) // Increment version number to 3
public abstract class AppDatabase extends RoomDatabase {
    public abstract ResultsDao resultsDao();

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE saved_results ADD COLUMN is_credit INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .addMigrations(MIGRATION_2_3)
                            .fallbackToDestructiveMigration() // Use this only for development to recreate the database
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
