package br.com.fiap.bestsign.ui.telas

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.Documento
import br.com.fiap.bestsign.modelo.PerfilUsuario
import br.com.fiap.bestsign.modelo.StatusDocumento
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.ControlesPaginacaoPdf
import br.com.fiap.bestsign.ui.componentes.EtiquetaPapelSignatario
import br.com.fiap.bestsign.ui.componentes.EtiquetaStatusDocumento
import br.com.fiap.bestsign.ui.tema.AmareloClaroFundo
import br.com.fiap.bestsign.ui.tema.AmareloPendente
import br.com.fiap.bestsign.ui.tema.AzulClaroFundo
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido
import kotlin.math.roundToInt

@Composable
fun TelaDetalhesDocumento(
    documentoId: Long,
    usuarioLogado: Usuario,
    documentoRepositorio: DocumentoRepositorio,
    aoIrParaAssinatura: (Long) -> Unit,
    aoVoltar: () -> Unit
) {
    var documento by remember { mutableStateOf<Documento?>(null) }
    var paginaAtual by remember { mutableIntStateOf(1) }
    var bitmapPagina by remember { mutableStateOf<Bitmap?>(null) }
    var tamanhoPreview by remember { mutableStateOf(IntSize(1, 1)) }

    LaunchedEffect(documentoId) {
        val doc = documentoRepositorio.buscarDocumentoPorIdComSeguranca(documentoId, usuarioLogado)
        documento = doc
        if (doc != null) {
            val pagComCampo = doc.camposAssinatura.firstOrNull()?.pagina ?: 1
            paginaAtual = pagComCampo.coerceIn(1, doc.totalPaginas.coerceAtLeast(1))
        }
    }

    LaunchedEffect(documento?.caminhoArquivo, paginaAtual) {
        val doc = documento
        if (doc != null) {
            bitmapPagina = documentoRepositorio.renderizarPaginaPdf(
                caminhoArquivo = doc.caminhoArquivo,
                numeroPagina = paginaAtual,
                dpi = 140f
            )
        }
    }

    val docAtual = documento
    if (docAtual == null) {
        Scaffold(
            topBar = { BarraSuperiorBestsign(titulo = "Acompanhamento", aoVoltar = aoVoltar) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Documento não encontrado ou acesso restrito.")
            }
        }
        return
    }

    val idSignatarioLogado = usuarioLogado.signatarioId ?: usuarioLogado.id
    val usuarioPodeAssinarAgora = usuarioLogado.perfil == PerfilUsuario.Signatario &&
        docAtual.signatarios.any { it.signatarioId == idSignatarioLogado && !it.assinado }

    val densidade = LocalDensity.current

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "Acompanhamento do Documento",
                aoVoltar = aoVoltar
            )
        }
    ) { espacamentoInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CinzaFundoTela)
                .padding(espacamentoInterno)
                .verticalScroll(rememberScrollState())
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = docAtual.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulPrimario,
                            modifier = Modifier.weight(1f)
                        )
                        EtiquetaStatusDocumento(status = docAtual.status)
                    }

                    Text(
                        text = "Arquivo: ${docAtual.nomeArquivo} (${docAtual.totalPaginas} página(s))",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )
                    Text(
                        text = "Criado em: ${docAtual.dataCriacao} | Enviado em: ${docAtual.dataEnvio}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AzulClaroFundo, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Mensagem do Convite:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AzulPrimario
                            )
                            Text(
                                text = docAtual.mensagemConvite,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Text(
                        text = "Nota de segurança: O documento enviado e suas configurações estão preservados e bloqueados para edição.",
                        fontSize = 12.sp,
                        color = CinzaTextoSecundario
                    )

                    if (usuarioPodeAssinarAgora) {
                        Button(
                            onClick = { aoIrParaAssinatura(docAtual.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeConcluido)
                        ) {
                            Text(
                                text = "IR PARA TELA DE ASSINATURA AGORA",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Situação Individual de Cada Signatário",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )

                    docAtual.signatarios.forEach { signatario ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (signatario.assinado) VerdeClaroFundo else AmareloClaroFundo,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (signatario.assinado) VerdeConcluido.copy(alpha = 0.35f) else AmareloPendente.copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = signatario.nomeCompleto,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    EtiquetaPapelSignatario(papel = signatario.papel)
                                }

                                Text(
                                    text = "E-mail: ${signatario.email} | Tel: ${signatario.telefone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CinzaTextoSecundario
                                )

                                Text(
                                    text = if (signatario.assinado) {
                                        "Status: Assinatura Realizada em ${signatario.dataAssinatura ?: ""}"
                                    } else {
                                        "Status: Assinatura Pendente"
                                    },
                                    color = if (signatario.assinado) VerdeConcluido else AmareloPendente,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (docAtual.status == StatusDocumento.Concluido) {
                            "Visualização do Documento PDF Final Concluído"
                        } else {
                            "Visualização do Documento PDF Atual"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )

                    ControlesPaginacaoPdf(
                        paginaAtual = paginaAtual,
                        totalPaginas = docAtual.totalPaginas.coerceAtLeast(1),
                        aoMudarPagina = { paginaAtual = it }
                    )

                    val bmp = bitmapPagina
                    if (bmp != null) {
                        val proporcao = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
                        val camposPendentesNaPagina = docAtual.camposAssinatura.filter {
                            it.pagina == paginaAtual && !it.assinado
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(proporcao)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.5.dp, CinzaBorda, RoundedCornerShape(8.dp))
                                .clipToBounds()
                                .onSizeChanged { tamanhoPreview = it }
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Página $paginaAtual do PDF",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )

                            val largTotal = tamanhoPreview.width.toFloat().coerceAtLeast(1f)
                            val altTotal = tamanhoPreview.height.toFloat().coerceAtLeast(1f)

                            camposPendentesNaPagina.forEach { campo ->
                                val largPx = campo.larguraProporcional * largTotal
                                val altPx = campo.alturaProporcional * altTotal
                                val xPx = campo.posicaoXProporcional * largTotal
                                val yPx = campo.posicaoYProporcional * altTotal

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
                                        .size(
                                            with(densidade) { largPx.toDp() },
                                            with(densidade) { altPx.toDp() }
                                        )
                                        .background(Color(0xD9FEF3C7), RoundedCornerShape(4.dp))
                                        .border(1.5.dp, AmareloPendente, RoundedCornerShape(4.dp))
                                        .padding(4.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Pendente: ${campo.nomeSignatario}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmareloPendente,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = campo.papelSignatario.descricao,
                                            fontSize = 9.sp,
                                            color = CinzaTextoSecundario,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Histórico Completo de Eventos",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )

                    docAtual.historico.forEachIndexed { indice, evento ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = evento.dataHora,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulPrimario
                            )
                            Text(
                                text = evento.descricao,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Responsável: ${evento.responsavel}",
                                fontSize = 12.sp,
                                color = CinzaTextoSecundario
                            )
                            if (indice < docAtual.historico.lastIndex) {
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = CinzaBorda)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
