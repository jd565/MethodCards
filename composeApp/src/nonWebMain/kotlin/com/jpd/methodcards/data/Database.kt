package com.jpd.methodcards.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.jpd.methodcards.domain.CallDetails
import com.jpd.methodcards.domain.MethodClassification
import com.jpd.methodcards.domain.MethodCollection
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation

@Database(
    entities = [MethodEntity::class, CallEntity::class, SelectionEntity::class, MethodStatisticsEntity::class, MethodCollectionEntity::class],
    version = 10,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(PlaceNotationTypeConverter::class)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun getMethodDao(): RoomSqlMethodDao

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

interface DB {
    fun clearAllTables() {}
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

@Entity
data class MethodEntity(
    @PrimaryKey val name: String,
    val placeNotation: PlaceNotation,
    val stage: Int,
    val ruleoffsEvery: Int,
    val ruleoffsFrom: Int,
    val magic: Int,
    val classification: MethodClassification,
    val customMethod: Boolean = false,
)

@Entity(primaryKeys = ["methodName", "name"])
data class CallEntity(
    val methodName: String,
    val name: String,
    val symbol: String,
    val notation: PlaceNotation,
    val from: Int,
    val every: Int,
) {
    fun toDomain(): CallDetails = CallDetails(
        methodName,
        name,
        symbol,
        notation,
        from,
        every,
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
    val classification: MethodClassification,
    @Relation(parentColumn = "name", entityColumn = "methodName")
    val calls: List<CallEntity>,
    val enabledForMultiMethod: Boolean,
    val multiMethodFrequency: MethodFrequency,
    val enabledForBlueline: Boolean,
    val customMethod: Boolean = false,
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
        customMethod,
    )
}

@Entity
@TypeConverters(ListIntTypeConverter::class)
data class MethodStatisticsEntity(
    @PrimaryKey val methodName: String,
    val leadsRung: List<Int>,
    val leadsRungWithError: List<Int>,
)

@Entity
@TypeConverters(ListStringTypeConverter::class)
data class MethodCollectionEntity(
    @PrimaryKey val collectionName: String,
    val methods: List<String>,
) {
    fun toDomain(): MethodCollection = MethodCollection(
        collectionName,
        methods,
    )
}

class ListStringTypeConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}

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

class PlaceNotationTypeConverter {
    @TypeConverter
    fun fromPlaceNotation(value: PlaceNotation): String {
        return value.asString()
    }

    @TypeConverter
    fun toPlaceNotation(value: String): PlaceNotation {
        return PlaceNotation(value)
    }
}
