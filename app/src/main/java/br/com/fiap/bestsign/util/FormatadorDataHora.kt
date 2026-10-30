package br.com.fiap.bestsign.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatadorDataHora {
    private val formatoPadrao = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("pt-BR"))

    fun agoraFormatado(): String {
        return formatoPadrao.format(Date())
    }
}
