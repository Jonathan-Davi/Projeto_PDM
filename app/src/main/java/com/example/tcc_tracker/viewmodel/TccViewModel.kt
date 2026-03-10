package com.example.tcc_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tcc_tracker.data.MarcoDao
import com.example.tcc_tracker.data.ProjetoDao
import com.example.tcc_tracker.model.Marco
import com.example.tcc_tracker.model.Projeto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TccViewModel(private val projetoDao: ProjetoDao, private val marcoDao: MarcoDao) : ViewModel() {

    val todosProjetos: StateFlow<List<Projeto>> = projetoDao.listarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun adicionarProjeto(tema: String, orientador: String, dataFinal: String) {
        viewModelScope.launch { projetoDao.inserir(Projeto(tema = tema, orientador = orientador, dataEntregaFinal = dataFinal)) }
    }

    fun listarMarcosDoProjeto(idProjeto: Int): StateFlow<List<Marco>> {
        return marcoDao.listarPorProjeto(idProjeto).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun adicionarMarco(idProjeto: Int, titulo: String, descricao: String, dataPrevista: String) {
        viewModelScope.launch { marcoDao.inserir(Marco(idProjeto = idProjeto, tituloMarco = titulo, descricao = descricao, dataPrevista = dataPrevista)) }
    }

    fun alternarStatusMarco(marco: Marco) {
        val novoStatus = when (marco.status) {
            "Pendente" -> "Em Andamento"
            "Em Andamento" -> "Concluído"
            else -> "Pendente"
        }
        viewModelScope.launch { marcoDao.atualizar(marco.copy(status = novoStatus)) }
    }

    fun deletarMarco(marco: Marco) {
        viewModelScope.launch { marcoDao.deletar(marco) }
    }
}

class TccViewModelFactory(private val projetoDao: ProjetoDao, private val marcoDao: MarcoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TccViewModel::class.java)) return TccViewModel(projetoDao, marcoDao) as T
        throw IllegalArgumentException("Classe Desconhecida")
    }
}