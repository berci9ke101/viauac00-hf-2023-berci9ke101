package hu.kszi2.android.schpincer.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "openings")
data class OpeningItem(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "circlename") var circleName: String,
    @ColumnInfo(name = "nextopeningdate") var nextOpeningDate: Long,
    @ColumnInfo(name = "outofstock") var outOfStock: Boolean,
) {
    override operator fun equals(other: Any?): Boolean {
        if (other !is OpeningItem) {
            return false
        }
        return (other.circleName == this.circleName) && (other.nextOpeningDate == this.nextOpeningDate)
    }
}

@Dao
interface OpeningItemDao {
    @Query("SELECT * FROM openings")
    fun getAll(): List<OpeningItem>

    @Insert
    fun insert(openingItem: OpeningItem): Long

    @Update
    fun update(openingItem: OpeningItem)

    @Delete
    fun deleteItem(openingItem: OpeningItem)

    @Query("DELETE FROM openings")
    fun clearAll()
}

@Database(entities = [OpeningItem::class], version = 1)
abstract class OpeningListDatabase : RoomDatabase() {
    abstract fun openingItemDao(): OpeningItemDao

    companion object {
        fun getDatabase(applicationContext: Context): OpeningListDatabase {
            return Room.databaseBuilder(
                applicationContext,
                OpeningListDatabase::class.java,
                "opening-list"
            ).build()
        }
    }
}