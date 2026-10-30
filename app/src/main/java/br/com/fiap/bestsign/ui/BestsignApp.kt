package br.com.fiap.bestsign.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.fiap.bestsign.modelo.PerfilUsuario
import br.com.fiap.bestsign.modelo.RascunhoDocumento
import br.com.fiap.bestsign.modelo.Usuario
import br.com.fiap.bestsign.repositorio.DocumentoRepositorio
import br.com.fiap.bestsign.repositorio.SignatarioRepositorio
import br.com.fiap.bestsign.ui.telas.TelaAssinaturaDocumento
import br.com.fiap.bestsign.ui.telas.TelaCadastroDocumento
import br.com.fiap.bestsign.ui.telas.TelaDetalhesDocumento
import br.com.fiap.bestsign.ui.telas.TelaGerenciarSignatarios
import br.com.fiap.bestsign.ui.telas.TelaLogin
import br.com.fiap.bestsign.ui.telas.TelaPainelAdmin
import br.com.fiap.bestsign.ui.telas.TelaPainelSignatario
import br.com.fiap.bestsign.ui.telas.TelaPosicionamentoCampos
import br.com.fiap.bestsign.ui.telas.TelaRevisaoEnvio

object RotasBestsign {
    const val LOGIN = "login"
    const val PAINEL_ADMIN = "painel_admin"
    const val PAINEL_SIGNATARIO = "painel_signatario"
    const val GERENCIAR_SIGNATARIOS = "gerenciar_signatarios"
    const val CADASTRO_DOCUMENTO = "cadastro_documento"
    const val POSICIONAMENTO_CAMPOS = "posicionamento_campos"
    const val REVISAO_ENVIO = "revisao_envio"
    const val DETALHES_DOCUMENTO = "detalhes_documento/{documentoId}"
    const val ASSINATURA_DOCUMENTO = "assinatura_documento/{documentoId}"

    fun rotaDetalhes(documentoId: Long): String = "detalhes_documento/$documentoId"
    fun rotaAssinatura(documentoId: Long): String = "assinatura_documento/$documentoId"
}

@Composable
fun BestsignApp() {
    val contexto = LocalContext.current
    val navController = rememberNavController()

    val signatarioRepositorio = remember { SignatarioRepositorio(contexto) }
    val documentoRepositorio = remember { DocumentoRepositorio(contexto) }

    var usuarioLogado by remember { mutableStateOf<Usuario?>(null) }
    var rascunhoDocumento by remember { mutableStateOf(RascunhoDocumento()) }

    LaunchedEffect(Unit) {
        documentoRepositorio.garantirDocumentoDemonstracaoInicial()
    }

    NavHost(
        navController = navController,
        startDestination = RotasBestsign.LOGIN
    ) {
        composable(RotasBestsign.LOGIN) {
            TelaLogin(
                signatarioRepositorio = signatarioRepositorio,
                aoAutenticarComSucesso = { usuario ->
                    usuarioLogado = usuario
                    val destino = if (usuario.perfil == PerfilUsuario.Admin) {
                        RotasBestsign.PAINEL_ADMIN
                    } else {
                        RotasBestsign.PAINEL_SIGNATARIO
                    }
                    navController.navigate(destino) {
                        popUpTo(RotasBestsign.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(RotasBestsign.PAINEL_ADMIN) {
            val usuario = usuarioLogado
            if (usuario != null && usuario.perfil == PerfilUsuario.Admin) {
                TelaPainelAdmin(
                    usuarioLogado = usuario,
                    documentoRepositorio = documentoRepositorio,
                    aoIniciarNovoDocumento = {
                        rascunhoDocumento = RascunhoDocumento()
                        navController.navigate(RotasBestsign.CADASTRO_DOCUMENTO)
                    },
                    aoAbrirGerenciarSignatarios = {
                        navController.navigate(RotasBestsign.GERENCIAR_SIGNATARIOS)
                    },
                    aoAbrirDetalhesDocumento = { idDoc ->
                        navController.navigate(RotasBestsign.rotaDetalhes(idDoc))
                    },
                    aoSair = {
                        usuarioLogado = null
                        navController.navigate(RotasBestsign.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(RotasBestsign.GERENCIAR_SIGNATARIOS) {
            TelaGerenciarSignatarios(
                signatarioRepositorio = signatarioRepositorio,
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(RotasBestsign.CADASTRO_DOCUMENTO) {
            TelaCadastroDocumento(
                rascunhoAtual = rascunhoDocumento,
                documentoRepositorio = documentoRepositorio,
                signatarioRepositorio = signatarioRepositorio,
                aoAtualizarRascunho = { rascunhoDocumento = it },
                aoAbrirCadastroSignatarios = {
                    navController.navigate(RotasBestsign.GERENCIAR_SIGNATARIOS)
                },
                aoAvancarParaPosicionamento = {
                    navController.navigate(RotasBestsign.POSICIONAMENTO_CAMPOS)
                },
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(RotasBestsign.POSICIONAMENTO_CAMPOS) {
            TelaPosicionamentoCampos(
                rascunhoAtual = rascunhoDocumento,
                documentoRepositorio = documentoRepositorio,
                aoAtualizarRascunho = { rascunhoDocumento = it },
                aoAvancarParaRevisao = {
                    navController.navigate(RotasBestsign.REVISAO_ENVIO)
                },
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(RotasBestsign.REVISAO_ENVIO) {
            val usuario = usuarioLogado
            if (usuario != null) {
                TelaRevisaoEnvio(
                    usuarioLogado = usuario,
                    rascunhoAtual = rascunhoDocumento,
                    documentoRepositorio = documentoRepositorio,
                    signatarioRepositorio = signatarioRepositorio,
                    aoAtualizarRascunho = { rascunhoDocumento = it },
                    aoConfirmarEnvioComSucesso = { idDocumentoSalvo ->
                        rascunhoDocumento = RascunhoDocumento()
                        navController.navigate(RotasBestsign.rotaDetalhes(idDocumentoSalvo)) {
                            popUpTo(RotasBestsign.PAINEL_ADMIN) { inclusive = false }
                        }
                    },
                    aoVoltar = { navController.popBackStack() }
                )
            }
        }

        composable(RotasBestsign.PAINEL_SIGNATARIO) {
            val usuario = usuarioLogado
            if (usuario != null && usuario.perfil == PerfilUsuario.Signatario) {
                TelaPainelSignatario(
                    usuarioLogado = usuario,
                    documentoRepositorio = documentoRepositorio,
                    aoAbrirAssinatura = { idDoc ->
                        navController.navigate(RotasBestsign.rotaAssinatura(idDoc))
                    },
                    aoAbrirDetalhes = { idDoc ->
                        navController.navigate(RotasBestsign.rotaDetalhes(idDoc))
                    },
                    aoSair = {
                        usuarioLogado = null
                        navController.navigate(RotasBestsign.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = RotasBestsign.ASSINATURA_DOCUMENTO,
            arguments = listOf(navArgument("documentoId") { type = NavType.LongType })
        ) { entrada ->
            val usuario = usuarioLogado
            val idDoc = entrada.arguments?.getLong("documentoId") ?: 0L
            if (usuario != null && idDoc > 0L) {
                TelaAssinaturaDocumento(
                    documentoId = idDoc,
                    usuarioLogado = usuario,
                    documentoRepositorio = documentoRepositorio,
                    aoFinalizarAssinatura = { idAssinado ->
                        navController.navigate(RotasBestsign.rotaDetalhes(idAssinado)) {
                            popUpTo(RotasBestsign.PAINEL_SIGNATARIO) { inclusive = false }
                        }
                    },
                    aoVoltar = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = RotasBestsign.DETALHES_DOCUMENTO,
            arguments = listOf(navArgument("documentoId") { type = NavType.LongType })
        ) { entrada ->
            val usuario = usuarioLogado
            val idDoc = entrada.arguments?.getLong("documentoId") ?: 0L
            if (usuario != null && idDoc > 0L) {
                TelaDetalhesDocumento(
                    documentoId = idDoc,
                    usuarioLogado = usuario,
                    documentoRepositorio = documentoRepositorio,
                    aoIrParaAssinatura = { idParaAssinar ->
                        navController.navigate(RotasBestsign.rotaAssinatura(idParaAssinar))
                    },
                    aoVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}
