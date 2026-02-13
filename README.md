# Vaga Liberada - Microsserviço de Agenda

Sistema de gerenciamento de agenda de consultas para o SUS, com controle de **especialidades**, **médicos**, **unidades de saúde**, **pacientes**, **agendamentos**, **lista de espera** e **realocação de vagas** (notificações via SQS/SNS).

---

## Por que este MVP

Este **MVP (produto mínimo viável)** está pronto para **simular e demonstrar a eficiência** de ter um fluxo de lista de espera com realocação automática de vagas e notificações integradas.

No SUS, o **alto número de desistências e faltas** é um problema conhecido: muitas vagas ficam ociosas porque o paciente não comparece ou desiste em cima da hora, e quem está na fila não é avisado a tempo. Com isso:

- **Vagas são perdidas** — o horário passa e a vaga não é reaproveitada.
- **A fila de espera demora mais** — quem poderia ser atendido naquele slot continua esperando.
- **Recursos (médicos, consultórios, tempo) são subutilizados** — impacto direto na capacidade de atendimento da rede.

Este projeto **mostra como reduzir esse desperdício**: quando alguém desiste ou não confirma no prazo, a vaga é **liberada na hora**, o **próximo da lista de espera é notificado** (e-mail/SMS via fila) e pode **aceitar a vaga** em poucos cliques. Se não aceitar em até 2 horas, a oferta segue para o próximo da fila. Assim, menos vagas ficam vazias e mais pessoas são atendidas com a mesma estrutura — exatamente o tipo de funcionalidade que pode **ajudar o SUS a aumentar o aproveitamento da agenda** e a **reduzir o tempo de espera** para quem depende da lista.

O MVP serve, portanto, como **prova de conceito** para avaliar o ganho de eficiência e a viabilidade de adoção em cenários reais (unidades, municípios ou redes).

---

## Funcionalidades principais

- **CRUD** de Pacientes, Médicos e Unidades de Saúde (com filtros e paginação onde aplicável)
- **Agendamento de consultas** (médico, unidade, data/hora), com status: `PENDENTE_CONFIRMACAO`, `AGENDADA`, `CANCELADA`, `DESISTENCIA`, `LIBERADA`, `REALIZADA`
- **Confirmação** de consulta (até 30 minutos antes do horário)
- **Cancelar** consulta (apenas não realizada) — não libera vaga
- **Desistir** da consulta — libera vaga e oferece à lista de espera
- **Lista de espera** por especialidade (opcional: médico e/ou unidade); adicionar, listar, obter próximo, remover
- **Realocação automática**: ao desistir ou ao não confirmar no prazo, a vaga é oferecida ao próximo da fila (notificação SQS)
- **Aceitar vaga** oferecida (paciente da lista de espera assume a consulta)
- **Timeout de oferta**: se o paciente não aceitar em 2 horas, a vaga é oferecida ao próximo
- **Schedulers**: confirmação (consultas não confirmadas → liberar vaga), timeout de oferta, notificações de lembrete

## Tecnologias

- **Java 21** · **Spring Boot 3.4** · **Spring Data JPA** · **MySQL 8** · **Flyway** · **AWS SQS/SNS** (LocalStack em dev)
- **Lombok** · **MapStruct** · **Maven**

---

## Pré-requisitos

- **Java 21** (JDK)
- **Docker** e **Docker Compose** (para ambiente com MySQL + LocalStack)
- **Maven** (opcional; o projeto inclui wrapper `mvnw`)

---

## Estrutura Docker

O ambiente pode subir com três serviços: aplicação, MySQL e LocalStack (SQS/SNS).

```text
/
├── docker-compose.yaml      # app (8080), mysql (3307), localstack (4566)
├── Dockerfile
└── docker/
    └── localstack/
        └── init-aws.sh      # Cria fila SQS e tópico SNS no LocalStack
```

O schema do banco e os dados iniciais são gerenciados pelo **Flyway** em `src/main/resources/db/migration/`.

---

## Subindo o ambiente

### Opção 1: Aplicação e infraestrutura juntas (Docker Compose)

Um único comando sobe a aplicação, o MySQL e o LocalStack; a API só inicia depois que MySQL e LocalStack estiverem saudáveis.

```bash
docker compose up -d --build
```

- Na primeira vez, o build da imagem da aplicação pode demorar (Maven baixa dependências dentro do container).
- **API:** http://localhost:8080  
- **MySQL:** porta 3307 (host) → 3306 (container)  
- **LocalStack:** http://localhost:4566  

Se o build da imagem falhar por rede/DNS dentro do Docker (ex.: “Temporary failure in name resolution”), use o JAR já buildado no host:

```bash
mvn clean package -DskipTests
docker build -f Dockerfile.prebuilt -t agenda-api:latest .
docker compose up -d
```

Para testar a API com curl, por exemplo:

```bash
curl -s http://localhost:8080/api/medicos | jq .
curl -s http://localhost:8080/api/consultas?status=AGENDADA | jq .
```

### Opção 2: Aplicação local (Maven) + MySQL e LocalStack no Docker

1. Subir apenas MySQL e LocalStack:

```bash
docker-compose up -d mysql localstack
```

2. Configurar o datasource para o MySQL na porta **3307** (ex.: `localhost:3307` em `application.properties` ou perfil local).

3. Rodar a API:

```bash
./mvnw spring-boot:run
```

Ou com JDK 21 explicitamente:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw spring-boot:run
```

---

## API REST (base: `/api`)

| Recurso | Endpoints principais |
|--------|------------------------|
| **Pacientes** `/api/pacientes` | `POST`, `GET /{id}`, `GET /cpf/{cpf}`, `GET` (paginado), `PUT /{id}`, `DELETE /{id}` |
| **Médicos** `/api/medicos` | `POST`, `GET /{id}`, `GET /crm/{crm}`, `GET` (paginado; filtros: `especialidadeId`, `unidadeId`, `ativo`), `PUT /{id}`, `PATCH /{id}/desativar`, `PATCH /{id}/ativar`, `DELETE /{id}` |
| **Unidades de saúde** `/api/unidades-saude` | `POST`, `GET /{id}`, `GET` (paginado ou filtros `cidade`, `bairro`), `PUT /{id}`, `DELETE /{id}` |
| **Consultas** `/api/consultas` | `POST` (agendar), `GET /{id}`, `GET` (filtros: `pacienteId`, `status`), `POST /{id}/confirmar`, `PATCH /{id}/cancelar`, `PATCH /{id}/desistir`, `POST /{id}/aceitar-vaga?listaEsperaId=` |
| **Lista de espera** `/api/lista-espera` | `POST`, `GET /{id}`, `GET` (filtros: `especialidadeId`, `medicoId`, `unidadeId`), `GET /proximo`, `DELETE /{id}` |

### Postman

Está disponível uma coleção Postman para testar os endpoints: **`postman/Vaga_Liberada_Agenda.postman_collection.json`**. No Postman, use *Import* e selecione esse arquivo. A base URL pode ser configurada como variável (ex.: `http://localhost:8080`) com a API em execução.

---

## Schedulers

| Scheduler | Intervalo | Função |
|-----------|-----------|--------|
| **ConfirmacaoScheduler** | 1 hora | Consultas `PENDENTE_CONFIRMACAO` com prazo de confirmação expirado → libera vaga e oferece à lista de espera |
| **TimeoutVagaScheduler** | 30 min | Consultas `LIBERADA` com vaga oferecida há mais de 2h → desfaz oferta e oferece ao próximo da fila |
| **NotificacaoScheduler** | 1 minuto | Consultas AGENDADAS/PENDENTE_CONFIRMACAO na janela de 24h30 → envia lembrete (SQS) |

---

## Testes e cobertura

```bash
./mvnw test
```

Relatório JaCoCo (após `mvn verify`):

```bash
./mvnw verify
# Relatório: target/site/jacoco/index.html
```

Recomendado usar **JDK 21** para evitar falhas de compilação/teste com versões mais novas do JDK.

---

## O que ainda falta implementar (TODO)

Itens planejados ou desejáveis para evoluir o sistema:

| Item | Descrição |
|------|-----------|
| **Agenda do médico (horários disponíveis)** | Hoje o agendamento aceita qualquer `dataHora` futura e só valida se aquele horário já está ocupado. Não existe: (1) cadastro de **agenda do médico** com faixa de horários (ex.: seg–sex 8h–12h, 14h–18h por unidade); (2) endpoint para **listar slots disponíveis** em um intervalo (ex.: “próximos 7 dias” ou “dia X”), considerando essa agenda e as consultas já agendadas. Implementar isso permitiria oferecer “horários disponíveis” no front e evitar tentativas em horários fora do expediente. |
| *(outros itens conforme prioridade)* | Ex.: relatórios, bloqueio de agenda (folga/feriado), duração configurável por tipo de consulta, etc. |

---

## Próximas fases de melhorias

Roadmap de evolução do sistema, organizado em fases que podem ser implementadas conforme prioridade e capacidade.

### Fase 1 – Agenda e slots

- **Agenda do médico:** cadastro de faixas de horário de atendimento por médico/unidade (ex.: seg–sex 8h–12h, 14h–18h).
- **Slots disponíveis:** endpoint para listar horários disponíveis em um período (dia ou range), considerando a agenda e as consultas já agendadas.
- **Duração da consulta:** duração configurável por especialidade ou tipo (ex.: 15 min, 30 min) para gerar slots de forma automática.
- **Bloqueios:** folgas, férias e feriados (bloquear dias/horários na agenda do médico ou da unidade).

### Fase 2 – Experiência e notificações

- **Canais de notificação:** além de SQS, disparar e-mail e SMS de fato (integração com provedores), com preferência do paciente (e-mail, SMS ou ambos).
- **Lembretes configuráveis:** intervalo de envio de lembrete (ex.: 24h, 48h antes) e quantidade de lembretes.
- **Confirmação por link:** link único no e-mail/SMS para confirmar ou desistir sem precisar logar no sistema.
- **Histórico de notificações:** registro do que foi enviado e quando (para suporte e auditoria).

### Fase 3 – Regras e relatórios

- **Regras de lista de espera:** prioridade por perfil (ex.: gestante, idoso), tempo de espera e possibilidade de “furar fila” em casos excepcionais.
- **Relatórios:** ocupação por médico/unidade, taxa de comparecimento, tempo médio na lista de espera, consultas canceladas/desistências por período.
- **Dashboard:** indicadores em tempo (quase) real para gestão da unidade (vagas liberadas hoje, fila por especialidade, etc.).
- **Exportação:** relatórios em CSV/Excel e agendamento de envio por e-mail.

### Fase 4 – Segurança e operação

- **Autenticação e autorização:** login (ex.: OAuth2/JWT), perfis (paciente, médico, administrador da unidade) e controle de acesso por recurso.
- **Auditoria:** log de alterações em consultas e lista de espera (quem alterou, quando e valor anterior).
- **Rate limiting e resiliência:** proteção de APIs contra abuso e retry/fallback nas integrações (SQS, e-mail, SMS).
- **Health e observabilidade:** endpoints de health detalhados (banco, fila), métricas (Prometheus/Micrometer) e tracing para troubleshooting.

### Fase 5 – Integrações e ecossistema

- **Integração com sistemas do SUS:** troca de dados com sistemas externos (ex.: e-SUS, prontuário) quando houver APIs ou protocolos definidos.
- **Check-in no local:** confirmação de presença na unidade (QR code ou totem) para atualizar status da consulta e liberar vaga em caso de no-show.
- **Agendamento por terceiros:** possibilidade de postos de saúde ou centrais agendarem em nome do paciente, com consentimento registrado.
