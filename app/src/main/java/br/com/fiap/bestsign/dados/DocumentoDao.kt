package br.com.fiap.bestsign.dados

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import br.com.fiap.bestsign.modelo.CampoAssinatura
import br.com.fiap.bestsign.modelo.Documento
import br.com.fiap.bestsign.modelo.HistoricoEvento
import br.com.fiap.bestsign.modelo.PapelSignatario
import br.com.fiap.bestsign.modelo.RascunhoDocumento
import br.com.fiap.bestsign.modelo.SignatarioDocumento
import br.com.fiap.bestsign.modelo.StatusDocumento

class DocumentoDao(private val bancoDados: BancoDadosLocal) {

    fun contarDocumentos(): Int {
        val db = bancoDados.readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM ${BancoDadosLocal.TABELA_DOCUMENTOS}", null).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    fun buscarIdPorChaveEnvio(chaveEnvio: String): Long? {
        val db = bancoDados.readableDatabase
        db.rawQuery(
            "SELECT id FROM ${BancoDadosLocal.TABELA_DOCUMENTOS} WHERE chave_envio = ? LIMIT 1",
            arrayOf(chaveEnvio)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return null
    }

    fun salvarNovoDocumento(
        rascunho: RascunhoDocumento,
        dataHoraAtual: String,
        nomeResponsavel: String
    ): Long {
        val idExistente = buscarIdPorChaveEnvio(rascunho.chaveUnicaEnvio)
        if (idExistente != null && idExistente > 0) {
            return idExistente
        }

        val db = bancoDados.writableDatabase
        db.beginTransaction()
        try {
            val valoresDocumento = ContentValues().apply {
                put("chave_envio", rascunho.chaveUnicaEnvio)
                put("titulo", rascunho.titulo.trim())
                put("nome_arquivo", rascunho.nomeArquivo)
                put("caminho_arquivo", rascunho.caminhoArquivo)
                put("mensagem_convite", rascunho.mensagemConvite.trim())
                put("data_criacao", dataHoraAtual)
                put("data_envio", dataHoraAtual)
                put("status", StatusDocumento.Pendente.name)
                put("total_paginas", rascunho.totalPaginas)
            }

            val idDocumento = db.insertOrThrow(
                BancoDadosLocal.TABELA_DOCUMENTOS,
                null,
                valoresDocumento
            )

            rascunho.signatarios.forEach { signatario ->
                val valoresSignatario = ContentValues().apply {
                    put("documento_id", idDocumento)
                    put("signatario_id", signatario.signatarioId)
                    put("papel", signatario.papel.name)
                    put("obrigatorio", if (signatario.obrigatorio) 1 else 0)
                    put("assinado", 0)
                    putNull("data_assinatura")
                }
                db.insertOrThrow(
                    BancoDadosLocal.TABELA_DOCUMENTO_SIGNATARIOS,
                    null,
                    valoresSignatario
                )
            }

            rascunho.camposAssinatura.forEach { campo ->
                val larguraLimitada = campo.larguraProporcional.coerceIn(0.08f, 0.90f)
                val alturaLimitada = campo.alturaProporcional.coerceIn(0.04f, 0.40f)
                val posXLimitada = campo.posicaoXProporcional.coerceIn(0f, 1f - larguraLimitada)
                val posYLimitada = campo.posicaoYProporcional.coerceIn(0f, 1f - alturaLimitada)

                val valoresCampo = ContentValues().apply {
                    put("documento_id", idDocumento)
                    put("signatario_id", campo.signatarioId)
                    put("pagina", campo.pagina)
                    put("posicao_x", posXLimitada)
                    put("posicao_y", posYLimitada)
                    put("largura", larguraLimitada)
                    put("altura", alturaLimitada)
                    put("assinado", 0)
                    putNull("data_assinatura")
                }
                db.insertOrThrow(
                    BancoDadosLocal.TABELA_CAMPOS_ASSINATURA,
                    null,
                    valoresCampo
                )
            }

            inserirEventoInterno(
                db = db,
                documentoId = idDocumento,
                descricao = "Documento cadastrado com o arquivo ${rascunho.nomeArquivo} (${rascunho.totalPaginas} pagina(s)).",
                dataHora = dataHoraAtual,
                responsavel = nomeResponsavel
            )

            val resumoDestinatarios = rascunho.signatarios.joinToString(", ") {
                "${it.nomeCompleto} (${it.papel.descricao})"
            }
            inserirEventoInterno(
                db = db,
                documentoId = idDocumento,
                descricao = "Documento enviado para assinatura de: $resumoDestinatarios.",
                dataHora = dataHoraAtual,
                responsavel = nomeResponsavel
            )

            db.setTransactionSuccessful()
            return idDocumento
        } finally {
            db.endTransaction()
        }
    }

    fun listarTodosDocumentos(): List<Documento> {
        val db = bancoDados.readableDatabase
        val ids = mutableListOf<Long>()
        db.rawQuery(
            "SELECT id FROM ${BancoDadosLocal.TABELA_DOCUMENTOS} ORDER BY id DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0))
            }
        }
        return ids.mapNotNull { buscarDocumentoPorId(it) }
    }

    fun listarDocumentosPorSignatario(signatarioId: Long): List<Documento> {
        val db = bancoDados.readableDatabase
        val ids = mutableListOf<Long>()
        db.rawQuery(
            """
            SELECT DISTINCT d.id
            FROM ${BancoDadosLocal.TABELA_DOCUMENTOS} d
            INNER JOIN ${BancoDadosLocal.TABELA_DOCUMENTO_SIGNATARIOS} ds ON ds.documento_id = d.id
            WHERE ds.signatario_id = ?
            ORDER BY d.id DESC
            """.trimIndent(),
            arrayOf(signatarioId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0))
            }
        }
        return ids.mapNotNull { buscarDocumentoPorId(it) }
    }

    fun buscarDocumentoPorId(documentoId: Long): Documento? {
        val db = bancoDados.readableDatabase

        var titulo = ""
        var nomeArquivo = ""
        var caminhoArquivo = ""
        var mensagemConvite = ""
        var dataCriacao = ""
        var dataEnvio = ""
        var status = StatusDocumento.Pendente
        var totalPaginas = 1

        db.rawQuery(
            """
            SELECT id, titulo, nome_arquivo, caminho_arquivo, mensagem_convite,
                   data_criacao, data_envio, status, total_paginas
            FROM ${BancoDadosLocal.TABELA_DOCUMENTOS}
            WHERE id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(documentoId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }
            titulo = cursor.getString(1)
            nomeArquivo = cursor.getString(2)
            caminhoArquivo = cursor.getString(3)
            mensagemConvite = cursor.getString(4)
            dataCriacao = cursor.getString(5)
            dataEnvio = cursor.getString(6)
            status = StatusDocumento.deTexto(cursor.getString(7))
            totalPaginas = cursor.getInt(8)
        }

        val signatarios = listarSignatariosDoDocumento(db, documentoId)
        val mapaPapelPorSignatario = signatarios.associate { it.signatarioId to it.papel }
        val camposAssinatura = listarCamposDoDocumento(db, documentoId, mapaPapelPorSignatario)
        val historico = listarHistoricoDoDocumento(db, documentoId)

        return Documento(
            id = documentoId,
            titulo = titulo,
            nomeArquivo = nomeArquivo,
            caminhoArquivo = caminhoArquivo,
            mensagemConvite = mensagemConvite,
            dataCriacao = dataCriacao,
            dataEnvio = dataEnvio,
            status = status,
            totalPaginas = totalPaginas,
            signatarios = signatarios,
            camposAssinatura = camposAssinatura,
            historico = historico
        )
    }

    fun registrarAssinaturaNosCampos(
        documentoId: Long,
        signatarioId: Long,
        idsCamposAssinados: List<Long>,
        dataHoraAssinatura: String,
        autenticadoPorDigital: Boolean = false
    ): StatusDocumento {
        val db = bancoDados.writableDatabase
        db.beginTransaction()
        try {
            idsCamposAssinados.forEach { campoId ->
                val valoresCampo = ContentValues().apply {
                    put("assinado", 1)
                    put("data_assinatura", dataHoraAssinatura)
                }
                db.update(
                    BancoDadosLocal.TABELA_CAMPOS_ASSINATURA,
                    valoresCampo,
                    "id = ? AND documento_id = ? AND signatario_id = ?",
                    arrayOf(campoId.toString(), documentoId.toString(), signatarioId.toString())
                )
            }

            var camposPendentesDoSignatario = 0
            db.rawQuery(
                """
                SELECT COUNT(*)
                FROM ${BancoDadosLocal.TABELA_CAMPOS_ASSINATURA}
                WHERE documento_id = ? AND signatario_id = ? AND assinado = 0
                """.trimIndent(),
                arrayOf(documentoId.toString(), signatarioId.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    camposPendentesDoSignatario = cursor.getInt(0)
                }
            }

            var nomeSignatario = "Signatário"
            var papelSignatario = PapelSignatario.Contratante

            db.rawQuery(
                """
                SELECT s.nome_completo, ds.papel
                FROM ${BancoDadosLocal.TABELA_DOCUMENTO_SIGNATARIOS} ds
                INNER JOIN ${BancoDadosLocal.TABELA_SIGNATARIOS} s ON s.id = ds.signatario_id
                WHERE ds.documento_id = ? AND ds.signatario_id = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(documentoId.toString(), signatarioId.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    nomeSignatario = cursor.getString(0)
                    papelSignatario = PapelSignatario.deTexto(cursor.getString(1))
                }
            }

            val complementoModo = if (autenticadoPorDigital) {
                " com autenticação de digital do celular e preenchimento por extenso"
            } else {
                ""
            }

            if (camposPendentesDoSignatario == 0) {
                val valoresVinculo = ContentValues().apply {
                    put("assinado", 1)
                    put("data_assinatura", dataHoraAssinatura)
                }
                db.update(
                    BancoDadosLocal.TABELA_DOCUMENTO_SIGNATARIOS,
                    valoresVinculo,
                    "documento_id = ? AND signatario_id = ?",
                    arrayOf(documentoId.toString(), signatarioId.toString())
                )

                inserirEventoInterno(
                    db = db,
                    documentoId = documentoId,
                    descricao = "Assinatura eletrônica finalizada$complementoModo por $nomeSignatario (${papelSignatario.descricao}).",
                    dataHora = dataHoraAssinatura,
                    responsavel = nomeSignatario
                )
            } else {
                inserirEventoInterno(
                    db = db,
                    documentoId = documentoId,
                    descricao = "Campo de assinatura preenchido$complementoModo por $nomeSignatario (${papelSignatario.descricao}). Restam $camposPendentesDoSignatario campo(s) para este signatário.",
                    dataHora = dataHoraAssinatura,
                    responsavel = nomeSignatario
                )
            }

            var totalObrigatorios = 0
            var totalObrigatoriosAssinados = 0
            var algumCampoAssinado = false

            db.rawQuery(
                """
                SELECT obrigatorio, assinado
                FROM ${BancoDadosLocal.TABELA_DOCUMENTO_SIGNATARIOS}
                WHERE documento_id = ?
                """.trimIndent(),
                arrayOf(documentoId.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val obrigatorio = cursor.getInt(0) == 1
                    val assinado = cursor.getInt(1) == 1
                    if (obrigatorio) {
                        totalObrigatorios++
                        if (assinado) {
                            totalObrigatoriosAssinados++
                        }
                    }
                    if (assinado) {
                        algumCampoAssinado = true
                    }
                }
            }

            if (!algumCampoAssinado) {
                db.rawQuery(
                    """
                    SELECT COUNT(*)
                    FROM ${BancoDadosLocal.TABELA_CAMPOS_ASSINATURA}
                    WHERE documento_id = ? AND assinado = 1
                    """.trimIndent(),
                    arrayOf(documentoId.toString())
                ).use { cursor ->
                    if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                        algumCampoAssinado = true
                    }
                }
            }

            val novoStatus = when {
                totalObrigatorios > 0 && totalObrigatoriosAssinados == totalObrigatorios -> StatusDocumento.Concluido
                algumCampoAssinado -> StatusDocumento.ParcialmenteAssinado
                else -> StatusDocumento.Pendente
            }

            val valoresStatusDoc = ContentValues().apply {
                put("status", novoStatus.name)
            }
            db.update(
                BancoDadosLocal.TABELA_DOCUMENTOS,
                valoresStatusDoc,
                "id = ?",
                arrayOf(documentoId.toString())
            )

            if (novoStatus == StatusDocumento.Concluido) {
                inserirEventoInterno(
                    db = db,
                    documentoId = documentoId,
                    descricao = "Todos os signatários obrigatórios assinaram. Documento marcado como Concluído.",
                    dataHora = dataHoraAssinatura,
                    responsavel = "Sistema Bestsign"
                )
            }

            db.setTransactionSuccessful()
            return novoStatus
        } finally {
            db.endTransaction()
        }
    }

    private fun listarSignatariosDoDocumento(
        db: SQLiteDatabase,
        documentoId: Long
    ): List<SignatarioDocumento> {
        val lista = mutableListOf<SignatarioDocumento>()
        db.rawQuery(
            """
            SELECT ds.id, ds.documento_id, ds.signatario_id, s.nome_completo, s.email, s.telefone,
                   ds.papel, ds.obrigatorio, ds.assinado, ds.data_assinatura
            FROM ${BancoDadosLocal.TABELA_DOCUMENTO_SIGNATARIOS} ds
            INNER JOIN ${BancoDadosLocal.TABELA_SIGNATARIOS} s ON s.id = ds.signatario_id
            WHERE ds.documento_id = ?
            ORDER BY ds.id ASC
            """.trimIndent(),
            arrayOf(documentoId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    SignatarioDocumento(
                        id = cursor.getLong(0),
                        documentoId = cursor.getLong(1),
                        signatarioId = cursor.getLong(2),
                        nomeCompleto = cursor.getString(3),
                        email = cursor.getString(4),
                        telefone = cursor.getString(5),
                        papel = PapelSignatario.deTexto(cursor.getString(6)),
                        obrigatorio = cursor.getInt(7) == 1,
                        assinado = cursor.getInt(8) == 1,
                        dataAssinatura = if (cursor.isNull(9)) null else cursor.getString(9)
                    )
                )
            }
        }
        return lista
    }

    private fun listarCamposDoDocumento(
        db: SQLiteDatabase,
        documentoId: Long,
        mapaPapelPorSignatario: Map<Long, PapelSignatario>
    ): List<CampoAssinatura> {
        val lista = mutableListOf<CampoAssinatura>()
        db.rawQuery(
            """
            SELECT c.id, c.documento_id, c.signatario_id, s.nome_completo, c.pagina,
                   c.posicao_x, c.posicao_y, c.largura, c.altura, c.assinado, c.data_assinatura
            FROM ${BancoDadosLocal.TABELA_CAMPOS_ASSINATURA} c
            INNER JOIN ${BancoDadosLocal.TABELA_SIGNATARIOS} s ON s.id = c.signatario_id
            WHERE c.documento_id = ?
            ORDER BY c.pagina ASC, c.id ASC
            """.trimIndent(),
            arrayOf(documentoId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val idSignatario = cursor.getLong(2)
                lista.add(
                    CampoAssinatura(
                        id = cursor.getLong(0),
                        documentoId = cursor.getLong(1),
                        signatarioId = idSignatario,
                        nomeSignatario = cursor.getString(3),
                        papelSignatario = mapaPapelPorSignatario[idSignatario] ?: PapelSignatario.Contratante,
                        pagina = cursor.getInt(4),
                        posicaoXProporcional = cursor.getFloat(5),
                        posicaoYProporcional = cursor.getFloat(6),
                        larguraProporcional = cursor.getFloat(7),
                        alturaProporcional = cursor.getFloat(8),
                        assinado = cursor.getInt(9) == 1,
                        dataAssinatura = if (cursor.isNull(10)) null else cursor.getString(10)
                    )
                )
            }
        }
        return lista
    }

    private fun listarHistoricoDoDocumento(
        db: SQLiteDatabase,
        documentoId: Long
    ): List<HistoricoEvento> {
        val lista = mutableListOf<HistoricoEvento>()
        db.rawQuery(
            """
            SELECT id, documento_id, descricao, data_hora, responsavel
            FROM ${BancoDadosLocal.TABELA_HISTORICO_EVENTOS}
            WHERE documento_id = ?
            ORDER BY id ASC
            """.trimIndent(),
            arrayOf(documentoId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    HistoricoEvento(
                        id = cursor.getLong(0),
                        documentoId = cursor.getLong(1),
                        descricao = cursor.getString(2),
                        dataHora = cursor.getString(3),
                        responsavel = cursor.getString(4)
                    )
                )
            }
        }
        return lista
    }

    private fun inserirEventoInterno(
        db: SQLiteDatabase,
        documentoId: Long,
        descricao: String,
        dataHora: String,
        responsavel: String
    ) {
        val valores = ContentValues().apply {
            put("documento_id", documentoId)
            put("descricao", descricao)
            put("data_hora", dataHora)
            put("responsavel", responsavel)
        }
        db.insertOrThrow(BancoDadosLocal.TABELA_HISTORICO_EVENTOS, null, valores)
    }
}
