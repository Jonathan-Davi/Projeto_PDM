package com.example.tcc_tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcc_tracker.model.Marco
import com.example.tcc_tracker.model.Projeto
import com.example.tcc_tracker.viewmodel.TccViewModel
import java.text.SimpleDateFormat
import java.util.*

// -------------------------------------------------------------------
// Funções auxiliares de data
// -------------------------------------------------------------------

private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

/** Converte "dd/MM/yyyy" para um Calendar zerado no início do dia, ou null se inválido. */
private fun parseData(data: String): Calendar? {
    if (data.isBlank()) return null
    return try {
        sdf.isLenient = false
        val date = sdf.parse(data) ?: return null
        Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
    } catch (e: Exception) { null }
}

/** Retorna "dd/MM/yyyy" de um Calendar. */
private fun Calendar.toDateString(): String = sdf.format(time)

/** Retorna o nome do mês em português. */
private fun nomeMes(mes: Int): String = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)[mes]

// -------------------------------------------------------------------
// Modelo interno de evento no calendário
// -------------------------------------------------------------------

private enum class TipoEvento { ENTREGA_PROJETO, MARCO }

private data class EventoCalendario(
    val titulo: String,
    val subtitulo: String,
    val tipo: TipoEvento,
    val status: String,
    val atrasado: Boolean,
    val concluido: Boolean = false
)

// Cor do ponto/badge por tipo e status
private fun corEvento(tipo: TipoEvento, status: String, atrasado: Boolean, concluido: Boolean = false): Color = when {
    tipo == TipoEvento.ENTREGA_PROJETO && concluido -> Color(0xFF34D399)  // ← verde se concluído
    tipo == TipoEvento.ENTREGA_PROJETO              -> Color(0xFFFBBF24)  // ← amarelo se ativo
    atrasado                                        -> Color(0xFFF87171)  // vermelho
    status == "Concluído"                           -> Color(0xFF34D399)  // verde
    status == "Em Andamento"                        -> Color(0xFFA67CF5)  // roxo
    else                                            -> Color(0xFFFB923C)  // laranja
}

// -------------------------------------------------------------------
// Composable principal
// -------------------------------------------------------------------

@Composable
fun CalendarScreen(viewModel: TccViewModel) {
    val projetos by viewModel.todosProjetos.collectAsState()
    val marcos   by viewModel.todosMarcos.collectAsState()

    // Mês/ano sendo exibido
    val hoje = remember { Calendar.getInstance() }
    var anoAtual by remember { mutableStateOf(hoje.get(Calendar.YEAR)) }
    var mesAtual by remember { mutableStateOf(hoje.get(Calendar.MONTH)) }

    // Dia selecionado (null = nenhum)
    var diaSelecionado by remember { mutableStateOf<String?>(null) }

    // Monta o mapa dia → lista de eventos
    val eventosPorDia: Map<String, List<EventoCalendario>> = remember(projetos, marcos) {
        val mapa = mutableMapOf<String, MutableList<EventoCalendario>>()

        // Entregas finais dos projetos
        projetos.forEach { projeto ->
            val cal = parseData(projeto.dataEntregaFinal) ?: return@forEach
            val chave = cal.toDateString()
            mapa.getOrPut(chave) { mutableListOf() }.add(
                EventoCalendario(
                    titulo    = if (projeto.concluido) "Entrega: ${projeto.tema}"   // ← ícone muda
                    else                  "Entrega: ${projeto.tema}",
                    subtitulo = if (projeto.concluido) "Projeto Concluído" else "Projeto", // ← subtítulo muda
                    tipo      = TipoEvento.ENTREGA_PROJETO,
                    status    = "",
                    atrasado  = false,
                    concluido = projeto.concluido  // ← NOVO: passa o valor real
                )
            )
        }


        // Marcos
        marcos.forEach { marco ->
            val cal = parseData(marco.dataPrevista) ?: return@forEach
            val chave = cal.toDateString()
            val atrasado = checarAtraso(marco.dataPrevista, marco.status)
            // Encontra o nome do projeto para exibir no subtítulo
            val nomeProjeto = projetos.firstOrNull { it.id == marco.idProjeto }?.tema ?: "Projeto"
            mapa.getOrPut(chave) { mutableListOf() }.add(
                EventoCalendario(
                    titulo    = marco.tituloMarco,
                    subtitulo = nomeProjeto,
                    tipo      = TipoEvento.MARCO,
                    status    = marco.status,
                    atrasado  = atrasado
                )
            )
        }
        mapa
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            "Calendário",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        // Mensagem descritiva
        Text(
            "Aqui você pode acompanhar visualmente as suas entregas!",
            fontSize = 13.sp,
            color = TextMutedColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        // Grade do calendário
        CalendarGrid(
            ano             = anoAtual,
            mes             = mesAtual,
            hoje            = hoje,
            eventosPorDia   = eventosPorDia,
            diaSelecionado  = diaSelecionado,
            onDiaClick      = { diaSelecionado = if (diaSelecionado == it) null else it },
            onMesAnterior   = {
                if (mesAtual == 0) { mesAtual = 11; anoAtual-- }
                else mesAtual--
                diaSelecionado = null
            },
            onProximoMes    = {
                if (mesAtual == 11) { mesAtual = 0; anoAtual++ }
                else mesAtual++
                diaSelecionado = null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Legenda
        Legenda()

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de eventos do dia selecionado
        val eventosHoje = diaSelecionado?.let { eventosPorDia[it] } ?: emptyList()
        if (diaSelecionado != null) {
            ListaEventosDia(dia = diaSelecionado!!, eventos = eventosHoje)
        }
    }
}

// -------------------------------------------------------------------
// Grade mensal
// -------------------------------------------------------------------

@Composable
private fun CalendarGrid(
    ano: Int,
    mes: Int,
    hoje: Calendar,
    eventosPorDia: Map<String, List<EventoCalendario>>,
    diaSelecionado: String?,
    onDiaClick: (String) -> Unit,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit
) {
    val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    // Primeiro dia do mês e total de dias
    val primeiroDia = Calendar.getInstance().apply {
        set(Calendar.YEAR, ano); set(Calendar.MONTH, mes); set(Calendar.DAY_OF_MONTH, 1)
    }
    val diaDaSemanaInicio = primeiroDia.get(Calendar.DAY_OF_WEEK) - 1  // 0=Dom … 6=Sáb
    val totalDias = primeiroDia.getActualMaximum(Calendar.DAY_OF_MONTH)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape  = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabeçalho com navegação de mês
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMesAnterior) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Mês anterior", tint = TextColor)
                }
                Text(
                    "${nomeMes(mes)} $ano",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
                IconButton(onClick = onProximoMes) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Próximo mês", tint = TextColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linha dos dias da semana
            Row(modifier = Modifier.fillMaxWidth()) {
                diasSemana.forEach { ds ->
                    Text(
                        ds,
                        modifier    = Modifier.weight(1f),
                        textAlign   = TextAlign.Center,
                        fontSize    = 11.sp,
                        fontWeight  = FontWeight.Bold,
                        color       = TextMutedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Células dos dias
            val totalCelulas = diaDaSemanaInicio + totalDias
            val linhas = (totalCelulas + 6) / 7

            for (linha in 0 until linhas) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val celula = linha * 7 + col
                        val dia    = celula - diaDaSemanaInicio + 1

                        if (dia < 1 || dia > totalDias) {
                            // Célula vazia
                            Box(modifier = Modifier.weight(1f).height(44.dp))
                        } else {
                            val calDia = Calendar.getInstance().apply {
                                set(Calendar.YEAR, ano); set(Calendar.MONTH, mes)
                                set(Calendar.DAY_OF_MONTH, dia)
                            }
                            val chave = calDia.toDateString()
                            val ehHoje = (
                                    dia  == hoje.get(Calendar.DAY_OF_MONTH) &&
                                            mes  == hoje.get(Calendar.MONTH) &&
                                            ano  == hoje.get(Calendar.YEAR)
                                    )
                            val ehSelecionado  = chave == diaSelecionado
                            val eventosNoDia   = eventosPorDia[chave] ?: emptyList()
                            val temEntrega     = eventosNoDia.any { it.tipo == TipoEvento.ENTREGA_PROJETO }

                            DiaCell(
                                dia           = dia,
                                ehHoje        = ehHoje,
                                ehSelecionado = ehSelecionado,
                                temEntrega    = temEntrega,
                                eventos       = eventosNoDia,
                                modifier      = Modifier.weight(1f),
                                onClick       = { onDiaClick(chave) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// Célula de um único dia
// -------------------------------------------------------------------

@Composable
private fun DiaCell(
    dia: Int,
    ehHoje: Boolean,
    ehSelecionado: Boolean,
    temEntrega: Boolean,
    eventos: List<EventoCalendario>,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bgMod = when {
        ehSelecionado -> Modifier.background(
            Brush.linearGradient(listOf(AccentColor, Accent2Color)), CircleShape
        )
        ehHoje        -> Modifier.background(TealColor.copy(alpha = 0.25f), CircleShape)
        temEntrega    -> Modifier.background(YellowColor.copy(alpha = 0.15f), CircleShape)
        else          -> Modifier
    }

    val borderMod = if (temEntrega && !ehSelecionado)
        Modifier.border(1.dp, YellowColor.copy(alpha = 0.6f), CircleShape)
    else Modifier

    Column(
        modifier  = modifier.height(44.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .then(bgMod)
                .then(borderMod),
            contentAlignment = Alignment.Center
        ) {
            Text(
                dia.toString(),
                fontSize   = 13.sp,
                fontWeight = if (ehHoje || ehSelecionado) FontWeight.ExtraBold else FontWeight.Normal,
                color      = when {
                    ehSelecionado -> Color.White
                    ehHoje        -> TealColor
                    else          -> TextColor
                }
            )
        }

        // Pontos coloridos dos eventos (máximo 3 pontos)
        if (eventos.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                eventos.take(3).forEach { ev ->
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(corEvento(ev.tipo, ev.status, ev.atrasado, ev.concluido))
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// Legenda de cores
// -------------------------------------------------------------------

@Composable
private fun Legenda() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ItemLegenda(cor = Color(0xFFFBBF24), label = "Entrega")
            ItemLegenda(cor = Color(0xFF34D399), label = "Concluído")
            ItemLegenda(cor = Color(0xFFA67CF5), label = "Andamento")
            ItemLegenda(cor = Color(0xFFFB923C), label = "Pendente")
            ItemLegenda(cor = Color(0xFFF87171), label = "Atrasado")
        }
    }
}

@Composable
private fun ItemLegenda(cor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(cor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = TextMutedColor)
    }
}

// -------------------------------------------------------------------
// Lista de eventos do dia selecionado
// -------------------------------------------------------------------

@Composable
private fun ListaEventosDia(dia: String, eventos: List<EventoCalendario>) {
    Text(
        "Eventos em $dia",
        fontSize   = 14.sp,
        fontWeight = FontWeight.Bold,
        color      = TextColor,
        modifier   = Modifier.padding(bottom = 8.dp)
    )

    if (eventos.isEmpty()) {
        Text(
            "Nenhum prazo neste dia.",
            color    = TextMutedColor,
            fontSize = 13.sp
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(eventos) { evento ->
            val cor = corEvento(evento.tipo, evento.status, evento.atrasado, evento.concluido)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardColor)
                    .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(cor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        evento.titulo,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextColor
                    )
                    Text(
                        buildString {
                            append(evento.subtitulo)
                            if (evento.tipo == TipoEvento.MARCO) {
                                append(" • ")
                                append(if (evento.atrasado) "Atrasado" else evento.status)
                            }
                        },
                        fontSize = 11.sp,
                        color    = cor
                    )
                }
            }
        }
    }
}