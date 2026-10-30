package br.com.fiap.bestsign.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.PapelSignatario
import br.com.fiap.bestsign.modelo.StatusDocumento
import br.com.fiap.bestsign.ui.tema.AmareloClaroFundo
import br.com.fiap.bestsign.ui.tema.AmareloPendente
import br.com.fiap.bestsign.ui.tema.AzulClaroFundo
import br.com.fiap.bestsign.ui.tema.AzulParcial
import br.com.fiap.bestsign.ui.tema.AzulParcialFundo
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiorBestsign(
    titulo: String,
    aoVoltar: (() -> Unit)? = null,
    acoes: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        navigationIcon = {
            if (aoVoltar != null) {
                IconButton(onClick = aoVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
            }
        },
        actions = { acoes() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AzulPrimario,
            titleContentColor = Color.White
        )
    )
}

@Composable
fun EtiquetaStatusDocumento(status: StatusDocumento) {
    val (corTexto, corFundo) = when (status) {
        StatusDocumento.Pendente -> AmareloPendente to AmareloClaroFundo
        StatusDocumento.ParcialmenteAssinado -> AzulParcial to AzulParcialFundo
        StatusDocumento.Concluido -> VerdeConcluido to VerdeClaroFundo
    }

    Box(
        modifier = Modifier
            .background(corFundo, RoundedCornerShape(8.dp))
            .border(1.dp, corTexto.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.descricao,
            color = corTexto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EtiquetaPapelSignatario(papel: PapelSignatario) {
    Box(
        modifier = Modifier
            .background(AzulClaroFundo, RoundedCornerShape(6.dp))
            .border(1.dp, AzulPrimario.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = papel.descricao,
            color = AzulPrimario,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ControlesPaginacaoPdf(
    paginaAtual: Int,
    totalPaginas: Int,
    aoMudarPagina: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { if (paginaAtual > 1) aoMudarPagina(paginaAtual - 1) },
            enabled = paginaAtual > 1
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Página anterior"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Anterior")
        }

        Text(
            text = "Página $paginaAtual de $totalPaginas",
            style = MaterialTheme.typography.titleMedium,
            color = AzulPrimario
        )

        OutlinedButton(
            onClick = { if (paginaAtual < totalPaginas) aoMudarPagina(paginaAtual + 1) },
            enabled = paginaAtual < totalPaginas
        ) {
            Text("Próxima")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Próxima página"
            )
        }
    }
}
