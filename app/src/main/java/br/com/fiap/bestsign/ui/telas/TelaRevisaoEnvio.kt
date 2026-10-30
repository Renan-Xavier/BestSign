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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import br.com.fiap.bestsign.modelo.PapelSignatario
import br.com.fiap.bestsign.modelo.RascunhoDocumento
import br.com.fiap.bestsign.modelo.Signatario
import br.com.fiap.bestsign.modelo.SignatarioDocumento
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.repositorio.SignatarioRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.ControlesPaginacaoPdf
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeConcluido
import br.com.fiap.bestsign.ui.tema.VermelhoClaroFundo
import br.com.fiap.bestsign.ui.tema.VermelhoErro
import kotlin.math.roundToInt

@Composable
fun TelaRevisaoEnvio(
    usuarioLogado: Usuario,
    rascunhoAtual: RascunhoDocumento,
    documentoRepositorio: DocumentoRepositorio,
    signatarioRepositorio: SignatarioRepositorio,
    aoAtualizarRascunho: (RascunhoDocumento) -> Unit,
    aoConfirmarEnvioComSucesso: (Long) -> Unit,
    aoVoltar: () -> Unit
) {
    val totalPaginas = rascunhoAtual.totalPaginas.coerceAtLeast(1)
    val paginaInicialComCampo = rascunhoAtual.camposAssinatura.firstOrNull()?.pagina ?: 1
    var paginaPreview by remember { mutableIntStateOf(paginaInicialComCampo.coerceIn(1, totalPaginas)) }
    var bitmapPreview by remember { mutableStateOf<Bitmap?>(null) }
    var tamanhoPreview by remember { mutableStateOf(IntSize(1, 1)) }
    var todosSignatariosBanco by remember { mutableStateOf<List<Signatario>>(emptyList()) }
    var signatarioParaSubstituir by remember { mutableStateOf<SignatarioDocumento?>(null) }
    var mensagemErroEnvio by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        todosSignatariosBanco = signatarioRepositorio.listarSignatarios()
    }

    LaunchedEffect(rascunhoAtual.caminhoArquivo, paginaPreview) {
        bitmapPreview = documentoRepositorio.renderizarPaginaPdf(
            caminhoArquivo = rascunhoAtual.caminhoArquivo,
            numeroPagina = paginaPreview,
            dpi = 120f
        )
    }

    val resumoPorPagina = (1..totalPaginas).map { numPagina ->
        val qtd = rascunhoAtual.camposAssinatura.count { it.pagina == numPagina }
        "Página $numPagina: $qtd campo(s)"
    }.joinToString(" | ")

    val densidade = LocalDensity.current

    if (signatarioParaSubstituir != null) {
        val alvo = signatarioParaSubstituir!!
        val idsJaNoDocumento = rascunhoAtual.signatarios.map { it.signatarioId }.toSet()
        val opcoesSubstituicao = todosSignatariosBanco.filter { it.id !in idsJaNoDocumento }

        AlertDialog(
            onDismissRequest = { signatarioParaSubstituir = null },
            title = { Text("Substituir Signatário") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Substituir ${alvo.nomeCompleto} por outro signatário cadastrado:")
                    if (opcoesSubstituicao.isEmpty()) {
                        Text(
                            text = "Todos os signatários cadastrados já estão incluídos neste documento.",
                            color = CinzaTextoSecundario
                        )
                    } else {
                        opcoesSubstituicao.forEach { candidato ->
                            OutlinedButton(
                                onClick = {
                                    val novosSignatarios = rascunhoAtual.signatarios.map { item ->
                                        if (item.signatarioId == alvo.signatarioId) {
                                            item.copy(
                                                signatarioId = candidato.id,
                                                nomeCompleto = candidato.nomeCompleto,
                                                email = candidato.email,
                                                telefone = candidato.telefone
                                            )
                                        } else item
                                    }
                                    val novosCampos = rascunhoAtual.camposAssinatura.map { campo ->
                                        if (campo.signatarioId == alvo.signatarioId) {
                                            campo.copy(
                                                signatarioId = candidato.id,
                                                nomeSignatario = candidato.nomeCompleto
                                            )
                                        } else campo
                                    }
                                    aoAtualizarRascunho(
                                        rascunhoAtual.copy(
                                            signatarios = novosSignatarios,
                                            camposAssinatura = novosCampos
                                        )
                                    )
                                    signatarioParaSubstituir = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${candidato.nomeCompleto} (${candidato.email})")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { signatarioParaSubstituir = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "3. Revisão e Disparo",
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
                    Text(
                        text = "Resumo do Documento e Páginas",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )
                    Text(
                        text = "Título: ${rascunhoAtual.titulo}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Arquivo PDF: ${rascunhoAtual.nomeArquivo} (${rascunhoAtual.totalPaginas} página(s))",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )
                    Text(
                        text = "Campos posicionados: $resumoPorPagina",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulPrimario
                    )

                    ControlesPaginacaoPdf(
                        paginaAtual = paginaPreview,
                        totalPaginas = totalPaginas,
                        aoMudarPagina = { paginaPreview = it }
                    )

                    val bmp = bitmapPreview
                    if (bmp != null) {
                        val proporcao = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
                        val camposNaPagina = rascunhoAtual.camposAssinatura.filter { it.pagina == paginaPreview }

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
                                contentDescription = "Revisão da página $paginaPreview",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )

                            val largTotal = tamanhoPreview.width.toFloat().coerceAtLeast(1f)
                            val altTotal = tamanhoPreview.height.toFloat().coerceAtLeast(1f)

                            camposNaPagina.forEach { campo ->
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
                                        .background(Color(0xD9EFF6FF), RoundedCornerShape(4.dp))
                                        .border(1.5.dp, AzulPrimario, RoundedCornerShape(4.dp))
                                        .padding(4.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = campo.nomeSignatario,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulPrimario,
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
                        text = "Signatários Selecionados (${rascunhoAtual.signatarios.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )

                    Text(
                        text = "Revise a classificação de cada signatário. Você pode alterar o papel, substituir ou remover antes do envio:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )

                    rascunhoAtual.signatarios.forEach { signatario ->
                        val qtdCampos = rascunhoAtual.camposAssinatura.count {
                            it.signatarioId == signatario.signatarioId
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .border(1.dp, CinzaBorda, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${signatario.nomeCompleto} (${signatario.email})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Campos de assinatura vinculados: $qtdCampos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinzaTextoSecundario
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PapelSignatario.entries.forEach { papel ->
                                    FilterChip(
                                        selected = signatario.papel == papel,
                                        onClick = {
                                            val novosSignatarios = rascunhoAtual.signatarios.map {
                                                if (it.signatarioId == signatario.signatarioId) {
                                                    it.copy(papel = papel)
                                                } else it
                                            }
                                            val novosCampos = rascunhoAtual.camposAssinatura.map {
                                                if (it.signatarioId == signatario.signatarioId) {
                                                    it.copy(papelSignatario = papel)
                                                } else it
                                            }
                                            aoAtualizarRascunho(
                                                rascunhoAtual.copy(
                                                    signatarios = novosSignatarios,
                                                    camposAssinatura = novosCampos
                                                )
                                            )
                                        },
                                        label = { Text(papel.descricao) }
                                    )
                                }
                            }

                            HorizontalDivider(color = CinzaBorda)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { signatarioParaSubstituir = signatario },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Substituir")
                                }

                                OutlinedButton(
                                    onClick = {
                                        mensagemErroEnvio = null
                                        val novosSignatarios = rascunhoAtual.signatarios.filterNot {
                                            it.signatarioId == signatario.signatarioId
                                        }
                                        val novosCampos = rascunhoAtual.camposAssinatura.filterNot {
                                            it.signatarioId == signatario.signatarioId
                                        }
                                        aoAtualizarRascunho(
                                            rascunhoAtual.copy(
                                                signatarios = novosSignatarios,
                                                camposAssinatura = novosCampos
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Remover", color = VermelhoErro)
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
                        text = "Mensagem do Convite",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )

                    Text(
                        text = "Esta mensagem será exibida aos signatários antes de efetuarem a assinatura:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )

                    OutlinedTextField(
                        value = rascunhoAtual.mensagemConvite,
                        onValueChange = { texto ->
                            mensagemErroEnvio = null
                            aoAtualizarRascunho(rascunhoAtual.copy(mensagemConvite = texto))
                        },
                        label = { Text("Mensagem para os signatários *") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (mensagemErroEnvio != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VermelhoClaroFundo, RoundedCornerShape(10.dp))
                        .border(1.dp, VermelhoErro.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mensagemErroEnvio!!,
                        color = VermelhoErro,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = {
                    val resultado = documentoRepositorio.salvarDocumento(
                        rascunho = rascunhoAtual,
                        nomeResponsavel = usuarioLogado.nomeCompleto
                    )
                    resultado.onSuccess { idDocumentoCriado ->
                        mensagemErroEnvio = null
                        aoConfirmarEnvioComSucesso(idDocumentoCriado)
                    }.onFailure { erro ->
                        mensagemErroEnvio = erro.message
                            ?: "Falha ao registrar o envio do documento. Verifique os dados e tente novamente."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeConcluido)
            ) {
                Text(
                    text = "CONFIRMAR E ENVIAR DOCUMENTO",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
