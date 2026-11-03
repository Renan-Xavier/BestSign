# BestSign — Assinatura Eletrônica e Gestão de Documentos PDF

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-API_24%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-Local_DB-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
[![YouTube Demo](https://img.shields.io/badge/YouTube-Demonstração_em_Vídeo-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/watch?v=xK8ZsPvU6fY)
[![Portfólio](https://img.shields.io/badge/Portfólio-renan--xavier.vercel.app-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://renan-xavier.vercel.app)

**Autor:** Renan Santa Rosa Xavier — RM566997 | 🌐 **Portfólio:** [renan-xavier.vercel.app](https://renan-xavier.vercel.app)

---

## 🎬 Demonstração em Vídeo do Aplicativo

Veja na prática o funcionamento do BestSign com envio de documento, posicionamento de campos, assinatura com biometria e trilha de auditoria:

[![Demonstração BestSign no YouTube](https://img.youtube.com/vi/xK8ZsPvU6fY/maxresdefault.jpg)](https://www.youtube.com/watch?v=xK8ZsPvU6fY)

> 🔗 **Assistir no YouTube:** [Bestsign — Demonstração das Funções](https://www.youtube.com/watch?v=xK8ZsPvU6fY)

---

## Visão Geral do Projeto

### O Problema
Processos de assinatura de contratos e documentos em formato PDF frequentemente enfrentam dois gargalos principais:
1. **Dificuldade de usabilidade:** Signatários com pouca familiaridade tecnológica ou idade avançada muitas vezes não encontram onde devem assinar ou abandonam fluxos complexos.
2. **Falta de isolamento e rastreabilidade:** Compartilhamento indevido de credenciais e ausência de clareza sobre o papel contratual (Contratante, Contratado ou Testemunha) e o histórico de auditoria do documento.

### A Solução
O **BestSign** é um aplicativo nativo Android voltado ao envio, posicionamento proporcional de campos, assinatura eletrônica (manuscrita em Canvas ou cursiva com autenticação biométrica) e acompanhamento completo de documentos PDF pelo celular.

---

## Galeria de Telas do Aplicativo

### 1. Autenticação, Painel Administrativo e Gestão de Signatários
| Login Individual | Painel do Administrador | Gestão de Signatários |
| :---: | :---: | :---: |
| ![Tela de Login](apresentacao/tela_01_login.png) | ![Painel Admin](apresentacao/tela_02_painel_admin.png) | ![Signatários](apresentacao/tela_03_signatarios.png) |

### 2. Criação do Documento, Posicionamento no PDF e Envio
| Upload / PDF Modelo | Seleção e Papéis | Posicionamento Proporcional | Revisão e Envio |
| :---: | :---: | :---: | :---: |
| ![Novo Documento](apresentacao/tela_06_novo_documento_pdf.png) | ![Seleção Signatários](apresentacao/tela_07_selecao_signatarios.png) | ![Posicionamento](apresentacao/tela_08_posicionamento_campos.png) | ![Revisão e Envio](apresentacao/tela_09_revisao_envio.png) |

### 3. Experiência do Signatário: Biometria ou Desenho Manual em Canvas
| Painel do Signatário | Destaque no PDF | Escolha da Assinatura | Biometria Nativa | Desenho no Canvas |
| :---: | :---: | :---: | :---: | :---: |
| ![Painel Signatário](apresentacao/tela_10_painel_signatario.png) | ![Campo Destacado](apresentacao/tela_11_assinatura_pdf.png) | ![Modal Assinatura](apresentacao/tela_14_escolha_forma_assinatura.png) | ![Prompt Biométrico](apresentacao/tela_15_prompt_android.png) | ![Quadro Canvas](apresentacao/tela_12_quadro_canvas.png) |

### 4. Confirmação, PDF Assinado e Trilha de Auditoria
| Confirmação Digital | PDF Assinado (Parcial) | PDF Concluído (Todos) | Histórico e Auditoria |
| :---: | :---: | :---: | :---: |
| ![Popup Sucesso](apresentacao/tela_16_popup_sucesso_digital.png) | ![PDF Parcial](apresentacao/tela_17_pdf_assinado_cursivo.png) | ![PDF Completo](apresentacao/tela_18_pdf_ambos_cursivo.png) | ![Histórico Auditoria](apresentacao/tela_13_historico_auditoria.png) |

---

## Arquitetura e Tecnologias Utilizadas

```mermaid
flowchart LR
    UI["UI Layer\nJetpack Compose (Material 3)"] --> Repo["Repository Layer\nDocumentoRepositorio / SignatarioRepositorio"]
    Repo --> DAO["Data Layer (SQLite)\nDocumentoDao / SignatarioDao"]
    Repo --> PDF["PDF & Biometria\nPDFBox-Android + BiometricPrompt"]
```

- **Desenvolvimento:** Android Nativo em Kotlin com Jetpack Compose (Material 3 e Navigation Compose).
- **Banco de Dados Local:** SQLite nativo (`SQLiteOpenHelper`) com DAOs dedicados (`SignatarioDao` e `DocumentoDao`).
- **Arquitetura em Camadas:** Padrão Repository isolando o acesso ao banco local e ao sistema de arquivos.
- **Manipulação de PDF e Assinatura:**
  - `Canvas` nativo para captura do desenho manual da assinatura.
  - Geração automática de assinatura cursiva por extenso após autenticação biométrica nativa (`BiometricPrompt`).
  - `PDFBox-Android` (`com.tom-roush:pdfbox-android`) para renderização das páginas e gravação definitiva das assinaturas nas coordenadas proporcionais selecionadas.

---

## Credenciais de Teste Pré-Cadastradas

O banco de dados local já inicia pré-populado com 1 conta de Administrador, 2 contas de Signatários e 1 contrato PDF de demonstração:

| Perfil | Nome | E-mail | Senha | Papel |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Admin BestSign | `admin@bestsign.com` | `123456` | Gestão e Envio |
| **Signatário 1** | Carlos Eduardo Silva | `carlos@email.com` | `123456` | Contratante |
| **Signatário 2** | Mariana Costa Souza | `mariana@email.com` | `123456` | Contratado |

---

## Como Executar o Projeto

1. Abra o **Android Studio**, clique em **Open** e selecione a pasta raiz do projeto `BestSign`.
2. Aguarde a sincronização do Gradle e execute o aplicativo em um emulador ou dispositivo Android (**API 24+**).
3. Faça login como **Administrador** (`admin@bestsign.com` / `123456`) para criar documentos, posicionar campos de assinatura sobre o PDF e enviar para os signatários.
4. Alterne para um **Signatário** (`carlos@email.com` ou `mariana@email.com` / `123456`) para visualizar o campo destacado no PDF e assinar via **Biometria do Android** ou **Desenho Manual na Tela**.
