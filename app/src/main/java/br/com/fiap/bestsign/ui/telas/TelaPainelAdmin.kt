package br.com.fiap.bestsign.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.Documento
import br.com.fiap.bestsign.modelo.StatusDocumento
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.EtiquetaStatusDocumento
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario

@Composable
fun TelaPainelAdmin(
    usuarioLogado: Usuario,
    documentoRepositorio: DocumentoRepositorio,
    aoIniciarNovoDocumento: () -> Unit,
    aoAbrirGerenciarSignatarios: () -> Unit,
    aoAbrirDetalhesDocumento: (Long) -> Unit,
    aoSair: () -> Unit
) {
    var listaDocumentos by remember { mutableStateOf<List<Documento>>(emptyList()) }
    var filtroStatus by remember { mutableStateOf<StatusDocumento?>(null) }

    LaunchedEffect(Unit) {
        listaDocumentos = documentoRepositorio.listarDocumentosParaAdmin()
    }

    val documentosFiltrados = remember(listaDocumentos, filtroStatus) {
        if (filtroStatus == null) {
            listaDocumentos
        } else {
            listaDocumentos.filter { it.status == filtroStatus }
        }
    }

    val totalPendentes = listaDocumentos.count { it.status == StatusDocumento.Pendente }
    val totalParciais = listaDocumentos.count { it.status == StatusDocumento.ParcialmenteAssinado }
    val totalConcluidos = listaDocumentos.count { it.status == StatusDocumento.Concluido }

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "Painel do Administrador",
                acoes = {
                    TextButton(onClick = aoSair) {
                        Text("Sair", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { espacamentoInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CinzaFundoTela)
                .padding(espacamentoInterno)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Olá, ${usuarioLogado.nomeCompleto}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulPrimario
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResumoStatusCard(
                            titulo = "Pendentes",
                            quantidade = totalPendentes,
                            modifier = Modifier.weight(1f)
                        )
                        ResumoStatusCard(
                            titulo = "Parciais",
                            quantidade = totalParciais,
                            modifier = Modifier.weight(1f)
                        )
                        ResumoStatusCard(
                            titulo = "Concluídos",
                            quantidade = totalConcluidos,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = aoIniciarNovoDocumento,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Novo Documento", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = aoAbrirGerenciarSignatarios,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Signatários", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroStatus == null,
                    onClick = { filtroStatus = null },
                    label = { Text("Todos (${listaDocumentos.size})") }
                )
                FilterChip(
                    selected = filtroStatus == StatusDocumento.Pendente,
                    onClick = { filtroStatus = StatusDocumento.Pendente },
                    label = { Text("Pendentes") }
                )
                FilterChip(
                    selected = filtroStatus == StatusDocumento.ParcialmenteAssinado,
                    onClick = { filtroStatus = StatusDocumento.ParcialmenteAssinado },
                    label = { Text("Parciais") }
                )
                FilterChip(
                    selected = filtroStatus == StatusDocumento.Concluido,
                    onClick = { filtroStatus = StatusDocumento.Concluido },
                    label = { Text("Concluídos") }
                )
            }

            if (documentosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum documento encontrado para o filtro selecionado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CinzaTextoSecundario
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documentosFiltrados, key = { it.id }) { documento ->
                        ItemDocumentoAdmin(
                            documento = documento,
                            aoClicar = { aoAbrirDetalhesDocumento(documento.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumoStatusCard(
    titulo: String,
    quantidade: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = quantidade.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPrimario
            )
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = CinzaTextoSecundario
            )
        }
    }
}

@Composable
private fun ItemDocumentoAdmin(
    documento: Documento,
    aoClicar: () -> Unit
) {
    val assinados = documento.signatarios.count { it.assinado }
    val totalSignatarios = documento.signatarios.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aoClicar() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = documento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaStatusDocumento(status = documento.status)
            }

            Text(
                text = "Arquivo: ${documento.nomeArquivo} (${documento.totalPaginas} pág.)",
                style = MaterialTheme.typography.bodyMedium,
                color = CinzaTextoSecundario
            )

            Text(
                text = "Assinaturas coletadas: $assinados de $totalSignatarios signatário(s)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AzulPrimario
            )

            val resumoNomes = documento.signatarios.joinToString(", ") {
                val situacao = if (it.assinado) "Assinado" else "Pendente"
                "${it.nomeCompleto} (${it.papel.descricao}: $situacao)"
            }
            Text(
                text = resumoNomes,
                style = MaterialTheme.typography.bodyMedium,
                color = CinzaTextoSecundario
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enviado em: ${documento.dataEnvio}",
                    fontSize = 12.sp,
                    color = CinzaTextoSecundario
                )
                Text(
                    text = "Ver acompanhamento ->",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPrimario
                )
            }
        }
    }
}
