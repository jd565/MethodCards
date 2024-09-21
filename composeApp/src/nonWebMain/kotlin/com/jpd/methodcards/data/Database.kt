package com.jpd.methodcards.data

import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.jpd.methodcards.domain.CallDetails
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation

@Database(
    entities = [MethodEntity::class, CallEntity::class, SelectionEntity::class, MethodStatisticsEntity::class],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 4, to = 5, spec = AppAutoMigration::class),
        AutoMigration(from = 5, to = 6),
    ],
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun getMethodDao(): RoomSqlMethodDao

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

interface DB {
    fun clearAllTables() {}
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

@Entity
data class MethodEntity(
    @PrimaryKey val name: String,
    val placeNotation: PlaceNotation,
    val stage: Int,
    val ruleoffsEvery: Int,
    val ruleoffsFrom: Int,
    val magic: Int,
    @ColumnInfo(defaultValue = "")
    val classification: String,
)

@Entity(primaryKeys = ["methodName", "name"])
data class CallEntity(
    val methodName: String,
    val name: String,
    val symbol: String,
    val notation: PlaceNotation,
    val from: Int,
    val every: Int,
    val cover: Int,
) {
    fun toDomain(): CallDetails = CallDetails(
        methodName,
        name,
        symbol,
        notation,
        from,
        every,
        cover,
    )
}

@Entity
data class SelectionEntity(
    @PrimaryKey val selectionName: String,
    val selectionStage: Int,
    val selected: Boolean,
    val enabledForMultiMethod: Boolean,
    val multiMethodFrequency: MethodFrequency,
    val enabledForBlueline: Boolean,
)

data class MethodWithCallsEntity(
    val name: String,
    private val placeNotation: PlaceNotation,
    val stage: Int,
    val ruleoffsEvery: Int,
    val ruleoffsFrom: Int,
    val classification: String,
    @Relation(parentColumn = "name", entityColumn = "methodName")
    val calls: List<CallEntity>,
    val enabledForMultiMethod: Boolean,
    val multiMethodFrequency: MethodFrequency,
    val enabledForBlueline: Boolean,
) {
    fun toDomain(): MethodWithCalls = MethodWithCalls(
        name,
        placeNotation,
        stage,
        ruleoffsEvery,
        ruleoffsFrom,
        classification,
        calls.map { it.toDomain() },
        enabledForMultiMethod,
        multiMethodFrequency,
        enabledForBlueline,
    )
}

@Entity
@TypeConverters(ListIntTypeConverter::class)
data class MethodStatisticsEntity(
    @PrimaryKey val methodName: String,
    val leadsRung: List<Int>,
    val leadsRungWithError: List<Int>,
)

class ListIntTypeConverter {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromList(list: List<Int>): String {
        return list.joinToString(",")
    }
}

@RenameColumn(tableName = "SelectionEntity", fromColumnName = "name", toColumnName = "selectionName")
@RenameColumn(tableName = "SelectionEntity", fromColumnName = "stage", toColumnName = "selectionStage")
class AppAutoMigration : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL("DELETE from MethodEntity")
    }
}
