package br.com.fiap.bestsign.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.fiap.bestsign.modelo.Signatario
import br.com.fiap.bestsign.repositorio.SignatarioRepositorio
import br.com.fiap.bestsign.ui.componentes.BarraSuperiorBestsign
import br.com.fiap.bestsign.ui.tema.AzulPrimario
import br.com.fiap.bestsign.ui.tema.CinzaFundoTela
import br.com.fiap.bestsign.ui.tema.CinzaTextoSecundario
import br.com.fiap.bestsign.ui.tema.VerdeClaroFundo
import br.com.fiap.bestsign.ui.tema.VerdeConcluido
import br.com.fiap.bestsign.ui.tema.VermelhoClaroFundo
import br.com.fiap.bestsign.ui.tema.VermelhoErro

@Composable
fun TelaGerenciarSignatarios(
    signatarioRepositorio: SignatarioRepositorio,
    aoVoltar: () -> Unit
) {
    var signatarios by remember { mutableStateOf<List<Signatario>>(emptyList()) }
    var idEmEdicao by remember { mutableLongStateOf(0L) }
    var nomeCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    fun recarregarLista() {
        signatarios = signatarioRepositorio.listarSignatarios()
    }

    fun limparFormulario() {
        idEmEdicao = 0L
        nomeCompleto = ""
        email = ""
        telefone = ""
        senha = ""
    }

    LaunchedEffect(Unit) {
        recarregarLista()
    }

    Scaffold(
        topBar = {
            BarraSuperiorBestsign(
                titulo = "Cadastro de Signatários",
                aoVoltar = aoVoltar
            )
        }
    ) { espacamentoInterno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CinzaFundoTela)
                .padding(espacamentoInterno)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
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
                            text = if (idEmEdicao == 0L) "Novo Signatário" else "Editando Signatário #$idEmEdicao",
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulPrimario
                        )

                        Text(
                            text = "Cada signatário utiliza seu próprio e-mail e senha para autenticar e assinar documentos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinzaTextoSecundario
                        )

                        OutlinedTextField(
                            value = nomeCompleto,
                            onValueChange = {
                                nomeCompleto = it
                                mensagemErro = null
                                mensagemSucesso = null
                            },
                            label = { Text("Nome completo *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                mensagemErro = null
                                mensagemSucesso = null
                            },
                            label = { Text("E-mail (login do signatário) *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = telefone,
                            onValueChange = {
                                telefone = it
                                mensagemErro = null
                                mensagemSucesso = null
                            },
                            label = { Text("Telefone *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = senha,
                            onValueChange = {
                                senha = it
                                mensagemErro = null
                                mensagemSucesso = null
                            },
                            label = { Text("Senha de autenticação *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (mensagemErro != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VermelhoClaroFundo, RoundedCornerShape(8.dp))
                                    .border(1.dp, VermelhoErro.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = mensagemErro!!,
                                    color = VermelhoErro,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (mensagemSucesso != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VerdeClaroFundo, RoundedCornerShape(8.dp))
                                    .border(1.dp, VerdeConcluido.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = mensagemSucesso!!,
                                    color = VerdeConcluido,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (idEmEdicao != 0L) {
                                OutlinedButton(
                                    onClick = {
                                        limparFormulario()
                                        mensagemErro = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("Cancelar Edição")
                                }
                            }

                            Button(
                                onClick = {
                                    val resultado = signatarioRepositorio.salvarSignatario(
                                        id = idEmEdicao,
                                        nomeCompleto = nomeCompleto,
                                        email = email,
                                        telefone = telefone,
                                        senha = senha
                                    )
                                    resultado.onSuccess { salvo ->
                                        mensagemSucesso = if (idEmEdicao == 0L) {
                                            "Signatário ${salvo.nomeCompleto} cadastrado com sucesso."
                                        } else {
                                            "Dados de ${salvo.nomeCompleto} atualizados com sucesso."
                                        }
                                        mensagemErro = null
                                        limparFormulario()
                                        recarregarLista()
                                    }.onFailure { erro ->
                                        mensagemErro = erro.message ?: "Erro ao salvar signatário."
                                        mensagemSucesso = null
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = if (idEmEdicao == 0L) "Cadastrar Signatário" else "Salvar Alterações",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Signatários Cadastrados (${signatarios.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulPrimario,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(signatarios, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.nomeCompleto,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "E-mail: ${item.email}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinzaTextoSecundario
                            )
                            Text(
                                text = "Telefone: ${item.telefone} | Senha: ${item.senha}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinzaTextoSecundario
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                idEmEdicao = item.id
                                nomeCompleto = item.nomeCompleto
                                email = item.email
                                telefone = item.telefone
                                senha = item.senha
                                mensagemErro = null
                                mensagemSucesso = null
                            }
                        ) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
}
