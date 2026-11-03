# BestSign — Assinatura Eletrônica e Gestão de Documentos PDF

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-API_24%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-Local_DB-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
[![YouTube Demo](https://img.shields.io/badge/YouTube-Demonstração_em_Vídeo-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/watch?v=xK8ZsPvU6fY)
[![Portfólio](https://img.shields.io/badge/Portfólio-renan--xavier.vercel.app-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://renan-xavier.vercel.app)

**Autor:** Renan Santa Rosa Xavier — RM566997 | 🌐 **Portfólio:** [renan-xavier.vercel.app](https://renan-xavier.vercel.app)

---

## 📌 Visão Geral do Projeto

O **BestSign** é um aplicativo nativo para Android voltado à assinatura eletrônica segura e descomplicada de contratos PDF pelo smartphone.

O projeto resolve o problema da baixa alfabetização digital e da burocracia na assinatura de contratos: permite ao administrador carregar um PDF, posicionar de forma proporcional os campos onde cada signatário deve assinar e enviá-lo. O signatário pode assinar via **autenticação biométrica do aparelho** (que gera um carimbo cursivo autenticado) ou desenhando manualmente na tela em um **Canvas dedicado**.

---

## 🎬 Demonstração do Sistema

### Vídeo de Demonstração no YouTube
Confira o fluxo completo do aplicativo no YouTube (envio do contrato, biometria e auditoria):

[![Demonstração BestSign no YouTube](https://img.youtube.com/vi/xK8ZsPvU6fY/maxresdefault.jpg)](https://www.youtube.com/watch?v=xK8ZsPvU6fY)

> 🔗 **Assistir no YouTube:** [Bestsign — Demonstração das Funções](https://www.youtube.com/watch?v=xK8ZsPvU6fY)

### Galeria de Telas do Aplicativo

#### 1. Autenticação, Painel do Administrador e Signatários
| Login Individual | Painel do Administrador | Gestão de Signatários |
| :---: | :---: | :---: |
| ![Tela de Login](apresentacao/tela_01_login.png) | ![Painel Admin](apresentacao/tela_02_painel_admin.png) | ![Signatários](apresentacao/tela_03_signatarios.png) |

#### 2. Criação do Documento, Posicionamento no PDF e Envio
| Upload / PDF Modelo | Seleção e Papéis | Posicionamento Proporcional | Revisão e Envio |
| :---: | :---: | :---: | :---: |
| ![Novo Documento](apresentacao/tela_06_novo_documento_pdf.png) | ![Seleção Signatários](apresentacao/tela_07_selecao_signatarios.png) | ![Posicionamento](apresentacao/tela_08_posicionamento_campos.png) | ![Revisão e Envio](apresentacao/tela_09_revisao_envio.png) |

#### 3. Experiência do Signatário: Biometria ou Desenho Manual em Canvas
| Painel do Signatário | Destaque no PDF | Escolha da Assinatura | Biometria Nativa | Desenho no Canvas |
| :---: | :---: | :---: | :---: | :---: |
| ![Painel Signatário](apresentacao/tela_10_painel_signatario.png) | ![Campo Destacado](apresentacao/tela_11_assinatura_pdf.png) | ![Modal Assinatura](apresentacao/tela_14_escolha_forma_assinatura.png) | ![Prompt Biométrico](apresentacao/tela_15_prompt_android.png) | ![Quadro Canvas](apresentacao/tela_12_quadro_canvas.png) |

#### 4. Confirmação, PDF Assinado e Trilha de Auditoria
| Confirmação Digital | PDF Assinado (Parcial) | PDF Concluído (Todos) | Histórico e Auditoria |
| :---: | :---: | :---: | :---: |
| ![Popup Sucesso](apresentacao/tela_16_popup_sucesso_digital.png) | ![PDF Parcial](apresentacao/tela_17_pdf_assinado_cursivo.png) | ![PDF Completo](apresentacao/tela_18_pdf_ambos_cursivo.png) | ![Histórico Auditoria](apresentacao/tela_13_historico_auditoria.png) |

---

## 🏛️ Arquitetura da Solução

O aplicativo segue o padrão Repository, separando a camada visual dos DAOs locais e dos serviços de arquivo:

```mermaid
flowchart TD
    subgraph UI ["Camada de Interface (Jetpack Compose)"]
        Nav["Navigation Compose\nControle de Rotas & Telas"]
        Screens["LoginScreen / AdminScreen / SignatarioScreen / CanvasScreen"]
    end

    subgraph Domain ["Camada de Negócio & Repositório"]
        DocRepo["DocumentoRepositorio\nValidação de Status & Coordenadas"]
        SigRepo["SignatarioRepositorio\nAutenticação & Perfis"]
    end

    subgraph Data ["Camada de Persistência (SQLite)"]
        Helper["DatabaseHelper (SQLiteOpenHelper)"]
        DocDao["DocumentoDao\nCRUD de Documentos & Trilhas"]
        SigDao["SignatarioDao\nCredenciais & Papéis"]
    end

    subgraph Engine ["Processamento Nativo & Biometria"]
        BioPrompt["BiometricPrompt (AndroidX Biometric)\nValidação Biométrica / Face / Digital"]
        PdfBox["PDFBox-Android\nRenderização de Páginas & Gravação das Assinaturas"]
    end

    UI --> Domain
    Domain --> Data
    Domain --> Engine
```

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Finalidade no Aplicativo |
| :--- | :--- |
| **Kotlin 2.0** | Linguagem moderna e concisa para desenvolvimento Android nativo. |
| **Jetpack Compose (Material 3)** | UI declarativa com componentes modernos do Material Design 3 e animações nativas. |
| **Navigation Compose** | Gerenciamento de rotas e passagem de parâmetros entre telas no Compose. |
| **SQLite Nativo (`SQLiteOpenHelper`)** | Banco de dados relacional embarcado com DAOs dedicados para signatários e documentos. |
| **BiometricPrompt API** | Autenticação biométrica nativa do sistema operacional (impressão digital ou reconhecimento facial). |
| **PDFBox-Android** | Biblioteca para manipulação de arquivos PDF, extração de páginas e gravação de assinaturas nas coordenadas proporcionais. |
| **Android Canvas API** | Quadro de desenho para captura de assinatura manuscrita com suavização de traço. |

---

## 🔑 Credenciais Pré-Cadastradas para Teste

O banco de dados local inicia pré-populado com contas de demonstração:

| Perfil | Nome | E-mail | Senha | Papel |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Admin BestSign | `admin@bestsign.com` | `123456` | Gestão e Envio de Documentos |
| **Signatário 1** | Carlos Eduardo Silva | `carlos@email.com` | `123456` | Contratante |
| **Signatário 2** | Mariana Costa Souza | `mariana@email.com` | `123456` | Contratado |

---

## 🚀 Como Executar o Projeto

1. Abra o **Android Studio**, clique em **Open** e selecione a pasta raiz `BestSign`.
2. Aguarde a sincronização do Gradle e execute o projeto em um emulador ou dispositivo físico com **Android 7.0+ (API 24+)**.
3. Acesse como **Administrador** (`admin@bestsign.com` / `123456`) para posicionar campos no PDF e enviar aos signatários.
4. Alterne para um **Signatário** (`carlos@email.com` / `123456`) para testar a assinatura via biometria ou canvas.
