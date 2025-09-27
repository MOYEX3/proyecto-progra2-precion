package com.example.precional.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.precional.data.entity.BloodPressureRecord;

import java.util.List;

@Dao
public interface BloodPressureDao {

    @Insert
    void insert(BloodPressureRecord record);

    @Update
    void update(BloodPressureRecord record);

    @Delete
    void delete(BloodPressureRecord record);

    @Query("SELECT * FROM blood_pressure_records ORDER BY timestamp DESC")
    List<BloodPressureRecord> getAllRecords();

    @Query("SELECT * FROM blood_pressure_records ORDER BY timestamp DESC LIMIT 1")
    BloodPressureRecord getLatestRecord();

    @Query("SELECT * FROM blood_pressure_records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    List<BloodPressureRecord> getRecordsByDateRange(long startTime, long endTime);

    @Query("SELECT * FROM blood_pressure_records ORDER BY timestamp DESC LIMIT 7")
    List<BloodPressureRecord> getLast7Records();

    @Query("SELECT * FROM blood_pressure_records WHERE id = :id")
    BloodPressureRecord getRecordById(int id);

    @Query("SELECT COUNT(*) FROM blood_pressure_records")
    int getRecordCount();

    @Query("DELETE FROM blood_pressure_records")
    void deleteAllRecords();

    // Consultas para estadísticas
    @Query("SELECT AVG(systolic) FROM blood_pressure_records WHERE timestamp >= :startTime AND timestamp <= :endTime")
    double getAverageSystolic(long startTime, long endTime);

    @Query("SELECT AVG(diastolic) FROM blood_pressure_records WHERE timestamp >= :startTime AND timestamp <= :endTime")
    double getAverageDiastolic(long startTime, long endTime);
}
