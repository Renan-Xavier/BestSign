package br.com.fiap.bestsign.repositorio

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import br.com.fiap.bestsign.dados.BancoDadosLocal
import br.com.fiap.bestsign.dados.DocumentoDao
import br.com.fiap.bestsign.dados.SignatarioDao
import br.com.fiap.bestsign.modelo.CampoAssinatura
import br.com.fiap.bestsign.modelo.Documento
import br.com.fiap.bestsign.modelo.PapelSignatario
import br.com.fiap.bestsign.modelo.PerfilUsuario
import br.com.fiap.bestsign.modelo.RascunhoDocumento
import br.com.fiap.bestsign.modelo.ResultadoValidacaoPdf
import br.com.fiap.bestsign.modelo.SignatarioDocumento
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.util.AutenticadorBiometrico
import br.com.fiap.bestsign.util.FormatadorDataHora
import br.com.fiap.bestsign.util.ManipuladorPdf
import java.io.File

class DocumentoRepositorio(contexto: Context) {

    private val contextoApp = contexto.applicationContext
    private val banco = BancoDadosLocal.obterInstancia(contextoApp)
    private val documentoDao = DocumentoDao(banco)
    private val signatarioDao = SignatarioDao(banco)

    init {
        ManipuladorPdf.inicializar(contextoApp)
    }

    fun garantirDocumentoDemonstracaoInicial() {
        if (documentoDao.contarDocumentos() > 0) return

        val signatariosCadastrados = signatarioDao.listarTodos()
        if (signatariosCadastrados.size < 2) return

        val primeiroSignatario = signatariosCadastrados[0]
        val segundoSignatario = signatariosCadastrados[1]

        val tituloExemplo = "Contrato de Prestação de Serviços de Desenvolvimento"
        val validacao = ManipuladorPdf.gerarPdfContratoModelo(
            contexto = contextoApp,
            tituloContrato = tituloExemplo,
            nomeExibicao = "contrato_desenvolvimento_mvp.pdf"
        )

        if (!validacao.valido) return

        val rascunhoInicial = RascunhoDocumento(
            chaveUnicaEnvio = "documento-inicial-demonstracao",
            titulo = tituloExemplo,
            nomeArquivo = validacao.nomeArquivo,
            caminhoArquivo = validacao.caminhoArquivo,
            totalPaginas = validacao.totalPaginas,
            mensagemConvite = "Olá! Encaminhamos o Contrato de Prestação de Serviços para conferência e assinatura eletrônica. Verifique seu papel no documento e toque no quadro indicado na página 2 para assinar.",
            signatarios = listOf(
                SignatarioDocumento(
                    signatarioId = primeiroSignatario.id,
                    nomeCompleto = primeiroSignatario.nomeCompleto,
                    email = primeiroSignatario.email,
                    telefone = primeiroSignatario.telefone,
                    papel = PapelSignatario.Contratante,
                    obrigatorio = true
                ),
                SignatarioDocumento(
                    signatarioId = segundoSignatario.id,
                    nomeCompleto = segundoSignatario.nomeCompleto,
                    email = segundoSignatario.email,
                    telefone = segundoSignatario.telefone,
                    papel = PapelSignatario.Contratado,
                    obrigatorio = true
                )
            ),
            camposAssinatura = listOf(
                CampoAssinatura(
                    id = 1L,
                    signatarioId = primeiroSignatario.id,
                    nomeSignatario = primeiroSignatario.nomeCompleto,
                    papelSignatario = PapelSignatario.Contratante,
                    pagina = validacao.totalPaginas,
                    posicaoXProporcional = 0.08f,
                    posicaoYProporcional = 0.56f,
                    larguraProporcional = 0.40f,
                    alturaProporcional = 0.14f
                ),
                CampoAssinatura(
                    id = 2L,
                    signatarioId = segundoSignatario.id,
                    nomeSignatario = segundoSignatario.nomeCompleto,
                    papelSignatario = PapelSignatario.Contratado,
                    pagina = validacao.totalPaginas,
                    posicaoXProporcional = 0.52f,
                    posicaoYProporcional = 0.56f,
                    larguraProporcional = 0.40f,
                    alturaProporcional = 0.14f
                )
            )
        )

        documentoDao.salvarNovoDocumento(
            rascunho = rascunhoInicial,
            dataHoraAtual = FormatadorDataHora.agoraFormatado(),
            nomeResponsavel = "Renan Santa Rosa Xavier (Admin)"
        )
    }

    fun validarArquivoPdf(uri: Uri): ResultadoValidacaoPdf {
        return ManipuladorPdf.validarArquivoPdf(contextoApp, uri)
    }

    fun gerarArquivoPdfModelo(tituloInformado: String): ResultadoValidacaoPdf {
        val tituloBase = tituloInformado.trim().ifBlank { "Contrato de Prestação de Serviços" }
        return ManipuladorPdf.gerarPdfContratoModelo(
            contexto = contextoApp,
            tituloContrato = tituloBase,
            nomeExibicao = "contrato_modelo_bestsign.pdf"
        )
    }

    fun renderizarPaginaPdf(caminhoArquivo: String, numeroPagina: Int, dpi: Float = 140f): Bitmap? {
        return ManipuladorPdf.renderizarPagina(
            contexto = contextoApp,
            caminhoArquivo = caminhoArquivo,
            numeroPagina = numeroPagina,
            dpi = dpi
        )
    }

    fun validarRascunhoAntesDoEnvio(rascunho: RascunhoDocumento): String? {
        if (rascunho.titulo.trim().length < 3) {
            return "Informe um título válido para o documento."
        }
        if (rascunho.caminhoArquivo.isBlank() || !File(rascunho.caminhoArquivo).exists()) {
            return "Selecione um arquivo PDF válido antes de enviar."
        }
        if (rascunho.totalPaginas <= 0) {
            return "O arquivo PDF selecionado não possui páginas válidas."
        }
        val signatariosObrigatorios = rascunho.signatarios.filter { it.obrigatorio }
        if (signatariosObrigatorios.isEmpty()) {
            return "O documento precisa ter pelo menos 1 signatário obrigatório."
        }
        if (rascunho.camposAssinatura.isEmpty()) {
            return "Posicione pelo menos 1 campo de assinatura no PDF."
        }
        val idsSignatariosDocumento = rascunho.signatarios.map { it.signatarioId }.toSet()
        val campoSemSignatarioValido = rascunho.camposAssinatura.any { it.signatarioId !in idsSignatariosDocumento }
        if (campoSemSignatarioValido) {
            return "Todos os campos de assinatura devem estar associados a um signatário do documento."
        }
        val signatarioSemCampo = rascunho.signatarios.firstOrNull { signatario ->
            rascunho.camposAssinatura.none { campo -> campo.signatarioId == signatario.signatarioId }
        }
        if (signatarioSemCampo != null) {
            return "Posicione pelo menos um campo de assinatura no PDF para ${signatarioSemCampo.nomeCompleto}."
        }
        val campoForaDosLimites = rascunho.camposAssinatura.any { campo ->
            campo.pagina < 1 ||
                campo.pagina > rascunho.totalPaginas ||
                campo.posicaoXProporcional < 0f ||
                campo.posicaoYProporcional < 0f ||
                campo.larguraProporcional <= 0f ||
                campo.alturaProporcional <= 0f ||
                (campo.posicaoXProporcional + campo.larguraProporcional) > 1.001f ||
                (campo.posicaoYProporcional + campo.alturaProporcional) > 1.001f
        }
        if (campoForaDosLimites) {
            return "Existem campos de assinatura fora dos limites da página do PDF."
        }
        if (rascunho.mensagemConvite.trim().length < 5) {
            return "Digite uma mensagem de convite para orientar os signatários."
        }
        return null
    }

    fun salvarDocumento(rascunho: RascunhoDocumento, nomeResponsavel: String): Result<Long> {
        val erroValidacao = validarRascunhoAntesDoEnvio(rascunho)
        if (erroValidacao != null) {
            return Result.failure(IllegalArgumentException(erroValidacao))
        }

        val validacaoFisica = ManipuladorPdf.validarArquivoFisico(
            arquivo = File(rascunho.caminhoArquivo),
            nomeExibicao = rascunho.nomeArquivo
        )
        if (!validacaoFisica.valido) {
            return Result.failure(
                IllegalStateException(validacaoFisica.mensagemErro ?: "O arquivo PDF não pôde ser validado.")
            )
        }

        return try {
            val idGerado = documentoDao.salvarNovoDocumento(
                rascunho = rascunho,
                dataHoraAtual = FormatadorDataHora.agoraFormatado(),
                nomeResponsavel = nomeResponsavel
            )
            if (idGerado <= 0L) {
                Result.failure(IllegalStateException("Não foi possível registrar o envio do documento."))
            } else {
                Result.success(idGerado)
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Falha ao enviar o documento: ${e.localizedMessage ?: "tente novamente"}.")
            )
        }
    }

    fun listarDocumentosParaAdmin(): List<Documento> {
        return documentoDao.listarTodosDocumentos()
    }

    fun listarDocumentosParaSignatario(signatarioId: Long): List<Documento> {
        return documentoDao.listarDocumentosPorSignatario(signatarioId)
    }

    fun buscarDocumentoPorIdComSeguranca(documentoId: Long, usuarioLogado: Usuario): Documento? {
        val documento = documentoDao.buscarDocumentoPorId(documentoId) ?: return null
        if (usuarioLogado.perfil == PerfilUsuario.Admin) {
            return documento
        }
        val idSignatarioLogado = usuarioLogado.signatarioId ?: usuarioLogado.id
        val pertenceAoSignatario = documento.signatarios.any { it.signatarioId == idSignatarioLogado }
        return if (pertenceAoSignatario) documento else null
    }

    fun assinarDocumentoComDigital(
        documentoId: Long,
        usuarioLogado: Usuario,
        idsCamposSelecionados: List<Long>
    ): Result<Documento> {
        val bitmapPorExtenso = AutenticadorBiometrico.gerarBitmapAssinaturaPorExtenso(
            nomeCompleto = usuarioLogado.nomeCompleto
        )
        return assinarDocumento(
            documentoId = documentoId,
            usuarioLogado = usuarioLogado,
            idsCamposSelecionados = idsCamposSelecionados,
            bitmapAssinatura = bitmapPorExtenso,
            autenticadoPorDigital = true
        )
    }

    fun assinarDocumento(
        documentoId: Long,
        usuarioLogado: Usuario,
        idsCamposSelecionados: List<Long>,
        bitmapAssinatura: Bitmap,
        autenticadoPorDigital: Boolean = false
    ): Result<Documento> {
        val idSignatario = usuarioLogado.signatarioId ?: usuarioLogado.id
        val documentoAtual = buscarDocumentoPorIdComSeguranca(documentoId, usuarioLogado)
            ?: return Result.failure(IllegalAccessException("Documento não encontrado ou acesso não autorizado."))

        val camposPendentesDoUsuario = documentoAtual.camposAssinatura.filter {
            it.signatarioId == idSignatario && !it.assinado && it.id in idsCamposSelecionados
        }

        if (camposPendentesDoUsuario.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nenhum campo pendente selecionado para assinar."))
        }

        val dataHoraAssinatura = FormatadorDataHora.agoraFormatado()

        val gravouNoPdf = ManipuladorPdf.gravarAssinaturasNoPdf(
            contexto = contextoApp,
            caminhoArquivo = documentoAtual.caminhoArquivo,
            camposParaGravar = camposPendentesDoUsuario,
            bitmapAssinatura = bitmapAssinatura,
            dataHoraAssinatura = dataHoraAssinatura,
            autenticadoPorDigital = autenticadoPorDigital
        )

        if (!gravouNoPdf) {
            return Result.failure(
                IllegalStateException("Não foi possível gravar o desenho da assinatura no arquivo PDF.")
            )
        }

        return try {
            documentoDao.registrarAssinaturaNosCampos(
                documentoId = documentoId,
                signatarioId = idSignatario,
                idsCamposAssinados = camposPendentesDoUsuario.map { it.id },
                dataHoraAssinatura = dataHoraAssinatura,
                autenticadoPorDigital = autenticadoPorDigital
            )

            val documentoAtualizado = buscarDocumentoPorIdComSeguranca(documentoId, usuarioLogado)
                ?: return Result.failure(IllegalStateException("Erro ao recarregar o documento assinado."))

            Result.success(documentoAtualizado)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Erro ao atualizar status da assinatura: ${e.localizedMessage ?: "falha no banco"}.")
            )
        }
    }
}
