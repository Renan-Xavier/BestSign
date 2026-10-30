# Colinha para Apresentação · Bestsign (MVP Sprint 1)

Roteiro curto do que falar em cada slide durante a apresentação.

---

### Slide 1 · Capa (Bestsign)
- "Olá, meu nome é Renan Santa Rosa Xavier (RM 566997) e vou apresentar o Bestsign, nosso aplicativo Android nativo para envio, posicionamento de campos e assinatura eletrônica de documentos PDF pelo celular."

### Slide 2 · Dificuldades na assinatura pelo celular
- "O projeto nasceu para resolver dois problemas práticos: primeiro, quem envia um contrato não sabe se a pessoa que vai assinar tem facilidade com celular ou idade mais avançada, e muitas vezes a pessoa não acha onde deve assinar no PDF. Segundo, evitar que pessoas compartilhem a mesma conta ou assinem sem saber qual papel estão assumindo no documento."

### Slide 3 · Frase de destaque
- "Por isso, a ideia central do aplicativo é garantir que qualquer pessoa consiga encontrar seu campo e assinar o PDF no celular sem depender de ajuda técnica, usando seu próprio login."

### Slide 4 · Fluxo do documento em passos
- "O funcionamento tem 4 passos simples: o administrador faz o upload do PDF e escolhe quem vai assinar; depois posiciona os quadros de assinatura arrastando com o dedo sobre a folha; o signatário entra na conta dele e assina na tela ou com a digital do celular; e o aplicativo grava essa assinatura diretamente dentro do arquivo PDF."

### Slide 5 · Arquitetura local preparada para API
- "No código, usamos apenas Android nativo com Kotlin, SQLite e PDFBox-Android. Criamos o `SignatarioRepositorio` e o `DocumentoRepositorio` para isolar o banco local das telas. Assim, nas próximas Sprints, podemos trocar o SQLite por uma API REST sem precisar refazer as telas."

### Slide 6 · Divisória 01 (Visão do administrador)
- "Agora vou mostrar as telas da visão do administrador."

### Slide 7 · Acesso, painel e signatários
- "Na tela de login, já deixamos 1 Admin e 2 Signatários pré-cadastrados no banco local para facilitar os testes. Entrando como Admin, temos os contadores de documentos pendentes, parciais e concluídos, e a tela de cadastro e edição de signatários com validação de campos obrigatórios e e-mail único."

### Slide 8 · Upload, papéis e posicionamento
- "Para criar um documento, o Admin seleciona o arquivo PDF do celular. O app valida se o PDF não está vazio ou corrompido e mostra as páginas na tela. Em seguida, o Admin escolhe os signatários, define o papel de cada um (Contratante, Contratado ou Testemunha) e arrasta o quadro de assinatura sobre a página do PDF."

### Slide 9 · Revisão e bloqueio pós-envio
- "Na etapa 3, o Admin revisa quantos campos ficaram em cada página, escreve uma mensagem de orientação para os signatários e dispara o documento. Depois que o documento é enviado, o PDF e as posições ficam bloqueados contra edição para garantir a integridade do contrato."

### Slide 10 · Divisória 02 (Visão do signatário e auditoria)
- "Agora passando para a visão de quem recebe o documento para assinar."

### Slide 11 · Assinatura guiada no celular
- "Quando a signatária Mariana entra com o login dela, ela vê apenas os documentos destinados a ela, já com o papel dela destacado e a mensagem do remetente. Ao abrir o PDF, um aviso no topo com botão de atalho leva direto para a página certa e destaca o quadro em amarelo com a frase 'Toque aqui para assinar'."

### Slide 12 · Assinatura com digital do celular
- "Ao tocar para assinar, abre um popup perguntando como a pessoa deseja assinar: manualmente desenhando no Canvas, ou com a autenticação de digital do próprio Android (caso o celular tenha bloqueio/digital configurada). Escolhendo a digital, o Android valida a biometria do usuário, o aplicativo preenche automaticamente o nome completo da pessoa em letra cursiva por extenso no PDF e mostra um popup de confirmação que fecha sozinho em um segundo e meio."

### Slide 13 · Gravação no PDF e histórico
- "Assim que o signatário confirma, o PDFBox-Android grava a assinatura, o nome, o papel e a data/hora diretamente dentro do arquivo PDF. O status muda sozinho de Pendente para Parcialmente Assinado e depois para Concluído, registrando cada etapa no histórico de eventos."

### Slide 14 · Estrutura entregue na Sprint 1
- "Em resumo, nesta Sprint 1 entregamos as 9 telas nativas 100% em português, 6 tabelas relacionais no SQLite, 2 repositórios isolando a camada de dados e 3 contas prontas para demonstração imediata."

### Slide 15 · Encerramento
- "Obrigado."
