package br.com.fiap.bestsign.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaCoresBestsign = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = AzulClaroFundo,
    onPrimaryContainer = AzulPrimario,
    secondary = AzulSecundario,
    onSecondary = Color.White,
    secondaryContainer = AzulParcialFundo,
    onSecondaryContainer = AzulParcial,
    background = CinzaFundoTela,
    onBackground = TextoEscuro,
    surface = Color.White,
    onSurface = TextoEscuro,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = CinzaTextoSecundario,
    error = VermelhoErro,
    onError = Color.White,
    errorContainer = VermelhoClaroFundo,
    onErrorContainer = VermelhoErro
)

@Composable
fun TemaBestsign(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaCoresBestsign,
        typography = TipografiaBestsign,
        content = conteudo
    )
}
