package br.com.fiap.bestsign.ui.telas

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.CampoAssinatura
import br.com.fiap.bestsign.modelo.Documento
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.ControlesPaginacaoPdf
import br.com.fiap.bestsign.ui.componentes.EtiquetaPapelSignatario
import br.com.fiap.bestsign.ui.componentes.EtiquetaStatusDocumento
import br.com.fiap.bestsign.ui.componentes.ModalDesenhoAssinatura
import br.com.fiap.bestsign.ui.componentes.ModalEscolhaFormaAssinatura
import br.com.fiap.bestsign.ui.componentes.PopupAssinaturaDigitalConcluida
import br.com.fiap.bestsign.ui.tema.AmareloClaroFundo
import br.com.fiap.bestsign.ui.tema.AmareloPendente
import br.com.fiap.bestsign.ui.tema.AzulClaroFundo
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido
import br.com.fiap.bestsign.ui.tema.VermelhoClaroFundo
import br.com.fiap.bestsign.ui.tema.VermelhoErro
import br.com.fiap.bestsign.util.AutenticadorBiometrico
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TelaAssinaturaDocumento(
    documentoId: Long,
    usuarioLogado: Usuario,
    documentoRepositorio: DocumentoRepositorio,
    aoFinalizarAssinatura: (Long) -> Unit,
    aoVoltar: () -> Unit
) {
    val contexto = LocalContext.current
    val idSignatarioLogado = usuarioLogado.signatarioId ?: usuarioLogado.id
    var documento by remember { mutableStateOf<Documento?>(null) }
    var paginaAtual by remember { mutableIntStateOf(1) }
    var bitmapPagina by remember { mutableStateOf<Bitmap?>(null) }
    var tamanhoContainer by remember { mutableStateOf(IntSize(1, 1)) }
    var campoParaAssinar by remember { mutableStateOf<CampoAssinatura?>(null) }
    var exibirEscolhaFormaAssinatura by remember { mutableStateOf(false) }
    var exibirQuadroDesenhoManual by remember { mutableStateOf(false) }
    var exibirPopupSucessoDigital by remember { mutableStateOf(false) }
    var erroAutenticacaoDigital by remember { mutableStateOf<String?>(null) }
    var versaoRenderizacaoPdf by remember { mutableIntStateOf(0) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(exibirPopupSucessoDigital) {
        if (exibirPopupSucessoDigital) {
            delay(1500L)
            exibirPopupSucessoDigital = false
        }
    }

    LaunchedEffect(documentoId) {
        val carregado = documentoRepositorio.buscarDocumentoPorIdComSeguranca(documentoId, usuarioLogado)
        documento = carregado
        if (carregado != null) {
            val primeiraPaginaPendente = carregado.camposAssinatura.firstOrNull {
                it.signatarioId == idSignatarioLogado && !it.assinado
            }?.pagina ?: 1
            paginaAtual = primeiraPaginaPendente.coerceIn(1, carregado.totalPaginas.coerceAtLeast(1))
        }
    }

    LaunchedEffect(documento?.caminhoArquivo, paginaAtual, versaoRenderizacaoPdf) {
        val doc = documento
        if (doc != null) {
            bitmapPagina = documentoRepositorio.renderizarPaginaPdf(
                caminhoArquivo = doc.caminhoArquivo,
                numeroPagina = paginaAtual,
                dpi = 150f
            )
        }
    }

    val docAtual = documento
    if (docAtual == null) {
        Scaffold(
            topBar = { BarraSuperiorBestsign(titulo = "Assinatura de Documento", aoVoltar = aoVoltar) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Documento não encontrado ou você não possui permissão para visualizá-lo.")
            }
        }
        return
    }

    val meuVinculo = docAtual.signatarios.find { it.signatarioId == idSignatarioLogado }
    val meusCamposPendentes = docAtual.camposAssinatura.filter {
        it.signatarioId == idSignatarioLogado && !it.assinado
    }
    val densidade = LocalDensity.current

    if (exibirPopupSucessoDigital) {
        PopupAssinaturaDigitalConcluida(
            nomeSignatario = usuarioLogado.nomeCompleto
        )
    }

    if (campoParaAssinar != null && exibirEscolhaFormaAssinatura) {
        val campoAlvo = campoParaAssinar!!
        val possuiDigital = AutenticadorBiometrico.possuiDigitalConfigurada(contexto)

        ModalEscolhaFormaAssinatura(
            nomeSignatario = usuarioLogado.nomeCompleto,
            papelSignatario = meuVinculo?.papel?.descricao ?: campoAlvo.papelSignatario.descricao,
            paginaCampo = campoAlvo.pagina,
            quantidadeCamposPendentes = meusCamposPendentes.size,
            possuiDigitalConfigurada = possuiDigital,
            mensagemErroDigital = erroAutenticacaoDigital,
            aoEscolherManual = {
                erroAutenticacaoDigital = null
                exibirEscolhaFormaAssinatura = false
                exibirQuadroDesenhoManual = true
            },
            aoEscolherDigital = { assinarTodosMeusCampos ->
                erroAutenticacaoDigital = null
                exibirEscolhaFormaAssinatura = false
                AutenticadorBiometrico.solicitarAutenticacaoDigital(
                    contexto = contexto,
                    nomeSignatario = usuarioLogado.nomeCompleto,
                    aoAutenticarComSucesso = {
                        val idsParaGravar = if (assinarTodosMeusCampos) {
                            meusCamposPendentes.map { it.id }
                        } else {
                            listOf(campoAlvo.id)
                        }

                        val resultado = documentoRepositorio.assinarDocumentoComDigital(
                            documentoId = docAtual.id,
                            usuarioLogado = usuarioLogado,
                            idsCamposSelecionados = idsParaGravar
                        )

                        resultado.onSuccess { docAtualizado ->
                            documento = docAtualizado
                            campoParaAssinar = null
                            versaoRenderizacaoPdf++
                            mensagemErro = null
                            exibirPopupSucessoDigital = true

                            val aindaRestamCampos = docAtualizado.camposAssinatura.any {
                                it.signatarioId == idSignatarioLogado && !it.assinado
                            }
                            if (!aindaRestamCampos) {
                                mensagemSucesso = "Assinatura por extenso autenticada por digital e gravada no PDF com sucesso."
                            } else {
                                mensagemSucesso = "Campo assinado com digital. Toque no próximo campo pendente para concluir."
                            }
                        }.onFailure { erro ->
                            campoParaAssinar = null
                            mensagemErro = erro.message ?: "Não foi possível gravar a assinatura no PDF."
                        }
                    },
                    aoFalharOuCancelar = { motivo ->
                        campoParaAssinar = null
                        mensagemErro = motivo
                    }
                )
            },
            aoCancelar = {
                erroAutenticacaoDigital = null
                exibirEscolhaFormaAssinatura = false
                campoParaAssinar = null
            }
        )
    }

    if (campoParaAssinar != null && exibirQuadroDesenhoManual) {
        val campoAlvo = campoParaAssinar!!
        ModalDesenhoAssinatura(
            nomeSignatario = usuarioLogado.nomeCompleto,
            papelSignatario = meuVinculo?.papel?.descricao ?: campoAlvo.papelSignatario.descricao,
            paginaCampo = campoAlvo.pagina,
            quantidadeCamposPendentes = meusCamposPendentes.size,
            aoCancelar = {
                exibirQuadroDesenhoManual = false
                campoParaAssinar = null
            },
            aoConfirmarAssinatura = { bitmapDesenhado, assinarTodosMeusCampos ->
                val idsParaGravar = if (assinarTodosMeusCampos) {
                    meusCamposPendentes.map { it.id }
                } else {
                    listOf(campoAlvo.id)
                }

                val resultado = documentoRepositorio.assinarDocumento(
                    documentoId = docAtual.id,
                    usuarioLogado = usuarioLogado,
                    idsCamposSelecionados = idsParaGravar,
                    bitmapAssinatura = bitmapDesenhado,
                    autenticadoPorDigital = false
                )

                resultado.onSuccess { docAtualizado ->
                    documento = docAtualizado
                    exibirQuadroDesenhoManual = false
                    campoParaAssinar = null
                    versaoRenderizacaoPdf++
                    mensagemErro = null

                    val aindaRestamCampos = docAtualizado.camposAssinatura.any {
                        it.signatarioId == idSignatarioLogado && !it.assinado
                    }
                    if (!aindaRestamCampos) {
                        mensagemSucesso = "Sua assinatura manual foi gravada no PDF com sucesso."
                    } else {
                        mensagemSucesso = "Campo assinado. Toque no próximo campo pendente para concluir."
                    }
                }.onFailure { erro ->
                    exibirQuadroDesenhoManual = false
                    campoParaAssinar = null
                    mensagemErro = erro.message ?: "Não foi possível gravar a assinatura no PDF."
                }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "Assinar Documento",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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

                    if (meuVinculo != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Seu papel neste documento:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
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
                                text = "Mensagem do convite:",
                                fontWeight = FontWeight.Bold,
                                color = AzulPrimario,
                                fontSize = 14.sp
                            )
                            Text(
                                text = docAtual.mensagemConvite,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (meusCamposPendentes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AmareloClaroFundo, RoundedCornerShape(10.dp))
                                .border(1.5.dp, AmareloPendente, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "INSTRUÇÃO: Você possui ${meusCamposPendentes.size} campo(s) pendente(s). Toque no quadro destacado no PDF abaixo ou no botão rápido para escolher como assinar.",
                                    color = AmareloPendente,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )

                                Button(
                                    onClick = {
                                        val proximoCampo = meusCamposPendentes.first()
                                        paginaAtual = proximoCampo.pagina
                                        erroAutenticacaoDigital = null
                                        campoParaAssinar = proximoCampo
                                        exibirEscolhaFormaAssinatura = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                                ) {
                                    val pagCampo = meusCamposPendentes.first().pagina
                                    Text(
                                        text = "ASSINAR MEU CAMPO AGORA (PÁGINA $pagCampo)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VerdeClaroFundo, RoundedCornerShape(10.dp))
                                .border(1.5.dp, VerdeConcluido, RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Sua assinatura neste documento foi concluída e gravada no PDF.",
                                    color = VerdeConcluido,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Button(
                                    onClick = { aoFinalizarAssinatura(docAtual.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeConcluido)
                                ) {
                                    Text("VER ACOMPANHAMENTO E HISTÓRICO DO DOCUMENTO", fontWeight = FontWeight.Bold)
                                }
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
                        .border(1.dp, VermelhoErro, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mensagemErro!!,
                        color = VermelhoErro,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (mensagemSucesso != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VerdeClaroFundo, RoundedCornerShape(10.dp))
                        .border(1.dp, VerdeConcluido, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mensagemSucesso!!,
                        color = VerdeConcluido,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ControlesPaginacaoPdf(
                paginaAtual = paginaAtual,
                totalPaginas = docAtual.totalPaginas.coerceAtLeast(1),
                aoMudarPagina = { paginaAtual = it }
            )

            val bmp = bitmapPagina
            if (bmp != null) {
                val proporcao = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
                val camposNaPagina = docAtual.camposAssinatura.filter { it.pagina == paginaAtual }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(proporcao)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, AzulPrimario.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clipToBounds()
                        .onSizeChanged { tamanhoContainer = it }
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Página $paginaAtual do documento PDF",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    val largTotal = tamanhoContainer.width.toFloat().coerceAtLeast(1f)
                    val altTotal = tamanhoContainer.height.toFloat().coerceAtLeast(1f)

                    camposNaPagina.forEach { campo ->
                        if (!campo.assinado) {
                            val ehMeuCampo = campo.signatarioId == idSignatarioLogado
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
                                    .background(
                                        if (ehMeuCampo) Color(0xEBFEF3C7) else Color(0xCCE2E8F0),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = if (ehMeuCampo) 2.5.dp else 1.dp,
                                        color = if (ehMeuCampo) AmareloPendente else CinzaBorda,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .let { mod ->
                                        if (ehMeuCampo) {
                                            mod.clickable {
                                                erroAutenticacaoDigital = null
                                                campoParaAssinar = campo
                                                exibirEscolhaFormaAssinatura = true
                                            }
                                        } else {
                                            mod
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (ehMeuCampo) "TOQUE AQUI PARA ASSINAR" else "Reservado: ${campo.nomeSignatario}",
                                        color = if (ehMeuCampo) AmareloPendente else CinzaTextoSecundario,
                                        fontSize = if (ehMeuCampo) 11.sp else 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${campo.nomeSignatario} (${campo.papelSignatario.descricao})",
                                        color = if (ehMeuCampo) AzulPrimario else CinzaTextoSecundario,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { aoFinalizarAssinatura(docAtual.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Ver Detalhes e Histórico de Eventos")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
