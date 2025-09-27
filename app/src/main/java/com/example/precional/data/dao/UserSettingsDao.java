package com.example.precional.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.precional.data.entity.UserSettings;

@Dao
public interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserSettings settings);

    @Update
    void update(UserSettings settings);

    @Query("SELECT * FROM user_settings WHERE id = 1")
    UserSettings getUserSettings();

    @Query("UPDATE user_settings SET userName = :name WHERE id = 1")
    void updateUserName(String name);

    @Query("UPDATE user_settings SET userAge = :age WHERE id = 1")
    void updateUserAge(int age);

    @Query("UPDATE user_settings SET userGender = :gender WHERE id = 1")
    void updateUserGender(String gender);

    @Query("UPDATE user_settings SET morningReminder = :enabled WHERE id = 1")
    void updateMorningReminder(boolean enabled);

    @Query("UPDATE user_settings SET eveningReminder = :enabled WHERE id = 1")
    void updateEveningReminder(boolean enabled);

    @Query("UPDATE user_settings SET isDarkModeEnabled = :enabled WHERE id = 1")
    void updateDarkMode(boolean enabled);

    @Query("UPDATE user_settings SET emergencyContactName = :name, emergencyContactPhone = :phone WHERE id = 1")
    void updateEmergencyContact(String name, String phone);

    @Query("SELECT COUNT(*) FROM user_settings WHERE id = 1")
    int hasSettings();
}
