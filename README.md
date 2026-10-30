# Bestsign

**Autor:** Renan Santa Rosa Xavier - RM566997

## Resumo do Problema e da Solucao

### Problema
Processos de assinatura de contratos e documentos em formato PDF frequentemente enfrentam dois gargalos principais:
1. Dificuldade de usabilidade para signatarios que possuem pouca familiaridade com tecnologia ou idade avancada, que muitas vezes nao encontram onde devem assinar ou abandonam fluxos complexos.
2. Falta de seguranca e compartilhamento indevido de credenciais, fazendo com que diferentes pessoas utilizem a mesma conta ou assinem documentos sem clareza sobre o seu papel contratual e o contexto da solicitacao.

### Solucao
O **Bestsign** e um aplicativo nativo Android voltado ao envio, posicionamento proporcional de campos, assinatura eletronica manuscrita e acompanhamento completo de documentos PDF pelo celular.

A solucao resolve as dores apontadas por meio de:
- **Autenticacao individual e isolamento de acesso:** Cada signatario possui login proprio (e-mail e senha) e visualiza exclusivamente os documentos destinados ao seu usuario, acompanhados da mensagem de contexto e da identificacao clara do seu papel (Contratante, Contratado ou Testemunha).
- **Interface acessivel e direta para o signatario:** Tipografia legivel, alto contraste, separacao clara entre documentos pendentes e assinados, e destaque visual direto sobre a pagina do PDF indicando exatamente onde tocar para desenhar a assinatura.
- **Posicionamento proporcional e gravacao direta no PDF:** O administrador posiciona os quadros de assinatura arrastando-os dentro dos limites da pagina do PDF com coordenadas proporcionais (X, Y, largura e altura), e a assinatura desenhada no Canvas e gravada diretamente nas coordenadas correspondentes do arquivo PDF final.

---

## Tecnologias Utilizadas

- **Desenvolvimento:** Android Nativo em Kotlin com Jetpack Compose (Material 3 e Navigation Compose).
- **Banco de Dados Local:** SQLite nativo (`SQLiteOpenHelper`) com DAOs dedicados (`SignatarioDao` e `DocumentoDao`).
- **Arquitetura de Dados:** Padrao Repository (`SignatarioRepositorio` e `DocumentoRepositorio`) isolando o acesso ao banco de dados local e ao sistema de arquivos, permitindo futura substituicao por uma API REST sem necessidade de refatorar as telas.
- **Armazenamento de Arquivos:** Gravacao dos arquivos PDF no armazenamento interno do dispositivo (`filesDir/documentos_pdf`) com referencia de caminho, metadados e status persistidos no banco de dados local.
- **Manipulacao de PDF e Assinatura (Manual ou com Digital):** `Canvas` nativo para captura do desenho manual da assinatura ou geracao automatica da assinatura cursiva por extenso a partir do nome completo do signatario apos autenticacao biometrica nativa do Android (`BiometricPrompt` / `BiometricManager`), com popup de confirmacao automatica (1,5 segundo) e `PDFBox-Android` (`com.tom-roush:pdfbox-android`) para validacao do arquivo, renderizacao das paginas do PDF e gravacao definitiva da assinatura nas paginas e coordenadas selecionadas.

---

## Credenciais de Teste Pre-Cadastradas

O banco de dados local ja inicia pre-populado com 1 conta de Administrador, 2 contas de Signatarios e 1 documento PDF de demonstracao pronto para conferencia e assinatura:

### Perfil Administrador
- **E-mail:** `admin@bestsign.com`
- **Senha:** `123456`

### Perfil Signatario 1 (Contratante)
- **Nome:** Carlos Eduardo Silva
- **E-mail:** `carlos@email.com`
- **Senha:** `123456`

### Perfil Signatario 2 (Contratado)
- **Nome:** Mariana Costa Souza
- **E-mail:** `mariana@email.com`
- **Senha:** `123456`

---

## Instrucoes para Testar o Fluxo no Emulador ou Aparelho

1. Abra o Android Studio, clique em **Open** e selecione a pasta raiz do projeto `BestSign`.
2. Aguarde a sincronizacao do Gradle e execute o aplicativo em um emulador ou dispositivo Android (API 24 ou superior).
3. **Teste como Administrador (`admin@bestsign.com` / `123456`):**
   - No painel inicial, consulte a lista de documentos filtrando por status (Pendentes, Parciais e Concluidos).
   - Toque em **Signatarios** para cadastrar novos signatarios ou editar os existentes.
   - Toque em **Novo Documento PDF**, informe o titulo do documento e selecione um arquivo PDF valido no armazenamento do aparelho (ou utilize o botao **Usar PDF de Modelo** para gerar um contrato PDF de 2 paginas caso o emulador nao possua arquivos salvos).
   - Selecione os signatarios desejados, classifique o papel de cada um (Contratante, Contratado ou Testemunha) e avance para o posicionamento.
   - Na tela de posicionamento, escolha a pagina do PDF, adicione os campos de assinatura para cada signatario e arraste os quadros ate a posicao desejada dentro dos limites da pagina.
   - Na tela de revisao, confira o resumo das paginas, ajuste ou substitua signatarios se necessario, escreva a mensagem do convite e confirme o envio.
4. **Teste como Signatario (`carlos@email.com` ou `mariana@email.com` / `123456`):**
   - Saia da conta de Admin e entre com a conta de um dos signatarios.
   - Visualize a aba **Pendentes para Assinar**, confira seu papel no documento e a mensagem do convite, e toque em **Abrir Documento e Assinar Agora**.
   - Toque no campo destacado sobre a pagina do PDF ou no botao de atalho para abrir o modal de escolha da forma de assinatura:
     - **Assinar com Digital do Celular:** autentica a digital/bloqueio do Android, preenche automaticamente o PDF com o nome completo do signatario em escrita cursiva por extenso e exibe um popup de confirmacao por 1,5 segundo.
     - **Assinar Manualmente na Tela:** abre o quadro `Canvas` para desenhar a assinatura com o dedo e gravar no PDF.
   - Acesse a tela de acompanhamento para verificar o status atualizado, o PDF assinado e o historico completo de eventos com datas e horarios.
   - Repita a assinatura com o segundo signatario para observar o status geral do documento mudar automaticamente de **Parcialmente Assinado** para **Concluido**.
