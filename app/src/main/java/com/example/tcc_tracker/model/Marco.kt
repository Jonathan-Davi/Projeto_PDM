package com.example.tcc_tracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabela_marcos")
data class Marco(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idProjeto: Int,
    val tituloMarco: String,
    val descricao: String,
    val dataPrevista: String,
    val status: String = "Pendente"
)