package br.com.fiap.bestsign.ui.telas

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.CampoAssinatura
import br.com.fiap.bestsign.modelo.RascunhoDocumento
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.ControlesPaginacaoPdf
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VermelhoClaroFundo
import br.com.fiap.bestsign.ui.tema.VermelhoErro
import kotlin.math.roundToInt

@Composable
fun TelaPosicionamentoCampos(
    rascunhoAtual: RascunhoDocumento,
    documentoRepositorio: DocumentoRepositorio,
    aoAtualizarRascunho: (RascunhoDocumento) -> Unit,
    aoAvancarParaRevisao: () -> Unit,
    aoVoltar: () -> Unit
) {
    val totalPaginas = rascunhoAtual.totalPaginas.coerceAtLeast(1)
    var paginaAtual by remember { mutableIntStateOf(totalPaginas) }
    var signatarioSelecionadoId by remember {
        mutableLongStateOf(rascunhoAtual.signatarios.firstOrNull()?.signatarioId ?: 0L)
    }
    var campoSelecionadoId by remember { mutableLongStateOf(0L) }
    var bitmapPagina by remember { mutableStateOf<Bitmap?>(null) }
    var tamanhoContainerPdf by remember { mutableStateOf(IntSize(1, 1)) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    val rascunhoMaisRecente by rememberUpdatedState(rascunhoAtual)
    val atualizarRascunhoMaisRecente by rememberUpdatedState(aoAtualizarRascunho)

    LaunchedEffect(rascunhoAtual.signatarios) {
        if (rascunhoAtual.signatarios.none { it.signatarioId == signatarioSelecionadoId }) {
            signatarioSelecionadoId = rascunhoAtual.signatarios.firstOrNull()?.signatarioId ?: 0L
        }
    }

    LaunchedEffect(rascunhoAtual.caminhoArquivo, paginaAtual) {
        bitmapPagina = documentoRepositorio.renderizarPaginaPdf(
            caminhoArquivo = rascunhoAtual.caminhoArquivo,
            numeroPagina = paginaAtual,
            dpi = 140f
        )
    }

    val camposDaPaginaAtual = rascunhoAtual.camposAssinatura.filter { it.pagina == paginaAtual }
    val densidade = LocalDensity.current

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "2. Posicionar Assinaturas",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Escolha o Signatário e Adicione o Campo",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulPrimario
                    )

                    Text(
                        text = "Selecione um signatário abaixo, toque em 'Adicionar Campo nesta Página' e arraste o quadro sobre o PDF.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        rascunhoAtual.signatarios.forEach { signatario ->
                            val qtdCampos = rascunhoAtual.camposAssinatura.count {
                                it.signatarioId == signatario.signatarioId
                            }
                            FilterChip(
                                selected = signatarioSelecionadoId == signatario.signatarioId,
                                onClick = { signatarioSelecionadoId = signatario.signatarioId },
                                label = {
                                    Text(
                                        "${signatario.nomeCompleto} (${signatario.papel.descricao}) - $qtdCampos campo(s)"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val signatarioAlvo = rascunhoAtual.signatarios.find {
                                it.signatarioId == signatarioSelecionadoId
                            } ?: rascunhoAtual.signatarios.firstOrNull()

                            if (signatarioAlvo != null) {
                                mensagemErro = null
                                val novoId = (rascunhoAtual.camposAssinatura.maxOfOrNull { it.id } ?: 0L) + 1L
                                val qtdNaPagina = camposDaPaginaAtual.size
                                val deslocamentoY = (0.50f + (qtdNaPagina * 0.14f)).coerceAtMost(0.82f)
                                val novoCampo = CampoAssinatura(
                                    id = novoId,
                                    signatarioId = signatarioAlvo.signatarioId,
                                    nomeSignatario = signatarioAlvo.nomeCompleto,
                                    papelSignatario = signatarioAlvo.papel,
                                    pagina = paginaAtual,
                                    posicaoXProporcional = 0.12f,
                                    posicaoYProporcional = deslocamentoY,
                                    larguraProporcional = 0.42f,
                                    alturaProporcional = 0.13f
                                )
                                campoSelecionadoId = novoId
                                aoAtualizarRascunho(
                                    rascunhoAtual.copy(
                                        camposAssinatura = rascunhoAtual.camposAssinatura + novoCampo
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adicionar Campo de Assinatura nesta Página ($paginaAtual)")
                    }
                }
            }

            ControlesPaginacaoPdf(
                paginaAtual = paginaAtual,
                totalPaginas = totalPaginas,
                aoMudarPagina = { paginaAtual = it }
            )

            val bmp = bitmapPagina
            if (bmp != null) {
                val proporcaoPagina = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(proporcaoPagina)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, AzulPrimario.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clipToBounds()
                        .onSizeChanged { tamanhoContainerPdf = it }
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Página $paginaAtual do PDF",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    val larguraTotalPx = tamanhoContainerPdf.width.toFloat().coerceAtLeast(1f)
                    val alturaTotalPx = tamanhoContainerPdf.height.toFloat().coerceAtLeast(1f)

                    camposDaPaginaAtual.forEach { campo ->
                        val larguraCampoPx = (campo.larguraProporcional * larguraTotalPx).coerceAtLeast(40f)
                        val alturaCampoPx = (campo.alturaProporcional * alturaTotalPx).coerceAtLeast(28f)
                        val posXPx = (campo.posicaoXProporcional * larguraTotalPx)
                            .coerceIn(0f, (larguraTotalPx - larguraCampoPx).coerceAtLeast(0f))
                        val posYPx = (campo.posicaoYProporcional * alturaTotalPx)
                            .coerceIn(0f, (alturaTotalPx - alturaCampoPx).coerceAtLeast(0f))

                        val larguraDp = with(densidade) { larguraCampoPx.toDp() }
                        val alturaDp = with(densidade) { alturaCampoPx.toDp() }
                        val selecionado = campo.id == campoSelecionadoId

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(posXPx.roundToInt(), posYPx.roundToInt()) }
                                .size(larguraDp, alturaDp)
                                .background(
                                    if (selecionado) Color(0xD9DBEAFE) else Color(0xCCEFF6FF),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = if (selecionado) 2.5.dp else 1.5.dp,
                                    color = if (selecionado) AzulPrimario else Color(0xFF2563EB),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { campoSelecionadoId = campo.id }
                                .pointerInput(campo.id, larguraTotalPx, alturaTotalPx) {
                                    detectDragGestures(
                                        onDragStart = {
                                            campoSelecionadoId = campo.id
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val rascunho = rascunhoMaisRecente
                                            val campoAtual = rascunho.camposAssinatura.find { it.id == campo.id }
                                                ?: return@detectDragGestures

                                            val deltaXProp = dragAmount.x / larguraTotalPx
                                            val deltaYProp = dragAmount.y / alturaTotalPx

                                            val maxPosX = (1f - campoAtual.larguraProporcional).coerceAtLeast(0f)
                                            val maxPosY = (1f - campoAtual.alturaProporcional).coerceAtLeast(0f)

                                            val novoX = (campoAtual.posicaoXProporcional + deltaXProp).coerceIn(0f, maxPosX)
                                            val novoY = (campoAtual.posicaoYProporcional + deltaYProp).coerceIn(0f, maxPosY)

                                            val novaLista = rascunho.camposAssinatura.map { item ->
                                                if (item.id == campo.id) {
                                                    item.copy(
                                                        posicaoXProporcional = novoX,
                                                        posicaoYProporcional = novoY
                                                    )
                                                } else {
                                                    item
                                                }
                                            }
                                            atualizarRascunhoMaisRecente(rascunho.copy(camposAssinatura = novaLista))
                                        }
                                    )
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = campo.nomeSignatario,
                                        color = AzulPrimario,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "X",
                                        color = VermelhoErro,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                val novaLista = rascunhoAtual.camposAssinatura.filterNot {
                                                    it.id == campo.id
                                                }
                                                aoAtualizarRascunho(
                                                    rascunhoAtual.copy(camposAssinatura = novaLista)
                                                )
                                            }
                                            .padding(start = 4.dp)
                                    )
                                }

                                Text(
                                    text = "${campo.papelSignatario.descricao} (Arraste)",
                                    color = Color(0xFF1E3A8A),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            val campoSelecionado = rascunhoAtual.camposAssinatura.find { it.id == campoSelecionadoId }
            if (campoSelecionado != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Campo Selecionado: ${campoSelecionado.nomeSignatario} (Pág. ${campoSelecionado.pagina})",
                                style = MaterialTheme.typography.titleMedium,
                                color = AzulPrimario,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    val novaLista = rascunhoAtual.camposAssinatura.filterNot {
                                        it.id == campoSelecionado.id
                                    }
                                    campoSelecionadoId = 0L
                                    aoAtualizarRascunho(rascunhoAtual.copy(camposAssinatura = novaLista))
                                }
                            ) {
                                Text("Excluir Campo", color = VermelhoErro, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "Coordenadas Proporcionais: X=${(campoSelecionado.posicaoXProporcional * 100).roundToInt()}%, Y=${(campoSelecionado.posicaoYProporcional * 100).roundToInt()}% | Largura=${(campoSelecionado.larguraProporcional * 100).roundToInt()}%, Altura=${(campoSelecionado.alturaProporcional * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinzaTextoSecundario
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val novaLargura = (campoSelecionado.larguraProporcional - 0.05f).coerceIn(0.20f, 0.80f)
                                    val novoX = campoSelecionado.posicaoXProporcional.coerceIn(0f, 1f - novaLargura)
                                    val atualizados = rascunhoAtual.camposAssinatura.map {
                                        if (it.id == campoSelecionado.id) {
                                            it.copy(larguraProporcional = novaLargura, posicaoXProporcional = novoX)
                                        } else it
                                    }
                                    aoAtualizarRascunho(rascunhoAtual.copy(camposAssinatura = atualizados))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Menor Largura")
                            }

                            OutlinedButton(
                                onClick = {
                                    val novaLargura = (campoSelecionado.larguraProporcional + 0.05f).coerceIn(0.20f, 0.80f)
                                    val novoX = campoSelecionado.posicaoXProporcional.coerceIn(0f, 1f - novaLargura)
                                    val atualizados = rascunhoAtual.camposAssinatura.map {
                                        if (it.id == campoSelecionado.id) {
                                            it.copy(larguraProporcional = novaLargura, posicaoXProporcional = novoX)
                                        } else it
                                    }
                                    aoAtualizarRascunho(rascunhoAtual.copy(camposAssinatura = atualizados))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Maior Largura")
                            }
                        }
                    }
                }
            }

            if (mensagemErro != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VermelhoClaroFundo, RoundedCornerShape(10.dp))
                        .border(1.dp, VermelhoErro.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mensagemErro!!,
                        color = VermelhoErro,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = {
                    val signatarioSemCampo = rascunhoAtual.signatarios.firstOrNull { signatario ->
                        rascunhoAtual.camposAssinatura.none { it.signatarioId == signatario.signatarioId }
                    }
                    if (rascunhoAtual.camposAssinatura.isEmpty()) {
                        mensagemErro = "Posicione pelo menos 1 campo de assinatura no PDF antes de avançar."
                    } else if (signatarioSemCampo != null) {
                        mensagemErro = "Adicione pelo menos 1 campo de assinatura para ${signatarioSemCampo.nomeCompleto}."
                    } else {
                        mensagemErro = null
                        aoAvancarParaRevisao()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "AVANÇAR PARA REVISÃO E DISPARO",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
