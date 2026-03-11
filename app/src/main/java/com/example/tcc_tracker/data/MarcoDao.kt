package com.example.tcc_tracker.data

import androidx.room.*
import com.example.tcc_tracker.model.Marco
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcoDao {
    @Query("SELECT * FROM tabela_marcos WHERE idProjeto = :idProjeto")
    fun listarPorProjeto(idProjeto: Int): Flow<List<Marco>>

    @Query("SELECT * FROM tabela_marcos")
    fun listarTodos(): Flow<List<Marco>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(marco: Marco)

    @Update
    suspend fun atualizar(marco: Marco)

    @Delete
    suspend fun deletar(marco: Marco)
}