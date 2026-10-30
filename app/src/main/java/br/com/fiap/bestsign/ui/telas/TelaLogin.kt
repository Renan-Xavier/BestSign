package br.com.fiap.bestsign.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.SignatarioRepositorio
import br.com.fiap.bestsign.ui.tema.AzulClaroFundo
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaBorda
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.VermelhoClaroFundo
import br.com.fiap.bestsign.ui.tema.VermelhoErro

@Composable
fun TelaLogin(
    signatarioRepositorio: SignatarioRepositorio,
    aoAutenticarComSucesso: (Usuario) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinzaFundoTela)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulPrimario, RoundedCornerShape(16.dp))
                .padding(vertical = 24.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BESTSIGN",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Assinatura Eletrônica e Gestão de Documentos PDF",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Acesso Individual Seguro",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPrimario
                )

                Text(
                    text = "Entre com seu e-mail e senha próprios para gerenciar ou assinar seus documentos.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        mensagemErro = null
                    },
                    label = { Text("E-mail de acesso") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = senha,
                    onValueChange = {
                        senha = it
                        mensagemErro = null
                    },
                    label = { Text("Senha") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (mensagemErro != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VermelhoClaroFundo, RoundedCornerShape(10.dp))
                            .border(1.dp, VermelhoErro.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = mensagemErro!!,
                            color = VermelhoErro,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = {
                        val usuarioAutenticado = signatarioRepositorio.autenticar(email, senha)
                        if (usuarioAutenticado != null) {
                            aoAutenticarComSucesso(usuarioAutenticado)
                        } else {
                            mensagemErro = "E-mail ou senha inválidos. Verifique os dados informados."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "ENTRAR NO APLICATIVO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AzulClaroFundo),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CinzaBorda, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Contas Pré-Cadastradas para Demonstração",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulPrimario
                )

                Text(
                    text = "Toque em um perfil abaixo para preencher e acessar rapidamente:",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedButton(
                    onClick = {
                        email = "admin@bestsign.com"
                        senha = "123456"
                        signatarioRepositorio.autenticar(email, senha)?.let(aoAutenticarComSucesso)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar como Admin (admin@bestsign.com)")
                }

                HorizontalDivider(color = CinzaBorda)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            email = "carlos@email.com"
                            senha = "123456"
                            signatarioRepositorio.autenticar(email, senha)?.let(aoAutenticarComSucesso)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Signatário 1\n(Carlos)", textAlign = TextAlign.Center, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            email = "mariana@email.com"
                            senha = "123456"
                            signatarioRepositorio.autenticar(email, senha)?.let(aoAutenticarComSucesso)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Signatário 2\n(Mariana)", textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
