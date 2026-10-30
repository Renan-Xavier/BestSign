package br.com.fiap.bestsign.util

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat

object AutenticadorBiometrico {

    fun possuiDigitalConfigurada(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val gerenciador = contexto.getSystemService(BiometricManager::class.java)
            if (gerenciador != null) {
                val resultado = gerenciador.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                if (resultado == BiometricManager.BIOMETRIC_SUCCESS) {
                    return true
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val gerenciador = contexto.getSystemService(BiometricManager::class.java)
            if (gerenciador != null && gerenciador.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                return true
            }
        }

        val gerenciadorBloqueio = contexto.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return gerenciadorBloqueio?.isDeviceSecure == true
    }

    fun solicitarAutenticacaoDigital(
        contexto: Context,
        nomeSignatario: String,
        aoAutenticarComSucesso: () -> Unit,
        aoFalharOuCancelar: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            aoFalharOuCancelar("Esta versão do Android não suporta o diálogo nativo de biometria.")
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(contexto)
            val sinalCancelamento = CancellationSignal()

            val construtor = BiometricPrompt.Builder(contexto)
                .setTitle("Confirmar Assinatura com Digital")
                .setSubtitle(nomeSignatario)
                .setDescription("Toque no sensor de digital do celular para assinar o documento por extenso.")

            var usarCredencialAparelho = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val gerenciador = contexto.getSystemService(BiometricManager::class.java)
                val temDigitalCadastrada = gerenciador?.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                ) == BiometricManager.BIOMETRIC_SUCCESS

                if (temDigitalCadastrada) {
                    construtor.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                } else {
                    usarCredencialAparelho = true
                    construtor.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
            }

            if (!usarCredencialAparelho) {
                construtor.setNegativeButton("Cancelar", executor) { _, _ ->
                    aoFalharOuCancelar("Autenticação por digital cancelada pelo usuário.")
                }
            }

            val promptBiometrico = construtor.build()

            promptBiometrico.authenticate(
                sinalCancelamento,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        aoAutenticarComSucesso()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        val mensagem = errString?.toString()?.takeIf { it.isNotBlank() }
                            ?: "Não foi possível validar a digital no dispositivo."
                        aoFalharOuCancelar(mensagem)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )
        } catch (e: Exception) {
            aoFalharOuCancelar("Erro ao iniciar leitor de digital: ${e.localizedMessage ?: "verifique o bloqueio do aparelho"}.")
        }
    }

    fun gerarBitmapAssinaturaPorExtenso(
        nomeCompleto: String,
        largura: Int = 600,
        altura: Int = 220
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val textoAssinatura = nomeCompleto.trim().ifBlank { "Assinatura Eletronica" }
        val fonteCursiva = Typeface.create("cursive", Typeface.ITALIC)

        val pinturaTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(11, 37, 69)
            typeface = fonteCursiva
            textSkewX = -0.14f
            isSubpixelText = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1.3f
        }

        var tamanhoFonte = 68f
        val larguraMaximaTexto = largura * 0.86f
        pinturaTexto.textSize = tamanhoFonte
        var larguraMedida = pinturaTexto.measureText(textoAssinatura)

        while (larguraMedida > larguraMaximaTexto && tamanhoFonte > 28f) {
            tamanhoFonte -= 2f
            pinturaTexto.textSize = tamanhoFonte
            larguraMedida = pinturaTexto.measureText(textoAssinatura)
        }

        val xTexto = ((largura - larguraMedida) / 2f).coerceAtLeast(20f)
        val metricas = pinturaTexto.fontMetrics
        val yTexto = (altura * 0.56f) - ((metricas.ascent + metricas.descent) / 2f)

        canvas.drawText(textoAssinatura, xTexto, yTexto, pinturaTexto)

        val pinturaRubrica = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 56, 110)
            style = Paint.Style.STROKE
            strokeWidth = 3.2f
            strokeCap = Paint.Cap.ROUND
        }

        val inicioLinhaX = xTexto + (larguraMedida * 0.08f)
        val fimLinhaX = xTexto + (larguraMedida * 0.94f)
        val baseLinhaY = (yTexto + 18f).coerceAtMost(altura - 20f)

        val caminhoRubrica = Path().apply {
            moveTo(inicioLinhaX, baseLinhaY + 4f)
            quadTo(
                (inicioLinhaX + fimLinhaX) / 2f,
                baseLinhaY - 10f,
                fimLinhaX,
                baseLinhaY + 2f
            )
        }
        canvas.drawPath(caminhoRubrica, pinturaRubrica)

        return bitmap
    }
}
