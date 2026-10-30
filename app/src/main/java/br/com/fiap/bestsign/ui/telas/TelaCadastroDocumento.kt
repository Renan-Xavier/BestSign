package br.com.fiap.bestsign.ui.telas

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.PapelSignatario
import br.com.fiap.bestsign.modelo.RascunhoDocumento
import br.com.fiap.bestsign.modelo.Signatario
import br.com.fiap.bestsign.modelo.SignatarioDocumento
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.repositorio.SignatarioRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.componentes.ControlesPaginacaoPdf
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido
import br.com.fiap.bestsign.ui.tema.VermelhoClaroFundo
import br.com.fiap.bestsign.ui.tema.VermelhoErro

@Composable
fun TelaCadastroDocumento(
    rascunhoAtual: RascunhoDocumento,
    documentoRepositorio: DocumentoRepositorio,
    signatarioRepositorio: SignatarioRepositorio,
    aoAtualizarRascunho: (RascunhoDocumento) -> Unit,
    aoAbrirCadastroSignatarios: () -> Unit,
    aoAvancarParaPosicionamento: () -> Unit,
    aoVoltar: () -> Unit
) {
    var signatariosDisponiveis by remember { mutableStateOf<List<Signatario>>(emptyList()) }
    var erroArquivoPdf by remember { mutableStateOf<String?>(null) }
    var erroFormulario by remember { mutableStateOf<String?>(null) }
    var paginaVisualizacao by remember { mutableIntStateOf(1) }
    var bitmapPaginaPreview by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        signatariosDisponiveis = signatarioRepositorio.listarSignatarios()
    }

    LaunchedEffect(rascunhoAtual.caminhoArquivo, paginaVisualizacao) {
        bitmapPaginaPreview = if (rascunhoAtual.caminhoArquivo.isNotBlank() && rascunhoAtual.totalPaginas > 0) {
            documentoRepositorio.renderizarPaginaPdf(
                caminhoArquivo = rascunhoAtual.caminhoArquivo,
                numeroPagina = paginaVisualizacao.coerceIn(1, rascunhoAtual.totalPaginas),
                dpi = 110f
            )
        } else {
            null
        }
    }

    val seletorArquivoPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uriSelecionada ->
        if (uriSelecionada != null) {
            val validacao = documentoRepositorio.validarArquivoPdf(uriSelecionada)
            if (validacao.valido) {
                erroArquivoPdf = null
                erroFormulario = null
                paginaVisualizacao = 1
                aoAtualizarRascunho(
                    rascunhoAtual.copy(
                        nomeArquivo = validacao.nomeArquivo,
                        caminhoArquivo = validacao.caminhoArquivo,
                        totalPaginas = validacao.totalPaginas,
                        camposAssinatura = emptyList()
                    )
                )
            } else {
                erroArquivoPdf = validacao.mensagemErro ?: "O arquivo PDF selecionado é inválido."
                aoAtualizarRascunho(
                    rascunhoAtual.copy(
                        nomeArquivo = "",
                        caminhoArquivo = "",
                        totalPaginas = 0,
                        camposAssinatura = emptyList()
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "1. Novo Fluxo de Documento",
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
                    Text(
                        text = "Dados e Arquivo PDF",
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulPrimario
                    )

                    OutlinedTextField(
                        value = rascunhoAtual.titulo,
                        onValueChange = { novoTitulo ->
                            erroFormulario = null
                            aoAtualizarRascunho(rascunhoAtual.copy(titulo = novoTitulo))
                        },
                        label = { Text("Título do documento *") },
                        placeholder = { Text("Ex: Contrato de Prestação de Serviços") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { seletorArquivoPdf.launch(arrayOf("application/pdf", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Selecionar PDF do Celular")
                        }

                        OutlinedButton(
                            onClick = {
                                val resultadoModelo = documentoRepositorio.gerarArquivoPdfModelo(rascunhoAtual.titulo)
                                if (resultadoModelo.valido) {
                                    erroArquivoPdf = null
                                    erroFormulario = null
                                    paginaVisualizacao = 1
                                    val tituloSugerido = rascunhoAtual.titulo.ifBlank {
                                        "Contrato de Prestação de Serviços"
                                    }
                                    aoAtualizarRascunho(
                                        rascunhoAtual.copy(
                                            titulo = tituloSugerido,
                                            nomeArquivo = resultadoModelo.nomeArquivo,
                                            caminhoArquivo = resultadoModelo.caminhoArquivo,
                                            totalPaginas = resultadoModelo.totalPaginas,
                                            camposAssinatura = emptyList()
                                        )
                                    )
                                } else {
                                    erroArquivoPdf = resultadoModelo.mensagemErro
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Usar PDF de Modelo")
                        }
                    }

                    if (erroArquivoPdf != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VermelhoClaroFundo, RoundedCornerShape(10.dp))
                                .border(1.dp, VermelhoErro.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = erroArquivoPdf!!,
                                color = VermelhoErro,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (rascunhoAtual.caminhoArquivo.isNotBlank() && rascunhoAtual.totalPaginas > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VerdeClaroFundo, RoundedCornerShape(10.dp))
                                .border(1.dp, VerdeConcluido.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "PDF validado com sucesso!",
                                    color = VerdeConcluido,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Arquivo: ${rascunhoAtual.nomeArquivo} (${rascunhoAtual.totalPaginas} página(s))",
                                    color = VerdeConcluido,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        ControlesPaginacaoPdf(
                            paginaAtual = paginaVisualizacao,
                            totalPaginas = rascunhoAtual.totalPaginas,
                            aoMudarPagina = { paginaVisualizacao = it }
                        )

                        val bmp = bitmapPaginaPreview
                        if (bmp != null) {
                            val proporcao = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(proporcao)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(1.dp, CinzaBorda, RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Visualização da página $paginaVisualizacao do PDF",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Signatários e Papéis",
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulPrimario
                        )
                        TextButton(onClick = aoAbrirCadastroSignatarios) {
                            Text("+ Cadastrar Novo")
                        }
                    }

                    Text(
                        text = "Selecione pelo menos 1 signatário obrigatório e defina seu papel no documento:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTextoSecundario
                    )

                    signatariosDisponiveis.forEach { signatario ->
                        val vinculoExistente = rascunhoAtual.signatarios.find {
                            it.signatarioId == signatario.id
                        }
                        val selecionado = vinculoExistente != null

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selecionado) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (selecionado) AzulPrimario else CinzaBorda,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = selecionado,
                                        onCheckedChange = { marcado ->
                                            erroFormulario = null
                                            if (marcado) {
                                                val novoVinculo = SignatarioDocumento(
                                                    signatarioId = signatario.id,
                                                    nomeCompleto = signatario.nomeCompleto,
                                                    email = signatario.email,
                                                    telefone = signatario.telefone,
                                                    papel = PapelSignatario.Contratante,
                                                    obrigatorio = true
                                                )
                                                aoAtualizarRascunho(
                                                    rascunhoAtual.copy(
                                                        signatarios = rascunhoAtual.signatarios + novoVinculo
                                                    )
                                                )
                                            } else {
                                                aoAtualizarRascunho(
                                                    rascunhoAtual.copy(
                                                        signatarios = rascunhoAtual.signatarios.filterNot {
                                                            it.signatarioId == signatario.id
                                                        },
                                                        camposAssinatura = rascunhoAtual.camposAssinatura.filterNot {
                                                            it.signatarioId == signatario.id
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = signatario.nomeCompleto,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = signatario.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CinzaTextoSecundario
                                        )
                                    }
                                }

                                if (vinculoExistente != null) {
                                    Text(
                                        text = "Papel no documento:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulPrimario
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PapelSignatario.entries.forEach { papelOpcao ->
                                            FilterChip(
                                                selected = vinculoExistente.papel == papelOpcao,
                                                onClick = {
                                                    val signatariosAtualizados = rascunhoAtual.signatarios.map {
                                                        if (it.signatarioId == signatario.id) {
                                                            it.copy(papel = papelOpcao)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                    val camposAtualizados = rascunhoAtual.camposAssinatura.map {
                                                        if (it.signatarioId == signatario.id) {
                                                            it.copy(papelSignatario = papelOpcao)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                    aoAtualizarRascunho(
                                                        rascunhoAtual.copy(
                                                            signatarios = signatariosAtualizados,
                                                            camposAssinatura = camposAtualizados
                                                        )
                                                    )
                                                },
                                                label = { Text(papelOpcao.descricao) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (erroFormulario != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VermelhoClaroFundo, RoundedCornerShape(10.dp))
                        .border(1.dp, VermelhoErro.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = erroFormulario!!,
                        color = VermelhoErro,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = {
                    when {
                        rascunhoAtual.titulo.trim().length < 3 -> {
                            erroFormulario = "Informe o título do documento (mínimo de 3 caracteres)."
                        }
                        rascunhoAtual.caminhoArquivo.isBlank() || rascunhoAtual.totalPaginas <= 0 -> {
                            erroFormulario = "Selecione um arquivo PDF válido antes de avançar."
                        }
                        rascunhoAtual.signatarios.isEmpty() -> {
                            erroFormulario = "Selecione pelo menos 1 signatário obrigatório para o documento."
                        }
                        else -> {
                            erroFormulario = null
                            aoAvancarParaPosicionamento()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "AVANÇAR PARA POSICIONAMENTO NO PDF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
