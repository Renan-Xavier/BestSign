package br.com.fiap.bestsign.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.EtiquetaPapelSignatario
import br.com.fiap.bestsign.ui.componentes.EtiquetaStatusDocumento
import br.com.fiap.bestsign.ui.tema.AmareloClaroFundo
import br.com.fiap.bestsign.ui.tema.AmareloPendente
import br.com.fiap.bestsign.ui.tema.AzulClaroFundo
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido

@Composable
fun TelaPainelSignatario(
    usuarioLogado: Usuario,
    documentoRepositorio: DocumentoRepositorio,
    aoAbrirAssinatura: (Long) -> Unit,
    aoAbrirDetalhes: (Long) -> Unit,
    aoSair: () -> Unit
) {
    val idSignatarioLogado = usuarioLogado.signatarioId ?: usuarioLogado.id
    var meusDocumentos by remember { mutableStateOf<List<Documento>>(emptyList()) }
    var exibirAbaPendentes by remember { mutableStateOf(true) }

    LaunchedEffect(idSignatarioLogado) {
        meusDocumentos = documentoRepositorio.listarDocumentosParaSignatario(idSignatarioLogado)
    }

    val documentosPendentesParaMim = remember(meusDocumentos, idSignatarioLogado) {
        meusDocumentos.filter { doc ->
            doc.signatarios.any { it.signatarioId == idSignatarioLogado && !it.assinado }
        }
    }

    val documentosJaAssinadosPorMim = remember(meusDocumentos, idSignatarioLogado) {
        meusDocumentos.filter { doc ->
            doc.signatarios.any { it.signatarioId == idSignatarioLogado && it.assinado }
        }
    }

    val listaExibida = if (exibirAbaPendentes) {
        documentosPendentesParaMim
    } else {
        documentosJaAssinadosPorMim
    }

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "Meus Documentos",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Bem-vindo(a), ${usuarioLogado.nomeCompleto}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )
                    Text(
                        text = "Conta autenticada: ${usuarioLogado.email}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CinzaTextoSecundario
                    )
                    Text(
                        text = "Aqui você visualiza exclusivamente os documentos enviados para você.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = exibirAbaPendentes,
                    onClick = { exibirAbaPendentes = true },
                    label = {
                        Text(
                            text = "Pendentes para Assinar (${documentosPendentesParaMim.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = !exibirAbaPendentes,
                    onClick = { exibirAbaPendentes = false },
                    label = {
                        Text(
                            text = "Já Assinados (${documentosJaAssinadosPorMim.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (listaExibida.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (exibirAbaPendentes) {
                            "Você não possui documentos pendentes para assinatura no momento."
                        } else {
                            "Você ainda não assinou nenhum documento."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = CinzaTextoSecundario
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaExibida, key = { it.id }) { documento ->
                        CartaoDocumentoSignatario(
                            documento = documento,
                            idSignatarioLogado = idSignatarioLogado,
                            aoAbrirParaAssinar = { aoAbrirAssinatura(documento.id) },
                            aoAbrirAcompanhamento = { aoAbrirDetalhes(documento.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoDocumentoSignatario(
    documento: Documento,
    idSignatarioLogado: Long,
    aoAbrirParaAssinar: () -> Unit,
    aoAbrirAcompanhamento: () -> Unit
) {
    val meuVinculo = documento.signatarios.find { it.signatarioId == idSignatarioLogado }
    val jaAssinadoPorMim = meuVinculo?.assinado == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = documento.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPrimario,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaStatusDocumento(status = documento.status)
            }

            if (meuVinculo != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Seu papel neste documento:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    EtiquetaPapelSignatario(papel = meuVinculo.papel)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulClaroFundo, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Mensagem do remetente:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulPrimario
                    )
                    Text(
                        text = documento.mensagemConvite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (jaAssinadoPorMim) VerdeClaroFundo else AmareloClaroFundo,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (jaAssinadoPorMim) VerdeConcluido.copy(alpha = 0.4f) else AmareloPendente.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = if (jaAssinadoPorMim) {
                        "Sua situação: Assinatura Realizada em ${meuVinculo?.dataAssinatura ?: ""}"
                    } else {
                        "Sua situação: Assinatura Pendente"
                    },
                    color = if (jaAssinadoPorMim) VerdeConcluido else AmareloPendente,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (!jaAssinadoPorMim) {
                Button(
                    onClick = aoAbrirParaAssinar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeConcluido)
                ) {
                    Text(
                        text = "ABRIR DOCUMENTO E ASSINAR AGORA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = aoAbrirAcompanhamento,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Ver Detalhes, PDF e Histórico Completo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
