package com.example.tcc_tracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabela_projetos")
data class Projeto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tema: String,
    val orientador: String,
    val dataEntregaFinal: String
)