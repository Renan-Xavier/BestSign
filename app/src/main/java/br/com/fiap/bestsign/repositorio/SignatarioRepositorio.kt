package br.com.fiap.bestsign.repositorio

import android.content.Context
import android.util.Patterns
import br.com.fiap.bestsign.dados.BancoDadosLocal
import br.com.fiap.bestsign.dados.SignatarioDao
import br.com.fiap.bestsign.modelo.Signatario
import br.com.fiap.bestsign.modelo.Usuario

class SignatarioRepositorio(contexto: Context) {

    private val signatarioDao = SignatarioDao(BancoDadosLocal.obterInstancia(contexto))

    fun autenticar(email: String, senha: String): Usuario? {
        if (email.isBlank() || senha.isBlank()) return null
        return signatarioDao.autenticarUsuario(email, senha)
    }

    fun listarSignatarios(): List<Signatario> {
        return signatarioDao.listarTodos()
    }

    fun buscarPorId(id: Long): Signatario? {
        return signatarioDao.buscarPorId(id)
    }

    fun salvarSignatario(
        id: Long = 0,
        nomeCompleto: String,
        email: String,
        telefone: String,
        senha: String
    ): Result<Signatario> {
        val nomeLimpo = nomeCompleto.trim()
        val emailLimpo = email.trim().lowercase()
        val telefoneLimpo = telefone.trim()
        val senhaLimpa = senha.trim()

        if (nomeLimpo.length < 3) {
            return Result.failure(IllegalArgumentException("Informe o nome completo do signatário."))
        }
        if (emailLimpo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches()) {
            return Result.failure(IllegalArgumentException("Informe um endereço de e-mail válido."))
        }
        if (telefoneLimpo.length < 8) {
            return Result.failure(IllegalArgumentException("Informe um número de telefone válido."))
        }
        if (senhaLimpa.length < 4) {
            return Result.failure(IllegalArgumentException("A senha deve conter pelo menos 4 caracteres."))
        }
        if (signatarioDao.emailJaExiste(emailLimpo, ignorarId = id)) {
            return Result.failure(IllegalArgumentException("Este e-mail já está cadastrado para outro usuário."))
        }

        val dadosSignatario = Signatario(
            id = id,
            nomeCompleto = nomeLimpo,
            email = emailLimpo,
            telefone = telefoneLimpo,
            senha = senhaLimpa
        )

        return try {
            if (id == 0L) {
                val novoId = signatarioDao.inserir(dadosSignatario)
                if (novoId <= 0L) {
                    Result.failure(IllegalStateException("Não foi possível cadastrar o signatário no banco local."))
                } else {
                    Result.success(dadosSignatario.copy(id = novoId))
                }
            } else {
                val linhasAfetadas = signatarioDao.atualizar(dadosSignatario)
                if (linhasAfetadas <= 0) {
                    Result.failure(IllegalStateException("Não foi possível atualizar os dados do signatário."))
                } else {
                    Result.success(dadosSignatario)
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Erro ao gravar signatário: ${e.localizedMessage ?: "falha no banco"}."))
        }
    }
}
