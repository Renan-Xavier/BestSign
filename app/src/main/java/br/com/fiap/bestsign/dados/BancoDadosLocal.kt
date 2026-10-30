package br.com.fiap.bestsign.dados

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.com.fiap.bestsign.modelo.PerfilUsuario

class BancoDadosLocal(contexto: Context) : SQLiteOpenHelper(
    contexto.applicationContext,
    NOME_BANCO,
    null,
    VERSAO_BANCO
) {

    override fun onConfigure(banco: SQLiteDatabase) {
        super.onConfigure(banco)
        banco.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(banco: SQLiteDatabase) {
        banco.execSQL(
            """
            CREATE TABLE $TABELA_USUARIOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome_completo TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                telefone TEXT NOT NULL,
                senha TEXT NOT NULL,
                perfil TEXT NOT NULL
            )
            """.trimIndent()
        )

        banco.execSQL(
            """
            CREATE TABLE $TABELA_SIGNATARIOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome_completo TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                telefone TEXT NOT NULL,
                senha TEXT NOT NULL
            )
            """.trimIndent()
        )

        banco.execSQL(
            """
            CREATE TABLE $TABELA_DOCUMENTOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chave_envio TEXT NOT NULL UNIQUE,
                titulo TEXT NOT NULL,
                nome_arquivo TEXT NOT NULL,
                caminho_arquivo TEXT NOT NULL,
                mensagem_convite TEXT NOT NULL,
                data_criacao TEXT NOT NULL,
                data_envio TEXT NOT NULL,
                status TEXT NOT NULL,
                total_paginas INTEGER NOT NULL
            )
            """.trimIndent()
        )

        banco.execSQL(
            """
            CREATE TABLE $TABELA_DOCUMENTO_SIGNATARIOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                documento_id INTEGER NOT NULL,
                signatario_id INTEGER NOT NULL,
                papel TEXT NOT NULL,
                obrigatorio INTEGER NOT NULL DEFAULT 1,
                assinado INTEGER NOT NULL DEFAULT 0,
                data_assinatura TEXT,
                FOREIGN KEY(documento_id) REFERENCES $TABELA_DOCUMENTOS(id) ON DELETE CASCADE,
                FOREIGN KEY(signatario_id) REFERENCES $TABELA_SIGNATARIOS(id) ON DELETE RESTRICT
            )
            """.trimIndent()
        )

        banco.execSQL(
            """
            CREATE TABLE $TABELA_CAMPOS_ASSINATURA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                documento_id INTEGER NOT NULL,
                signatario_id INTEGER NOT NULL,
                pagina INTEGER NOT NULL,
                posicao_x REAL NOT NULL,
                posicao_y REAL NOT NULL,
                largura REAL NOT NULL,
                altura REAL NOT NULL,
                assinado INTEGER NOT NULL DEFAULT 0,
                data_assinatura TEXT,
                FOREIGN KEY(documento_id) REFERENCES $TABELA_DOCUMENTOS(id) ON DELETE CASCADE,
                FOREIGN KEY(signatario_id) REFERENCES $TABELA_SIGNATARIOS(id) ON DELETE RESTRICT
            )
            """.trimIndent()
        )

        banco.execSQL(
            """
            CREATE TABLE $TABELA_HISTORICO_EVENTOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                documento_id INTEGER NOT NULL,
                descricao TEXT NOT NULL,
                data_hora TEXT NOT NULL,
                responsavel TEXT NOT NULL,
                FOREIGN KEY(documento_id) REFERENCES $TABELA_DOCUMENTOS(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        inserirDadosIniciais(banco)
    }

    override fun onUpgrade(banco: SQLiteDatabase, versaoAntiga: Int, versaoNova: Int) {
        banco.execSQL("DROP TABLE IF EXISTS $TABELA_HISTORICO_EVENTOS")
        banco.execSQL("DROP TABLE IF EXISTS $TABELA_CAMPOS_ASSINATURA")
        banco.execSQL("DROP TABLE IF EXISTS $TABELA_DOCUMENTO_SIGNATARIOS")
        banco.execSQL("DROP TABLE IF EXISTS $TABELA_DOCUMENTOS")
        banco.execSQL("DROP TABLE IF EXISTS $TABELA_SIGNATARIOS")
        banco.execSQL("DROP TABLE IF EXISTS $TABELA_USUARIOS")
        onCreate(banco)
    }

    private fun inserirDadosIniciais(banco: SQLiteDatabase) {
        val valoresAdmin = ContentValues().apply {
            put("nome_completo", "Renan Santa Rosa Xavier (Admin)")
            put("email", "admin@bestsign.com")
            put("telefone", "(11) 99999-0000")
            put("senha", "123456")
            put("perfil", PerfilUsuario.Admin.name)
        }
        banco.insert(TABELA_USUARIOS, null, valoresAdmin)

        val signatarioUm = ContentValues().apply {
            put("nome_completo", "Carlos Eduardo Silva")
            put("email", "carlos@email.com")
            put("telefone", "(11) 98765-4321")
            put("senha", "123456")
        }
        banco.insert(TABELA_SIGNATARIOS, null, signatarioUm)

        val signatarioDois = ContentValues().apply {
            put("nome_completo", "Mariana Costa Souza")
            put("email", "mariana@email.com")
            put("telefone", "(11) 91234-5678")
            put("senha", "123456")
        }
        banco.insert(TABELA_SIGNATARIOS, null, signatarioDois)
    }

    companion object {
        const val NOME_BANCO = "bestsign_dados.db"
        const val VERSAO_BANCO = 1

        const val TABELA_USUARIOS = "usuarios"
        const val TABELA_SIGNATARIOS = "signatarios"
        const val TABELA_DOCUMENTOS = "documentos"
        const val TABELA_DOCUMENTO_SIGNATARIOS = "documento_signatarios"
        const val TABELA_CAMPOS_ASSINATURA = "campos_assinatura"
        const val TABELA_HISTORICO_EVENTOS = "historico_eventos"

        @Volatile
        private var instancia: BancoDadosLocal? = null

        fun obterInstancia(contexto: Context): BancoDadosLocal {
            return instancia ?: synchronized(this) {
                instancia ?: BancoDadosLocal(contexto).also { instancia = it }
            }
        }
    }
}
