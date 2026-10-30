package br.com.fiap.bestsign.dados

import android.content.ContentValues
import br.com.fiap.bestsign.modelo.PerfilUsuario
import br.com.fiap.bestsign.modelo.Signatario
import br.com.fiap.bestsign.modelo.Usuario

class SignatarioDao(private val bancoDados: BancoDadosLocal) {

    fun autenticarUsuario(emailInformado: String, senhaInformada: String): Usuario? {
        val emailLimpo = emailInformado.trim().lowercase()
        val senhaLimpa = senhaInformada.trim()
        val db = bancoDados.readableDatabase

        db.rawQuery(
            """
            SELECT id, nome_completo, email, telefone, senha, perfil
            FROM ${BancoDadosLocal.TABELA_USUARIOS}
            WHERE LOWER(email) = ? AND senha = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(emailLimpo, senhaLimpa)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return Usuario(
                    id = cursor.getLong(0),
                    nomeCompleto = cursor.getString(1),
                    email = cursor.getString(2),
                    telefone = cursor.getString(3),
                    senha = cursor.getString(4),
                    perfil = PerfilUsuario.Admin,
                    signatarioId = null
                )
            }
        }

        db.rawQuery(
            """
            SELECT id, nome_completo, email, telefone, senha
            FROM ${BancoDadosLocal.TABELA_SIGNATARIOS}
            WHERE LOWER(email) = ? AND senha = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(emailLimpo, senhaLimpa)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val idSignatario = cursor.getLong(0)
                return Usuario(
                    id = idSignatario,
                    nomeCompleto = cursor.getString(1),
                    email = cursor.getString(2),
                    telefone = cursor.getString(3),
                    senha = cursor.getString(4),
                    perfil = PerfilUsuario.Signatario,
                    signatarioId = idSignatario
                )
            }
        }

        return null
    }

    fun listarTodos(): List<Signatario> {
        val lista = mutableListOf<Signatario>()
        val db = bancoDados.readableDatabase

        db.rawQuery(
            """
            SELECT id, nome_completo, email, telefone, senha
            FROM ${BancoDadosLocal.TABELA_SIGNATARIOS}
            ORDER BY nome_completo ASC
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Signatario(
                        id = cursor.getLong(0),
                        nomeCompleto = cursor.getString(1),
                        email = cursor.getString(2),
                        telefone = cursor.getString(3),
                        senha = cursor.getString(4)
                    )
                )
            }
        }

        return lista
    }

    fun buscarPorId(id: Long): Signatario? {
        val db = bancoDados.readableDatabase
        db.rawQuery(
            """
            SELECT id, nome_completo, email, telefone, senha
            FROM ${BancoDadosLocal.TABELA_SIGNATARIOS}
            WHERE id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(id.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return Signatario(
                    id = cursor.getLong(0),
                    nomeCompleto = cursor.getString(1),
                    email = cursor.getString(2),
                    telefone = cursor.getString(3),
                    senha = cursor.getString(4)
                )
            }
        }
        return null
    }

    fun emailJaExiste(email: String, ignorarId: Long = 0): Boolean {
        val emailLimpo = email.trim().lowercase()
        val db = bancoDados.readableDatabase

        db.rawQuery(
            """
            SELECT id FROM ${BancoDadosLocal.TABELA_SIGNATARIOS}
            WHERE LOWER(email) = ? AND id != ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(emailLimpo, ignorarId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) return true
        }

        db.rawQuery(
            """
            SELECT id FROM ${BancoDadosLocal.TABELA_USUARIOS}
            WHERE LOWER(email) = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(emailLimpo)
        ).use { cursor ->
            if (cursor.moveToFirst()) return true
        }

        return false
    }

    fun inserir(signatario: Signatario): Long {
        val db = bancoDados.writableDatabase
        val valores = ContentValues().apply {
            put("nome_completo", signatario.nomeCompleto.trim())
            put("email", signatario.email.trim().lowercase())
            put("telefone", signatario.telefone.trim())
            put("senha", signatario.senha.trim())
        }
        return db.insert(BancoDadosLocal.TABELA_SIGNATARIOS, null, valores)
    }

    fun atualizar(signatario: Signatario): Int {
        val db = bancoDados.writableDatabase
        val valores = ContentValues().apply {
            put("nome_completo", signatario.nomeCompleto.trim())
            put("email", signatario.email.trim().lowercase())
            put("telefone", signatario.telefone.trim())
            put("senha", signatario.senha.trim())
        }
        return db.update(
            BancoDadosLocal.TABELA_SIGNATARIOS,
            valores,
            "id = ?",
            arrayOf(signatario.id.toString())
        )
    }
}
