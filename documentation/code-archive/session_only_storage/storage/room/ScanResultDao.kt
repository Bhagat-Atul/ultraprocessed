package com.b2.ultraprocessed.storage.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    // Save a new scan result
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResult)

    // Get all scan results, newest first
    @Query("SELECT * FROM scan_results ORDER BY scannedAt DESC")
    fun getAllScanResults(): Flow<List<ScanResult>>

    // Get a single scan result by id
    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getScanResultById(id: Long): ScanResult?

    // Delete a single scan result
    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteScanResultById(id: Long)

    // Delete all scan results (for when user clears history)
    @Query("DELETE FROM scan_results")
    suspend fun deleteAllScanResults()
}