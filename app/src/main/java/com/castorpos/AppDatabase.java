package com.castorpos;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

@Database(entities = {SavedResult.class}, version = 3, exportSchema = false) // Updated version to 3
public abstract class AppDatabase extends RoomDatabase {
    public abstract ResultsDao resultsDao();

    // Migration from version 1 to 2 to add the 'serverName' column
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE saved_results ADD COLUMN serverName TEXT");
        }
    };

    // Migration from version 2 to 3 to add the 'is_credit' column
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Added both migrations
                            .fallbackToDestructiveMigration() // Use this only for development to recreate the database
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
