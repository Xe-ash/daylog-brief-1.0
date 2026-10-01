package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class WriteStatus {
    SUCCESS,
    FAILED,
    PENDING,
    SKIPPED_GUEST,
    OFFLINE_CACHED
}

data class FirestoreWriteLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val operation: String,
    val path: String,
    val status: WriteStatus,
    val message: String? = null,
    val durationMs: Long = 0L
)

sealed class SyncState {
    object LocalOnly : SyncState()
    object Syncing : SyncState()
    object Synced : SyncState()
    object OfflineCached : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Real-time diagnostic tracker and write debugger for Firestore operations.
 */
object FirestoreDebugManager {
    private const val TAG = "DaylogFirestore"
    private const val MAX_LOGS = 50

    private val _syncState = MutableStateFlow<SyncState>(SyncState.LocalOnly)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _writeLogs = MutableStateFlow<List<FirestoreWriteLog>>(emptyList())
    val writeLogs: StateFlow<List<FirestoreWriteLog>> = _writeLogs.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _totalSucceeded = MutableStateFlow(0)
    val totalSucceeded: StateFlow<Int> = _totalSucceeded.asStateFlow()

    private val _totalFailed = MutableStateFlow(0)
    val totalFailed: StateFlow<Int> = _totalFailed.asStateFlow()

    fun setSyncState(state: SyncState) {
        _syncState.value = state
    }

    fun recordWriteSuccess(operation: String, path: String, durationMs: Long) {
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _totalSucceeded.value += 1
        _syncState.value = SyncState.Synced
        addLog(
            FirestoreWriteLog(
                operation = operation,
                path = path,
                status = WriteStatus.SUCCESS,
                message = "Write confirmed in ${durationMs}ms",
                durationMs = durationMs
            )
        )
        Log.i(TAG, "[$operation] SUCCESS at $path (${durationMs}ms)")
    }

    fun recordWriteFailure(operation: String, path: String, e: Throwable, durationMs: Long) {
        _totalFailed.value += 1
        val errorMsg = formatFirestoreError(e)
        _syncState.value = SyncState.Error(errorMsg)
        addLog(
            FirestoreWriteLog(
                operation = operation,
                path = path,
                status = WriteStatus.FAILED,
                message = errorMsg,
                durationMs = durationMs
            )
        )
        Log.e(TAG, "[$operation] FAILED at $path: $errorMsg", e)
    }

    fun recordGuestSkipped(operation: String, path: String) {
        addLog(
            FirestoreWriteLog(
                operation = operation,
                path = path,
                status = WriteStatus.SKIPPED_GUEST,
                message = "Guest mode: saved to local memory only. Sign in to sync to cloud."
            )
        )
        Log.d(TAG, "[$operation] SKIPPED (Guest mode) at $path")
    }

    fun recordOfflineCached(operation: String, path: String, durationMs: Long) {
        _syncState.value = SyncState.OfflineCached
        addLog(
            FirestoreWriteLog(
                operation = operation,
                path = path,
                status = WriteStatus.OFFLINE_CACHED,
                message = "Saved to Firestore local offline cache (${durationMs}ms)",
                durationMs = durationMs
            )
        )
        Log.w(TAG, "[$operation] CACHED OFFLINE at $path (${durationMs}ms)")
    }

    private fun addLog(log: FirestoreWriteLog) {
        val current = _writeLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > MAX_LOGS) {
            _writeLogs.value = current.take(MAX_LOGS)
        } else {
            _writeLogs.value = current
        }
    }

    fun clearLogs() {
        _writeLogs.value = emptyList()
    }

    /**
     * Executes a live write/read/delete probe against Firestore to test credentials & security rules.
     */
    suspend fun runConnectionDiagnostic(firestore: FirebaseFirestore, uid: String): Result<String> {
        if (uid.isBlank() || uid == "local_guest") {
            return Result.failure(Exception("Not authenticated. Sign in with Google or Email first to run Cloud Diagnostics."))
        }

        val testDocId = "probe_${System.currentTimeMillis()}"
        val testRef = firestore.collection("users").document(uid)
            .collection("_diagnostics").document(testDocId)

        val startTime = System.currentTimeMillis()
        return try {
            _syncState.value = SyncState.Syncing

            // Step 1: Write test probe
            val probeData = mapOf(
                "probeId" to testDocId,
                "uid" to uid,
                "timestamp" to System.currentTimeMillis(),
                "testName" to "Firestore Connection Probe",
                "clientApp" to "Daylog"
            )
            testRef.set(probeData).await()
            val writeDuration = System.currentTimeMillis() - startTime

            // Step 2: Read test probe back
            val snapshot = testRef.get().await()
            if (!snapshot.exists()) {
                throw Exception("Probe was written but could not be read back.")
            }

            // Step 3: Cleanup probe document
            testRef.delete().await()

            val totalDuration = System.currentTimeMillis() - startTime
            val summary = "Connection Verified! Write latency: ${writeDuration}ms, Total RTT: ${totalDuration}ms. Target Path: users/$uid"
            recordWriteSuccess("DIAGNOSTIC_PROBE", testRef.path, totalDuration)
            Result.success(summary)
        } catch (e: Exception) {
            val formatted = formatFirestoreError(e)
            recordWriteFailure("DIAGNOSTIC_PROBE", testRef.path, e, System.currentTimeMillis() - startTime)
            Result.failure(Exception(formatted, e))
        }
    }

    fun formatFirestoreError(e: Throwable): String {
        if (e is FirebaseFirestoreException) {
            return when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "PERMISSION_DENIED: Firebase Security Rules blocked this write. Check Firestore Rules in Firebase Console."
                FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                    "UNAUTHENTICATED: Request lacks valid Firebase Auth credentials. Please sign in again."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "UNAVAILABLE: Cloud Firestore service is unreachable or device is offline. Write queued locally."
                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                    "QUOTA_EXCEEDED: Firestore project quota limit reached."
                FirebaseFirestoreException.Code.CANCELLED ->
                    "CANCELLED: Firestore operation was cancelled."
                else -> "${e.code.name}: ${e.localizedMessage ?: "Unknown Firestore error"}"
            }
        }
        return e.localizedMessage ?: e.javaClass.simpleName
    }
}
