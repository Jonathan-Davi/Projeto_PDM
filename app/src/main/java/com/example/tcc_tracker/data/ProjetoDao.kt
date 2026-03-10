package com.example.tcc_tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tcc_tracker.model.Projeto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjetoDao {
    @Insert
    suspend fun inserir(projeto: Projeto)

    @Query("SELECT * FROM tabela_projetos")
    fun listarTodos(): Flow<List<Projeto>>

    @Update
    suspend fun atualizar(projeto: Projeto)

    @Delete
    suspend fun deletar(projeto: Projeto)
}