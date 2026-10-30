package br.com.fiap.bestsign.modelo

data class Usuario(
    val id: Long = 0,
    val nomeCompleto: String,
    val email: String,
    val telefone: String,
    val senha: String,
    val perfil: PerfilUsuario,
    val signatarioId: Long? = null
)

data class Signatario(
    val id: Long = 0,
    val nomeCompleto: String,
    val email: String,
    val telefone: String,
    val senha: String
)

data class SignatarioDocumento(
    val id: Long = 0,
    val documentoId: Long = 0,
    val signatarioId: Long,
    val nomeCompleto: String,
    val email: String,
    val telefone: String,
    val papel: PapelSignatario,
    val obrigatorio: Boolean = true,
    val assinado: Boolean = false,
    val dataAssinatura: String? = null
)

data class CampoAssinatura(
    val id: Long = 0,
    val documentoId: Long = 0,
    val signatarioId: Long,
    val nomeSignatario: String,
    val papelSignatario: PapelSignatario,
    val pagina: Int,
    val posicaoXProporcional: Float,
    val posicaoYProporcional: Float,
    val larguraProporcional: Float,
    val alturaProporcional: Float,
    val assinado: Boolean = false,
    val dataAssinatura: String? = null
)

data class HistoricoEvento(
    val id: Long = 0,
    val documentoId: Long = 0,
    val descricao: String,
    val dataHora: String,
    val responsavel: String
)

data class Documento(
    val id: Long = 0,
    val titulo: String,
    val nomeArquivo: String,
    val caminhoArquivo: String,
    val mensagemConvite: String,
    val dataCriacao: String,
    val dataEnvio: String,
    val status: StatusDocumento,
    val totalPaginas: Int,
    val signatarios: List<SignatarioDocumento> = emptyList(),
    val camposAssinatura: List<CampoAssinatura> = emptyList(),
    val historico: List<HistoricoEvento> = emptyList()
)

data class RascunhoDocumento(
    val chaveUnicaEnvio: String = java.util.UUID.randomUUID().toString(),
    val titulo: String = "",
    val nomeArquivo: String = "",
    val caminhoArquivo: String = "",
    val totalPaginas: Int = 0,
    val signatarios: List<SignatarioDocumento> = emptyList(),
    val camposAssinatura: List<CampoAssinatura> = emptyList(),
    val mensagemConvite: String = "Solicitamos sua assinatura eletrônica neste documento. Revise as cláusulas e toque no campo destacado para assinar."
)

data class ResultadoValidacaoPdf(
    val valido: Boolean,
    val mensagemErro: String? = null,
    val nomeArquivo: String = "",
    val caminhoArquivo: String = "",
    val totalPaginas: Int = 0
)
