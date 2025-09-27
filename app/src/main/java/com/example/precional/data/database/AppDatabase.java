package com.example.precional.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.precional.data.dao.BloodPressureDao;
import com.example.precional.data.dao.UserSettingsDao;
import com.example.precional.data.entity.BloodPressureRecord;
import com.example.precional.data.entity.UserSettings;

@Database(
    entities = {BloodPressureRecord.class, UserSettings.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BloodPressureDao bloodPressureDao();
    public abstract UserSettingsDao userSettingsDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "blood_pressure_database")
                            .allowMainThreadQueries() // Para simplicidad, en producción usar AsyncTask o Executors
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
