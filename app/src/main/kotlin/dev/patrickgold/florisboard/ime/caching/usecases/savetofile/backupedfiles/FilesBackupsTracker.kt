package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.backupedfiles

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "uploaded_files")
internal data class UploadedFile(
    @PrimaryKey val filename: String,
)

@Dao
internal interface UploadedFileDao {
    @Query("SELECT COUNT(*) FROM uploaded_files WHERE filename = :filename")
    suspend fun exists(filename: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(file: UploadedFile)
}

@Database(entities = [UploadedFile::class], version = 1)
internal abstract class FilesBackupsDatabase : RoomDatabase() {
    abstract fun dao(): UploadedFileDao

    companion object {
        @Volatile
        private var instance: FilesBackupsDatabase? = null

        fun get(context: Context): FilesBackupsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FilesBackupsDatabase::class.java,
                    "files_backups_db",
                ).build().also { instance = it }
            }
    }
}

internal object FilesBackupsTracker {
    suspend fun isAlreadyUploaded(context: Context, filename: String): Boolean =
        FilesBackupsDatabase.get(context).dao().exists(filename) > 0

    suspend fun markAsUploaded(context: Context, filename: String) {
        FilesBackupsDatabase.get(context).dao().insert(UploadedFile(filename))
    }
}
