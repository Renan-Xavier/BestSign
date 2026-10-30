package br.com.fiap.bestsign.modelo

enum class PerfilUsuario(val descricao: String) {
    Admin("Administrador"),
    Signatario("Signatário")
}

enum class PapelSignatario(val descricao: String) {
    Contratante("Contratante"),
    Contratado("Contratado"),
    Testemunha("Testemunha");

    companion object {
        fun deTexto(valor: String): PapelSignatario {
            return entries.find { it.name.equals(valor, ignoreCase = true) } ?: Contratante
        }
    }
}

enum class StatusDocumento(val descricao: String) {
    Pendente("Pendente"),
    ParcialmenteAssinado("Parcialmente Assinado"),
    Concluido("Concluído");

    companion object {
        fun deTexto(valor: String): StatusDocumento {
            return entries.find { it.name.equals(valor, ignoreCase = true) } ?: Pendente
        }
    }
}
