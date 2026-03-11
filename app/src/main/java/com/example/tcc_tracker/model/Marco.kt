package com.example.tcc_tracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabela_marcos",
    foreignKeys = [
        ForeignKey(
            entity        = Projeto::class,
            parentColumns = ["id"],
            childColumns  = ["idProjeto"],
            onDelete      = ForeignKey.CASCADE  // ← ao deletar o projeto, deleta os marcos automaticamente
        )
    ],
    indices = [Index("idProjeto")]  // ← necessário para performance com ForeignKey
)
data class Marco(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idProjeto: Int,
    val tituloMarco: String,
    val descricao: String,
    val dataPrevista: String,
    val status: String = "Pendente"
)