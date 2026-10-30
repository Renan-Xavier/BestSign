package br.com.fiap.bestsign.ui.componentes

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.fiap.bestsign.ui.tema.AzulClaroFundo
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido
import br.com.fiap.bestsign.ui.tema.VermelhoErro
import br.com.fiap.bestsign.util.AutenticadorBiometrico

data class TracoAssinatura(
    val pontos: List<Offset>
)

@Composable
fun ModalEscolhaFormaAssinatura(
    nomeSignatario: String,
    papelSignatario: String,
    paginaCampo: Int,
    quantidadeCamposPendentes: Int,
    possuiDigitalConfigurada: Boolean,
    mensagemErroDigital: String?,
    aoEscolherManual: () -> Unit,
    aoEscolherDigital: (Boolean) -> Unit,
    aoCancelar: () -> Unit
) {
    var aplicarEmTodosMeusCampos by remember { mutableStateOf(true) }
    val previaAssinaturaCursiva = remember(nomeSignatario) {
        AutenticadorBiometrico.gerarBitmapAssinaturaPorExtenso(nomeSignatario)
    }

    Dialog(
        onDismissRequest = aoCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Escolha a Forma de Assinatura",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPrimario
                )

                Text(
                    text = "Signatário: $nomeSignatario ($papelSignatario) - Página $paginaCampo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Selecione como deseja assinar este documento:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinzaTextoSecundario
                )

                if (possuiDigitalConfigurada) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AzulClaroFundo, RoundedCornerShape(12.dp))
                            .border(1.5.dp, AzulPrimario, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Opção 1: Autenticação com Digital do Celular",
                            fontWeight = FontWeight.Bold,
                            color = AzulPrimario,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Usa a digital cadastrada no Android e preenche automaticamente seu nome completo em escrita cursiva por extenso no PDF:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(86.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, CinzaBorda, RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previaAssinaturaCursiva.asImageBitmap(),
                                contentDescription = "Prévia da assinatura por extenso",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Button(
                            onClick = { aoEscolherDigital(aplicarEmTodosMeusCampos) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeConcluido)
                        ) {
                            Text(
                                text = "ASSINAR COM DIGITAL DO CELULAR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .border(1.dp, CinzaBorda, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Assinatura com digital indisponível: nenhuma digital ou bloqueio configurado no Android deste aparelho.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinzaTextoSecundario
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                        .border(1.dp, CinzaBorda, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (possuiDigitalConfigurada) "Opção 2: Assinatura Manual na Tela" else "Assinatura Manual na Tela",
                        fontWeight = FontWeight.Bold,
                        color = AzulPrimario,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Abre o quadro em branco (Canvas) para você desenhar sua rubrica manualmente com o dedo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = aoEscolherManual,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                    ) {
                        Text(
                            text = "ASSINAR MANUALMENTE NA TELA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (quantidadeCamposPendentes > 1 && possuiDigitalConfigurada) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = aplicarEmTodosMeusCampos,
                            onCheckedChange = { aplicarEmTodosMeusCampos = it }
                        )
                        Text(
                            text = "Aplicar em todos os meus $quantidadeCamposPendentes campos pendentes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (mensagemErroDigital != null) {
                    Text(
                        text = mensagemErroDigital,
                        color = VermelhoErro,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = aoCancelar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun PopupAssinaturaDigitalConcluida(
    nomeSignatario: String
) {
    val bitmapCursivo = remember(nomeSignatario) {
        AutenticadorBiometrico.gerarBitmapAssinaturaPorExtenso(nomeSignatario)
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerdeClaroFundo)
                    .border(2.dp, VerdeConcluido, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(VerdeConcluido, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DIGITAL CONFIRMADA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Assinatura Concluída com Sucesso",
                    style = MaterialTheme.typography.titleLarge,
                    color = VerdeConcluido,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sua digital foi autenticada pelo Android e seu nome por extenso foi gravado no PDF:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, VerdeConcluido, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmapCursivo.asImageBitmap(),
                        contentDescription = "Assinatura por extenso gravada",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ModalDesenhoAssinatura(
    nomeSignatario: String,
    papelSignatario: String,
    paginaCampo: Int,
    quantidadeCamposPendentes: Int,
    aoCancelar: () -> Unit,
    aoConfirmarAssinatura: (Bitmap, Boolean) -> Unit
) {
    val tracos = remember { mutableStateListOf<TracoAssinatura>() }
    var pontosTracoAtual by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var tamanhoQuadro by remember { mutableStateOf(IntSize(600, 260)) }
    var aplicarEmTodosMeusCampos by remember { mutableStateOf(true) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = aoCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Desenhar Assinatura Eletrônica",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPrimario
                )

                Text(
                    text = "Signatário: $nomeSignatario ($papelSignatario) - Página $paginaCampo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Use o dedo na área em branco abaixo para assinar seu nome:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                        .border(2.dp, AzulPrimario, RoundedCornerShape(12.dp))
                        .clipToBounds()
                        .onSizeChanged { tamanhoQuadro = it }
                ) {
                    if (tracos.isEmpty() && pontosTracoAtual.isEmpty()) {
                        Text(
                            text = "Assine aqui dentro deste quadro",
                            color = Color(0xFF94A3B8),
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { posicaoToque ->
                                        mensagemErro = null
                                        val pontoLimitado = Offset(
                                            x = posicaoToque.x.coerceIn(0f, size.width.toFloat()),
                                            y = posicaoToque.y.coerceIn(0f, size.height.toFloat())
                                        )
                                        val realizado = tryAwaitRelease()
                                        if (realizado) {
                                            tracos.add(
                                                TracoAssinatura(
                                                    listOf(
                                                        pontoLimitado,
                                                        Offset(pontoLimitado.x + 2f, pontoLimitado.y + 2f)
                                                    )
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { inicio ->
                                        mensagemErro = null
                                        val pontoLimitado = Offset(
                                            x = inicio.x.coerceIn(0f, size.width.toFloat()),
                                            y = inicio.y.coerceIn(0f, size.height.toFloat())
                                        )
                                        pontosTracoAtual = listOf(pontoLimitado)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val pos = change.position
                                        val pontoLimitado = Offset(
                                            x = pos.x.coerceIn(0f, size.width.toFloat()),
                                            y = pos.y.coerceIn(0f, size.height.toFloat())
                                        )
                                        pontosTracoAtual = pontosTracoAtual + pontoLimitado
                                    },
                                    onDragEnd = {
                                        if (pontosTracoAtual.isNotEmpty()) {
                                            tracos.add(TracoAssinatura(pontosTracoAtual))
                                            pontosTracoAtual = emptyList()
                                        }
                                    },
                                    onDragCancel = {
                                        if (pontosTracoAtual.isNotEmpty()) {
                                            tracos.add(TracoAssinatura(pontosTracoAtual))
                                            pontosTracoAtual = emptyList()
                                        }
                                    }
                                )
                            }
                    ) {
                        val alturaGuia = size.height * 0.78f
                        drawLine(
                            color = CinzaBorda,
                            start = Offset(24f, alturaGuia),
                            end = Offset(size.width - 24f, alturaGuia),
                            strokeWidth = 2f
                        )

                        val todosOsTracos = if (pontosTracoAtual.isNotEmpty()) {
                            tracos + TracoAssinatura(pontosTracoAtual)
                        } else {
                            tracos
                        }

                        todosOsTracos.forEach { traco ->
                            if (traco.pontos.size == 1) {
                                drawCircle(
                                    color = Color(0xFF0B2545),
                                    radius = 4f,
                                    center = traco.pontos.first()
                                )
                            } else if (traco.pontos.size > 1) {
                                val caminho = Path().apply {
                                    moveTo(traco.pontos.first().x, traco.pontos.first().y)
                                    for (i in 1 until traco.pontos.size) {
                                        lineTo(traco.pontos[i].x, traco.pontos[i].y)
                                    }
                                }
                                drawPath(
                                    path = caminho,
                                    color = Color(0xFF0B2545),
                                    style = Stroke(
                                        width = 6f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }
                }

                if (quantidadeCamposPendentes > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = aplicarEmTodosMeusCampos,
                            onCheckedChange = { aplicarEmTodosMeusCampos = it }
                        )
                        Text(
                            text = "Aplicar minha assinatura em todos os meus $quantidadeCamposPendentes campos pendentes deste documento",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (mensagemErro != null) {
                    Text(
                        text = mensagemErro!!,
                        color = VermelhoErro,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tracos.clear()
                            pontosTracoAtual = emptyList()
                            mensagemErro = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpar")
                    }

                    OutlinedButton(
                        onClick = aoCancelar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }

                Button(
                    onClick = {
                        val tracosFinais = if (pontosTracoAtual.isNotEmpty()) {
                            tracos + TracoAssinatura(pontosTracoAtual)
                        } else {
                            tracos.toList()
                        }

                        if (tracosFinais.isEmpty()) {
                            mensagemErro = "Por favor, desenhe sua assinatura no quadro acima antes de confirmar."
                        } else {
                            val bitmapGerado = converterTracosParaBitmap(
                                tracos = tracosFinais,
                                larguraQuadro = tamanhoQuadro.width.coerceAtLeast(300),
                                alturaQuadro = tamanhoQuadro.height.coerceAtLeast(150)
                            )
                            aoConfirmarAssinatura(bitmapGerado, aplicarEmTodosMeusCampos)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeConcluido)
                ) {
                    Text(
                        text = "CONFIRMAR E GRAVAR ASSINATURA NO PDF",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun converterTracosParaBitmap(
    tracos: List<TracoAssinatura>,
    larguraQuadro: Int,
    alturaQuadro: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(larguraQuadro, alturaQuadro, Bitmap.Config.ARGB_8888)
    val canvasAndroid = android.graphics.Canvas(bitmap)

    val pinturaTraco = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(11, 37, 69)
        style = Paint.Style.STROKE
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    tracos.forEach { traco ->
        if (traco.pontos.size == 1) {
            val p = traco.pontos.first()
            canvasAndroid.drawPoint(p.x, p.y, pinturaTraco)
        } else if (traco.pontos.size > 1) {
            val caminho = android.graphics.Path()
            caminho.moveTo(traco.pontos.first().x, traco.pontos.first().y)
            for (i in 1 until traco.pontos.size) {
                caminho.lineTo(traco.pontos[i].x, traco.pontos[i].y)
            }
            canvasAndroid.drawPath(caminho, pinturaTraco)
        }
    }

    return bitmap
}
