package com.jpd.methodcards.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.jpd.MethodProto
import com.jpd.methodcards.data.library.toDomain
import com.jpd.methodcards.domain.MethodFrequency
import com.jpd.methodcards.domain.MethodSelection
import com.jpd.methodcards.domain.MethodWithCalls
import com.jpd.methodcards.domain.PlaceNotation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomMethodDao(
    private val dao: RoomSqlMethodDao,
) : MethodDao {
    override fun getMethodsByStage(stage: Int): Flow<List<MethodSelection>> {
        return dao.getMethodsByStage(stage)
    }

    override fun getSelectedMethods(): Flow<List<MethodWithCalls>> {
        return dao.getSelectedMethods().map { it.map { m -> m.toDomain() } }
    }

    override fun getMethod(name: String): Flow<MethodWithCalls?> {
        return dao.getMethod(name).map { it?.toDomain() }
    }

    override suspend fun searchByPlaceNotation(pn: List<PlaceNotation>): List<MethodWithCalls> {
        return dao.searchByPlaceNotation(pn.map { it.asString() }).map { it.toDomain() }
    }

    override suspend fun toggleMethodSelected(name: String) {
        dao.toggleMethodSelected(name)
    }

    override suspend fun toggleMethodEnabledForMultiMethod(name: String) {
        dao.toggleMethodEnabledForMultiMethod(name)
    }

    override suspend fun deselectAllMethodsForMultiMethod() {
        dao.deselectAllMethodsForMultiMethod()
    }

    override suspend fun setMultiMethodFrequency(name: String, frequency: MethodFrequency) {
        dao.setMultiMethodFrequency(name, frequency)
    }

    override suspend fun setBluelineMethod(name: String) {
        dao.setBluelineMethod(name)
    }

    override suspend fun incrementMethodStatistics(methodName: String, stage: Int, lead: Int, error: Boolean) {
        dao.incrementMethodStatistics(methodName, stage, lead, error)
    }

    override suspend fun addMethod(method: MethodWithCalls) {
        val methodEntity = MethodEntity(
            name = method.name,
            placeNotation = method.placeNotation,
            stage = method.stage,
            ruleoffsFrom = method.ruleoffsFrom,
            ruleoffsEvery = method.ruleoffsEvery,
            magic = 0,
            classification = method.classification,
        )
        val calls = method.calls.map { call ->
            CallEntity(
                methodName = method.name,
                name = call.name,
                symbol = call.symbol,
                notation = call.notation,
                from = call.from,
                every = call.every,
            )
        }
        dao.insert(listOf(methodEntity), calls)
    }

    override suspend fun insert(methods: List<MethodProto>) {
        val methodEntities = ArrayList<MethodEntity>(methods.size)
        val callEntities = ArrayList<CallEntity>(methods.size * 2)
        methods.forEach { method ->
            methodEntities.add(
                MethodEntity(
                    name = method.name,
                    placeNotation = PlaceNotation(method.notation),
                    stage = method.stage,
                    ruleoffsFrom = method.ruleoffsFrom,
                    ruleoffsEvery = method.ruleoffsEvery,
                    magic = method.magic,
                    classification = method.classification.toDomain(),
                ),
            )
            method.calls.forEach { call ->
                callEntities.add(
                    CallEntity(
                        methodName = method.name,
                        name = call.name,
                        symbol = call.symbol,
                        notation = PlaceNotation(call.notation),
                        from = call.from,
                        every = call.every(method.lengthOfLead),
                    ),
                )
            }
        }
        dao.insert(methodEntities, callEntities)
    }
}

@Dao
interface RoomSqlMethodDao {
    @Query("""
        SELECT m.name, m.placeNotation, s.selected
        FROM MethodEntity AS m
        INNER JOIN SelectionEntity AS s on s.selectionName = m.name
        WHERE m.stage = :stage
        ORDER BY s.selected DESC, m.magic
    """)
    fun getMethodsByStage(stage: Int): Flow<List<MethodSelection>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *
        FROM MethodEntity AS m
        INNER JOIN SelectionEntity AS s on s.selectionName = m.name
        WHERE s.selected = true
        ORDER BY m.magic
    """,)
    fun getSelectedMethods(): Flow<List<MethodWithCallsEntity>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *
        FROM MethodEntity AS m
        INNER JOIN SelectionEntity AS s on s.selectionName = m.name
        WHERE m.name = :name
    """)
    fun getMethod(name: String): Flow<MethodWithCallsEntity?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *
        FROM MethodEntity AS m
        INNER JOIN SelectionEntity AS s on s.selectionName = m.name
        WHERE m.placeNotation IN (:pn)
    """)
    suspend fun searchByPlaceNotation(pn: List<String>): List<MethodWithCallsEntity>

    @Query("UPDATE SelectionEntity SET selected = NOT selected WHERE selectionName = :name")
    suspend fun toggleMethodSelected(name: String)

    @Query("UPDATE SelectionEntity SET enabledForMultiMethod = NOT enabledForMultiMethod WHERE selectionName = :name")
    suspend fun toggleMethodEnabledForMultiMethod(name: String)

    @Query("UPDATE SelectionEntity SET enabledForMultiMethod = false")
    suspend fun deselectAllMethodsForMultiMethod()

    @Query("UPDATE SelectionEntity SET multiMethodFrequency = :frequency WHERE selectionName = :name")
    suspend fun setMultiMethodFrequency(name: String, frequency: MethodFrequency)

    @Query("UPDATE SelectionEntity SET enabledForBlueline = CASE WHEN selectionName = :name THEN true ELSE false END")
    suspend fun setBluelineMethod(name: String)

    suspend fun insert(methods: Iterable<MethodEntity>, calls: Iterable<CallEntity>) {
        coroutineScope {
            launch {
                insertMethods(methods)
            }
            launch {
                insertCalls(calls)
            }
            insertSelections(
                methods.map {
                    SelectionEntity(it.name, it.stage, false, false, MethodFrequency.Regular, false)
                },
            )
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethods(methods: Iterable<MethodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: Iterable<CallEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSelections(selections: Iterable<SelectionEntity>)

    @Transaction
    suspend fun incrementMethodStatistics(
        methodName: String,
        stage: Int,
        lead: Int,
        error: Boolean
    ) {
        val methodStatistics = getMethodStatistics(methodName) ?: MethodStatisticsEntity(methodName, List(stage) { 0 }, List(stage) {0})
        val leadsRung = methodStatistics.leadsRung.toMutableList()
        val leadsRungWithError = methodStatistics.leadsRungWithError.toMutableList()
        leadsRung[lead - 1] = leadsRung[lead - 1] + 1
        if (error) {
            leadsRungWithError[lead - 1] = leadsRungWithError[lead - 1] + 1
        }
        insertMethodStatistics(MethodStatisticsEntity(methodName, leadsRung, leadsRungWithError))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethodStatistics(methodStatistics: MethodStatisticsEntity)

    @Query("SELECT * FROM MethodStatisticsEntity WHERE methodName = :methodName")
    suspend fun getMethodStatistics(methodName: String): MethodStatisticsEntity?
}
