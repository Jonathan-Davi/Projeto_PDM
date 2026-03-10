package com.example.tcc_tracker.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcc_tracker.model.Marco
import com.example.tcc_tracker.model.Projeto
import com.example.tcc_tracker.viewmodel.TccViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val BgColor = Color(0xFF0F0F1A)
val CardColor = Color(0xFF22223B)
val Card2Color = Color(0xFF2A2A45)
val AccentColor = Color(0xFF7C5CBF)
val Accent2Color = Color(0xFFA67CF5)
val TealColor = Color(0xFF38BDF8)
val GreenColor = Color(0xFF34D399)
val OrangeColor = Color(0xFFFB923C)
val YellowColor = Color(0xFFFBBF24)
val RedColor = Color(0xFFF87171)
val TextColor = Color(0xFFF0F0FF)
val TextMutedColor = Color(0xFF9090B0)

fun checarAtraso(data: String, status: String): Boolean {
    if (status == "Concluído" || data.isBlank()) return false
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val dataMarco = sdf.parse(data)
        val hojeSdf = sdf.parse(sdf.format(Date()))
        dataMarco != null && dataMarco.before(hojeSdf)
    } catch (e: Exception) {
        false
    }
}

@Composable
fun HomeScreen(viewModel: TccViewModel) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("TccAppPrefs", Context.MODE_PRIVATE)
    var nomeUsuario by remember { mutableStateOf(sharedPreferences.getString("nome_usuario", "") ?: "") }
    var mostrarDialogNome by remember { mutableStateOf(nomeUsuario.isEmpty()) }

    var projetoSelecionado by remember { mutableStateOf<Projeto?>(null) }
    var mostrarDialogProjeto by remember { mutableStateOf(false) }

    var filtroSelecionado by remember { mutableStateOf("Todos") }

    Scaffold(
        containerColor = BgColor,
        bottomBar = { CustomBottomNav(onAddClick = { mostrarDialogProjeto = true }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
        ) {
            HeaderSection(nome = nomeUsuario, onPerfilClick = { mostrarDialogNome = true })
            Spacer(modifier = Modifier.height(16.dp))

            FilterChips(filtroSelecionado) { novoFiltro -> filtroSelecionado = novoFiltro }

            Spacer(modifier = Modifier.height(22.dp))

            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (projetoSelecionado == null) {
                    ListaDeProjetos(viewModel, onProjetoClick = { projetoSelecionado = it })
                } else {
                    TelaDashboardProjeto(projetoSelecionado!!, viewModel, filtroSelecionado, onVoltar = { projetoSelecionado = null })
                }
            }
        }
    }

    val textConfig = KeyboardOptions(keyboardType = KeyboardType.Text)

    if (mostrarDialogNome) {
        var novoNome by remember { mutableStateOf(nomeUsuario) }
        AlertDialog(
            onDismissRequest = { if (nomeUsuario.isNotEmpty()) mostrarDialogNome = false },
            containerColor = CardColor, title = { Text("Bem-vindo!", color = TextColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(value = novoNome, onValueChange = { novoNome = it }, label = { Text("Qual o seu nome?", color = TextMutedColor) }, keyboardOptions = textConfig, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor), modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (novoNome.isNotBlank()) {
                        sharedPreferences.edit().putString("nome_usuario", novoNome).apply()
                        nomeUsuario = novoNome; mostrarDialogNome = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) { Text("Salvar") }
            }
        )
    }

    if (mostrarDialogProjeto) {
        var tema by remember { mutableStateOf("") }
        var orientador by remember { mutableStateOf("") }
        var data by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogProjeto = false }, containerColor = CardColor, title = { Text("Novo Projeto", color = TextColor) },
            text = {
                Column {
                    OutlinedTextField(value = tema, onValueChange = { tema = it }, label = { Text("Tema", color = TextMutedColor) }, keyboardOptions = textConfig, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor))
                    OutlinedTextField(value = orientador, onValueChange = { orientador = it }, label = { Text("Orientador", color = TextMutedColor) }, keyboardOptions = textConfig, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor))
                    OutlinedTextField(value = data, onValueChange = { data = it }, label = { Text("Prazo (Ex: 15/12/2026)", color = TextMutedColor) }, keyboardOptions = textConfig, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor))
                }
            },
            confirmButton = { Button(onClick = { viewModel.adicionarProjeto(tema, orientador, data); mostrarDialogProjeto = false }, colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) { Text("Salvar") } },
            dismissButton = { TextButton(onClick = { mostrarDialogProjeto = false }) { Text("Cancelar", color = TextMutedColor) } }
        )
    }
}

@Composable
fun TelaDashboardProjeto(projeto: Projeto, viewModel: TccViewModel, filtroSelecionado: String, onVoltar: () -> Unit) {
    val marcosFlow = remember(projeto.id) { viewModel.listarMarcosDoProjeto(projeto.id) }
    val marcos by marcosFlow.collectAsState(initial = emptyList())
    var mostrarDialogNovoMarco by remember { mutableStateOf(false) }

    val marcosFiltrados = marcos.filter { marco ->
        val ehAtrasado = checarAtraso(marco.dataPrevista, marco.status)
        when (filtroSelecionado) {
            "Em Andamento" -> marco.status == "Em Andamento"
            "Concluídos" -> marco.status == "Concluído"
            "Atrasados" -> ehAtrasado
            else -> true
        }
    }

    val total = marcos.size
    val concluidos = marcos.count { it.status == "Concluído" }
    val atrasados = marcos.count { checarAtraso(it.dataPrevista, it.status) }
    val progressoFloat = if (total > 0) concluidos.toFloat() / total else 0f
    val progressoPct = (progressoFloat * 100).toInt()

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp).clickable { onVoltar() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = TealColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voltar aos Projetos", color = TealColor, fontWeight = FontWeight.Bold)
                }
            }
            item { ProjectHeroCard(projeto, progressoFloat, progressoPct) }
            item { Spacer(modifier = Modifier.height(22.dp)) }
            item { SectionTitle("Visão Geral", "") }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(modifier = Modifier.weight(1f), num = total.toString(), lbl = "Marcos", icon = Icons.Filled.Place, color = Accent2Color, bg = AccentColor.copy(alpha = 0.2f))
                    StatCard(modifier = Modifier.weight(1f), num = concluidos.toString(), lbl = "Concluídas", icon = Icons.Filled.CheckCircle, color = GreenColor, bg = GreenColor.copy(alpha = 0.15f))
                    StatCard(modifier = Modifier.weight(1f), num = atrasados.toString(), lbl = "Atrasados", icon = Icons.Filled.Warning, color = RedColor, bg = RedColor.copy(alpha = 0.15f))
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { SectionTitle("Marcos do Projeto", "") }

            if (marcosFiltrados.isEmpty()) {
                item { Text("Nenhuma tarefa encontrada neste filtro.", color = TextMutedColor, modifier = Modifier.padding(bottom = 16.dp)) }
            } else {
                items(marcosFiltrados, key = { it.id }) { marco ->
                    MilestoneCardFuncional(marco, viewModel)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Button(
            onClick = { mostrarDialogNovoMarco = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar Nova Tarefa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    if (mostrarDialogNovoMarco) {
        var titulo by remember { mutableStateOf("") }
        var descricao by remember { mutableStateOf("") }
        var dataPrevista by remember { mutableStateOf("") }
        val textConfig = KeyboardOptions(keyboardType = KeyboardType.Text)

        AlertDialog(
            onDismissRequest = { mostrarDialogNovoMarco = false }, containerColor = CardColor, title = { Text("Novo Marco/Tarefa", color = TextColor) },
            text = {
                Column {
                    OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título", color = TextMutedColor) }, keyboardOptions = textConfig, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor))
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição", color = TextMutedColor) }, keyboardOptions = textConfig, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor))
                    OutlinedTextField(value = dataPrevista, onValueChange = { dataPrevista = it }, label = { Text("Data Prevista (Ex: 19/02/2026)", color = TextMutedColor) }, keyboardOptions = textConfig, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextColor, unfocusedTextColor = TextColor))
                }
            },
            confirmButton = { Button(onClick = { viewModel.adicionarMarco(projeto.id, titulo, descricao, dataPrevista); mostrarDialogNovoMarco = false }, colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) { Text("Adicionar") } },
            dismissButton = { TextButton(onClick = { mostrarDialogNovoMarco = false }) { Text("Cancelar", color = TextMutedColor) } }
        )
    }
}

@Composable
fun MilestoneCardFuncional(marco: Marco, viewModel: TccViewModel) {
    val ehAtrasado = checarAtraso(marco.dataPrevista, marco.status)

    val corStatus = when {
        ehAtrasado -> RedColor
        marco.status == "Concluído" -> GreenColor
        marco.status == "Em Andamento" -> Accent2Color
        else -> OrangeColor
    }

    val statusTexto = if (ehAtrasado) "Atrasado" else marco.status

    val icone = when {
        marco.status == "Concluído" -> Icons.Filled.Check
        marco.status == "Em Andamento" -> Icons.Filled.PlayArrow
        else -> Icons.Filled.List
    }

    val dataExibicao = if (marco.dataPrevista.isNotBlank()) marco.dataPrevista else "Sem data"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { viewModel.alternarStatusMarco(marco) },
        colors = CardDefaults.cardColors(containerColor = CardColor), shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(corStatus.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(icone, contentDescription = null, tint = corStatus, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(marco.tituloMarco, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextColor)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = TextMutedColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(dataExibicao, fontSize = 11.sp, color = TextMutedColor)
                        }
                        if (marco.descricao.isNotBlank()) {
                            Text(marco.descricao, fontSize = 12.sp, color = TextMutedColor, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(statusTexto, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = corStatus, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(corStatus.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    IconButton(
                        onClick = { viewModel.deletarMarco(marco) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Deletar", tint = RedColor.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChips(filtroAtual: String, onFiltroChange: (String) -> Unit) {
    val chips = listOf("Todos", "Em Andamento", "Concluídos", "Atrasados")
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chips.forEach { texto ->
            val isSelected = texto == filtroAtual
            val fundoModifier = if (isSelected) Modifier.background(Brush.linearGradient(listOf(AccentColor, Accent2Color))) else Modifier.background(CardColor)
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).then(fundoModifier).border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)).clickable { onFiltroChange(texto) }.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text(texto, color = if (isSelected) Color.White else TextMutedColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ListaDeProjetos(viewModel: TccViewModel, onProjetoClick: (Projeto) -> Unit) {
    val projetos by viewModel.todosProjetos.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        SectionTitle("Seus Projetos", "Ver todos")
        if (projetos.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Nenhum projeto cadastrado.", color = TextMutedColor) }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(projetos, key = { it.id }) { projeto ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onProjetoClick(projeto) }, colors = CardDefaults.cardColors(containerColor = CardColor), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AccentColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Star, contentDescription = null, tint = Accent2Color, modifier = Modifier.size(18.dp)) }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column { Text(projeto.tema, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextColor); Text("Entrega: ${projeto.dataEntregaFinal}", fontSize = 12.sp, color = TextMutedColor) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectHeroCard(projeto: Projeto, progressoFloat: Float, progressoPct: Int) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFF3D1F8D), Color(0xFF1E3A6E), Color(0xFF0F4C75)))).padding(22.dp)) {
        Column {
            Row(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Star, contentDescription = null, tint = YellowColor, modifier = Modifier.size(12.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("PROJETO ATIVO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            Spacer(modifier = Modifier.height(10.dp))
            Text(projeto.tema, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 22.sp); Text("Orientador: ${projeto.orientador}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Progresso Geral", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f)); Text("$progressoPct%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
            LinearProgressIndicator(progress = { progressoFloat }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)), color = TealColor, trackColor = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.DateRange, contentDescription = null, tint = OrangeColor, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("Entrega Final: ${projeto.dataEntregaFinal}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f)) }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, num: String, lbl: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bg: Color) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(CardColor).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp)).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(bg), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) }
        Spacer(modifier = Modifier.height(8.dp)); Text(num, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color); Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextMutedColor)
    }
}

@Composable
fun HeaderSection(nome: String, onPerfilClick: () -> Unit) {
    val iniciais = remember(nome) { if (nome.isNotBlank()) { val partes = nome.trim().split(" "); if (partes.size > 1) "${partes[0].first()}${partes[1].first()}".uppercase() else nome.take(2).uppercase() } else "..." }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text("Bem Vindo,", fontSize = 13.sp, color = TextMutedColor); Text(if (nome.isEmpty()) "Visitante" else nome, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextColor) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CardColor), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Notifications, contentDescription = "Avisos", tint = TextMutedColor, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(AccentColor, TealColor))).border(3.dp, AccentColor.copy(alpha = 0.3f), CircleShape).clickable { onPerfilClick() }, contentAlignment = Alignment.Center) { Text(iniciais, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp) }
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String, iconTitle: androidx.compose.ui.graphics.vector.ImageVector? = null, onActionClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) { if (iconTitle != null) Icon(iconTitle, contentDescription = null, tint = TealColor, modifier = Modifier.size(14.dp).padding(end = 6.dp)); Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextColor) }
        Text(action, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextMutedColor, modifier = Modifier.clickable { onActionClick?.invoke() })
    }
}

@Composable
fun CustomBottomNav(onAddClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(BgColor.copy(alpha = 0.95f)).border(1.dp, Color.White.copy(alpha = 0.05f)).padding(vertical = 12.dp, horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Home, contentDescription = "Home", tint = Accent2Color, modifier = Modifier.size(24.dp))
            Icon(Icons.Filled.DateRange, contentDescription = "Agenda", tint = TextMutedColor, modifier = Modifier.size(24.dp))
            Box(modifier = Modifier.offset(y = (-15).dp).size(56.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(AccentColor, Accent2Color))).clickable { onAddClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(28.dp)) }
            Icon(Icons.Filled.Share, contentDescription = "Refs", tint = TextMutedColor, modifier = Modifier.size(24.dp))
            Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = TextMutedColor, modifier = Modifier.size(24.dp))
        }
    }
}