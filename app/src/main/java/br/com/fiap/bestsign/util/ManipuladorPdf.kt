package br.com.fiap.bestsign.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.provider.OpenableColumns
import br.com.fiap.bestsign.modelo.CampoAssinatura
import br.com.fiap.bestsign.modelo.ResultadoValidacaoPdf
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.rendering.PDFRenderer
import java.io.File
import java.io.FileOutputStream

object ManipuladorPdf {

    private var inicializado = false

    fun inicializar(contexto: Context) {
        if (!inicializado) {
            PDFBoxResourceLoader.init(contexto.applicationContext)
            inicializado = true
        }
    }

    fun obterPastaDocumentos(contexto: Context): File {
        val pasta = File(contexto.filesDir, "documentos_pdf")
        if (!pasta.exists()) {
            pasta.mkdirs()
        }
        return pasta
    }

    fun validarArquivoPdf(contexto: Context, uri: Uri): ResultadoValidacaoPdf {
        inicializar(contexto)
        val nomeOriginal = extrairNomeArquivo(contexto, uri)
        if (!nomeOriginal.lowercase().endsWith(".pdf")) {
            return ResultadoValidacaoPdf(
                valido = false,
                mensagemErro = "O arquivo selecionado não possui extensão .pdf válida."
            )
        }

        val pastaDestino = obterPastaDocumentos(contexto)
        val nomeUnico = "doc_${System.currentTimeMillis()}_${limparNomeArquivo(nomeOriginal)}"
        val arquivoDestino = File(pastaDestino, nomeUnico)

        try {
            val streamEntrada = contexto.contentResolver.openInputStream(uri)
                ?: return ResultadoValidacaoPdf(
                    valido = false,
                    mensagemErro = "Não foi possível abrir o arquivo selecionado no armazenamento."
                )

            streamEntrada.use { entrada ->
                FileOutputStream(arquivoDestino).use { saida ->
                    entrada.copyTo(saida)
                }
            }
        } catch (e: Exception) {
            arquivoDestino.delete()
            return ResultadoValidacaoPdf(
                valido = false,
                mensagemErro = "Falha ao ler o arquivo do dispositivo: ${e.localizedMessage ?: "erro de leitura"}."
            )
        }

        return validarArquivoFisico(arquivoDestino, nomeOriginal)
    }

    fun validarArquivoFisico(arquivo: File, nomeExibicao: String): ResultadoValidacaoPdf {
        if (!arquivo.exists() || arquivo.length() < 8L) {
            arquivo.delete()
            return ResultadoValidacaoPdf(
                valido = false,
                mensagemErro = "O arquivo PDF selecionado está vazio ou não pôde ser encontrado."
            )
        }

        try {
            val cabecalho = ByteArray(5)
            arquivo.inputStream().use { it.read(cabecalho) }
            val assinaturaCabecalho = String(cabecalho, Charsets.US_ASCII)
            if (!assinaturaCabecalho.startsWith("%PDF-")) {
                arquivo.delete()
                return ResultadoValidacaoPdf(
                    valido = false,
                    mensagemErro = "Arquivo inválido: o cabeçalho do arquivo não corresponde a um documento PDF."
                )
            }

            PDDocument.load(arquivo).use { documento ->
                if (documento.isEncrypted) {
                    arquivo.delete()
                    return ResultadoValidacaoPdf(
                        valido = false,
                        mensagemErro = "Este PDF possui proteção por senha e não pode ser processado para assinatura."
                    )
                }

                val quantidadePaginas = documento.numberOfPages
                if (quantidadePaginas <= 0) {
                    arquivo.delete()
                    return ResultadoValidacaoPdf(
                        valido = false,
                        mensagemErro = "O arquivo PDF não possui páginas válidas para exibição."
                    )
                }

                val renderizador = PDFRenderer(documento)
                val imagemTeste = renderizador.renderImageWithDPI(0, 36f)
                if (imagemTeste.width <= 0 || imagemTeste.height <= 0) {
                    arquivo.delete()
                    return ResultadoValidacaoPdf(
                        valido = false,
                        mensagemErro = "Não foi possível renderizar o conteúdo visual do arquivo PDF."
                    )
                }

                return ResultadoValidacaoPdf(
                    valido = true,
                    mensagemErro = null,
                    nomeArquivo = nomeExibicao,
                    caminhoArquivo = arquivo.absolutePath,
                    totalPaginas = quantidadePaginas
                )
            }
        } catch (e: Exception) {
            arquivo.delete()
            return ResultadoValidacaoPdf(
                valido = false,
                mensagemErro = "O arquivo PDF está corrompido ou não pôde ser processado."
            )
        }
    }

    fun renderizarPagina(
        contexto: Context,
        caminhoArquivo: String,
        numeroPagina: Int,
        dpi: Float = 140f
    ): Bitmap? {
        inicializar(contexto)
        val arquivo = File(caminhoArquivo)
        if (!arquivo.exists()) return null

        return try {
            PDDocument.load(arquivo).use { documento ->
                val indice = (numeroPagina - 1).coerceIn(0, (documento.numberOfPages - 1).coerceAtLeast(0))
                val renderizador = PDFRenderer(documento)
                renderizador.renderImageWithDPI(indice, dpi)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun gravarAssinaturasNoPdf(
        contexto: Context,
        caminhoArquivo: String,
        camposParaGravar: List<CampoAssinatura>,
        bitmapAssinatura: Bitmap,
        dataHoraAssinatura: String,
        autenticadoPorDigital: Boolean = false
    ): Boolean {
        inicializar(contexto)
        val arquivoOriginal = File(caminhoArquivo)
        if (!arquivoOriginal.exists() || camposParaGravar.isEmpty()) return false

        val arquivoTemporario = File(arquivoOriginal.parentFile, "${arquivoOriginal.name}.tmp")

        return try {
            PDDocument.load(arquivoOriginal).use { documento ->
                camposParaGravar.forEach { campo ->
                    val indicePagina = (campo.pagina - 1).coerceIn(0, documento.numberOfPages - 1)
                    val paginaPdf = documento.getPage(indicePagina)
                    val caixaPagina = paginaPdf.mediaBox

                    val larguraPagina = caixaPagina.width
                    val alturaPagina = caixaPagina.height

                    val larguraLimitada = campo.larguraProporcional.coerceIn(0.08f, 0.90f)
                    val alturaLimitada = campo.alturaProporcional.coerceIn(0.04f, 0.40f)
                    val posXLimitada = campo.posicaoXProporcional.coerceIn(0f, 1f - larguraLimitada)
                    val posYLimitada = campo.posicaoYProporcional.coerceIn(0f, 1f - alturaLimitada)

                    val larguraPdf = larguraLimitada * larguraPagina
                    val alturaPdf = alturaLimitada * alturaPagina
                    val xPdf = caixaPagina.lowerLeftX + (posXLimitada * larguraPagina)
                    val yPdf = caixaPagina.lowerLeftY + alturaPagina - (posYLimitada * alturaPagina) - alturaPdf

                    val bitmapCarimbo = montarCarimboComAssinatura(
                        bitmapAssinatura = bitmapAssinatura,
                        nomeSignatario = campo.nomeSignatario,
                        papelSignatario = campo.papelSignatario.descricao,
                        dataHora = dataHoraAssinatura,
                        autenticadoPorDigital = autenticadoPorDigital
                    )

                    val objetoImagem = LosslessFactory.createFromImage(documento, bitmapCarimbo)

                    PDPageContentStream(
                        documento,
                        paginaPdf,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                    ).use { fluxo ->
                        fluxo.drawImage(objetoImagem, xPdf, yPdf, larguraPdf, alturaPdf)
                    }
                }

                documento.save(arquivoTemporario)
            }

            if (arquivoTemporario.exists() && arquivoTemporario.length() > 0) {
                arquivoTemporario.copyTo(arquivoOriginal, overwrite = true)
                arquivoTemporario.delete()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            arquivoTemporario.delete()
            false
        }
    }

    private fun montarCarimboComAssinatura(
        bitmapAssinatura: Bitmap,
        nomeSignatario: String,
        papelSignatario: String,
        dataHora: String,
        autenticadoPorDigital: Boolean
    ): Bitmap {
        val larguraTotal = 640
        val alturaTotal = 240
        val resultado = Bitmap.createBitmap(larguraTotal, alturaTotal, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultado)

        val pinturaFundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(245, 248, 250, 255)
            style = Paint.Style.FILL
        }
        val pinturaBorda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 86, 160)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val retangulo = RectF(3f, 3f, larguraTotal - 3f, alturaTotal - 3f)
        canvas.drawRoundRect(retangulo, 12f, 12f, pinturaFundo)
        canvas.drawRoundRect(retangulo, 12f, 12f, pinturaBorda)

        val areaAssinaturaDestino = Rect(20, 12, larguraTotal - 20, alturaTotal - 68)
        canvas.drawBitmap(bitmapAssinatura, null, areaAssinaturaDestino, Paint(Paint.FILTER_BITMAP_FLAG))

        val pinturaLinha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 195, 215)
            strokeWidth = 2f
        }
        canvas.drawLine(24f, alturaTotal - 62f, larguraTotal - 24f, alturaTotal - 62f, pinturaLinha)

        val pinturaTextoPrincipal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 44, 84)
            textSize = 21f
            isFakeBoldText = true
        }
        val pinturaTextoSecundario = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 85, 105)
            textSize = 17f
        }

        canvas.drawText(
            "Assinado por: $nomeSignatario ($papelSignatario)",
            24f,
            alturaTotal - 36f,
            pinturaTextoPrincipal
        )
        val textoRodape = if (autenticadoPorDigital) {
            "Data/Hora: $dataHora - Autenticado por Digital no Android"
        } else {
            "Data/Hora: $dataHora - Bestsign Assinatura Eletronica"
        }
        canvas.drawText(
            textoRodape,
            24f,
            alturaTotal - 13f,
            pinturaTextoSecundario
        )

        return resultado
    }

    fun gerarPdfContratoModelo(
        contexto: Context,
        tituloContrato: String,
        nomeExibicao: String = "contrato_prestacao_servicos.pdf"
    ): ResultadoValidacaoPdf {
        inicializar(contexto)
        val pasta = obterPastaDocumentos(contexto)
        val nomeFisico = "modelo_${System.currentTimeMillis()}_${limparNomeArquivo(nomeExibicao)}"
        val arquivo = File(pasta, nomeFisico)

        val tituloLimpo = removerAcentosParaFontePadrao(
            tituloContrato.ifBlank { "CONTRATO DE PRESTACAO DE SERVICOS TECNOLOGICOS" }
        )

        try {
            PDDocument().use { documento ->
                val pagina1 = PDPage(PDRectangle.A4)
                documento.addPage(pagina1)

                PDPageContentStream(documento, pagina1).use { fluxo ->
                    escreverCabecalhoPagina(fluxo, tituloLimpo.uppercase(), "PAGINA 1 DE 2")

                    val linhasPagina1 = listOf(
                        "1. IDENTIFICACAO DAS PARTES",
                        "Este instrumento particular regula a prestacao de servicos entre as partes",
                        "identificadas no quadro de signatarios vinculado a este fluxo no sistema Bestsign,",
                        "compreendendo os papeis de CONTRATANTE, CONTRATADO e TESTEMUNHA.",
                        "",
                        "2. OBJETO DO CONTRATO",
                        "Clausula 2.1. O presente contrato tem como objeto o fornecimento de servicos",
                        "especializados de tecnologia, suporte operacional e gestao documental digital,",
                        "conforme especificacoes tecnicas aprovadas entre as partes.",
                        "",
                        "3. OBRIGACOES E RESPONSABILIDADES",
                        "Clausula 3.1. O CONTRATADO compromete-se a executar os servicos com zelo,",
                        "qualidade tecnica, pontualidade e observancia das normas de seguranca da informacao.",
                        "Clausula 3.2. O CONTRATANTE compromete-se a fornecer as informacoes necessarias",
                        "e efetuar os pagamentos nas datas e condicoes estabelecidas neste instrumento.",
                        "",
                        "4. VIGENCIA E VALIDADE JURIDICA",
                        "Clausula 4.1. As partes reconhecem a validade juridica da assinatura eletronica",
                        "realizada por meio da plataforma Bestsign, mediante autenticacao individual",
                        "por e-mail e senha exclusivos de cada signatario.",
                        "",
                        "5. DISPOSICOES DE SEGURANCA E PRIVACIDADE",
                        "Clausula 5.1. Cada signatario possui acesso restrito exclusivamente aos seus",
                        "documentos e registra sua assinatura manual diretamente nas coordenadas",
                        "definidas para o seu papel neste documento."
                    )

                    escreverBlocoLinhas(fluxo, linhasPagina1, 690f)
                }

                val pagina2 = PDPage(PDRectangle.A4)
                documento.addPage(pagina2)

                PDPageContentStream(documento, pagina2).use { fluxo ->
                    escreverCabecalhoPagina(fluxo, tituloLimpo.uppercase(), "PAGINA 2 DE 2")

                    val linhasPagina2 = listOf(
                        "6. CONDICOES COMERCIAIS E AUDITORIA",
                        "Clausula 6.1. Todos os eventos de criacao, envio, visualizacao e assinatura",
                        "deste documento sao registrados com data e horario exatos no historico de",
                        "auditoria do aplicativo Bestsign.",
                        "",
                        "7. ENCERRAMENTO E FORO",
                        "Clausula 7.1. Uma vez enviado para assinatura, os termos deste documento e o",
                        "posicionamento dos campos ficam preservados e bloqueados contra alteracoes.",
                        "Clausula 7.2. O documento alcanca o status CONCLUIDO somente apos a coleta",
                        "de todas as assinaturas obrigatorias.",
                        "",
                        "8. QUADRO RESERVADO PARA ASSINATURAS ELETRONICAS",
                        "Utilize as areas abaixo na visualizacao do documento para posicionar ou",
                        "realizar as assinaturas do Contratante, Contratado e Testemunha:"
                    )

                    escreverBlocoLinhas(fluxo, linhasPagina2, 690f)
                }

                documento.save(arquivo)
            }
        } catch (e: Exception) {
            arquivo.delete()
            return ResultadoValidacaoPdf(
                valido = false,
                mensagemErro = "Erro ao gerar o documento PDF modelo: ${e.localizedMessage ?: "falha interna"}."
            )
        }

        return validarArquivoFisico(arquivo, nomeExibicao)
    }

    private fun escreverCabecalhoPagina(
        fluxo: PDPageContentStream,
        titulo: String,
        indicadorPagina: String
    ) {
        fluxo.beginText()
        fluxo.setFont(PDType1Font.HELVETICA_BOLD, 13f)
        fluxo.newLineAtOffset(50f, 780f)
        fluxo.showText("BESTSIGN - GESTAO E ASSINATURA ELETRONICA DE DOCUMENTOS")
        fluxo.endText()

        fluxo.beginText()
        fluxo.setFont(PDType1Font.HELVETICA, 9f)
        fluxo.newLineAtOffset(460f, 780f)
        fluxo.showText(indicadorPagina)
        fluxo.endText()

        fluxo.beginText()
        fluxo.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        fluxo.newLineAtOffset(50f, 745f)
        fluxo.showText(titulo.take(65))
        fluxo.endText()

        fluxo.setLineWidth(1f)
        fluxo.moveTo(50f, 732f)
        fluxo.lineTo(545f, 732f)
        fluxo.stroke()
    }

    private fun escreverBlocoLinhas(
        fluxo: PDPageContentStream,
        linhas: List<String>,
        yInicial: Float
    ) {
        var posicaoY = yInicial
        linhas.forEach { linha ->
            if (linha.isNotBlank()) {
                val ehTituloSecao = linha.first().isDigit()
                fluxo.beginText()
                fluxo.setFont(
                    if (ehTituloSecao) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA,
                    if (ehTituloSecao) 11f else 10.5f
                )
                fluxo.newLineAtOffset(50f, posicaoY)
                fluxo.showText(linha)
                fluxo.endText()
            }
            posicaoY -= 20f
        }
    }

    private fun removerAcentosParaFontePadrao(texto: String): String {
        val comAcento = "ÁÀÂÃÄáàâãäÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÕÖóòôõöÚÙÛÜúùûüÇçÑñ"
        val semAcento = "AAAAAaaaaaEEEEeeeeIIIIiiiiOOOOOoooooUUUUuuuuCcNn"
        val builder = StringBuilder(texto.length)
        for (caractere in texto) {
            val indice = comAcento.indexOf(caractere)
            if (indice >= 0) {
                builder.append(semAcento[indice])
            } else if (caractere.code in 32..126) {
                builder.append(caractere)
            } else {
                builder.append(' ')
            }
        }
        return builder.toString()
    }

    private fun extrairNomeArquivo(contexto: Context, uri: Uri): String {
        var nome = "documento_selecionado.pdf"
        try {
            contexto.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val indiceNome = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (indiceNome >= 0 && cursor.moveToFirst()) {
                    val valor = cursor.getString(indiceNome)
                    if (!valor.isNullOrBlank()) {
                        nome = valor
                    }
                }
            }
        } catch (_: Exception) {
        }
        return nome
    }

    private fun limparNomeArquivo(nome: String): String {
        return nome.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
