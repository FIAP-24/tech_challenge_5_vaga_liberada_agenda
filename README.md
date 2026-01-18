# Vaga Liberada - Microsserviço de Agenda

Este projeto gerencia a agenda de consultas e integra-se com serviços de mensageria (SNS/SQS) para notificações de vagas liberadas.

## 🚀 Pré-requisitos

Para rodar o projeto e o ambiente local, você precisará de:

* **Java 21** (JDK)
* **Docker** e **Docker Compose**
* **Maven** (opcional, pois o projeto possui o wrapper `mvnw`)

---

## 🛠️ Subindo o Ambiente Local (Docker)

O ambiente de desenvolvimento conta com containers para o Banco de Dados e para a simulação da AWS (LocalStack).

### 1. Estrutura de Arquivos Docker
Certifique-se de que a estrutura de pastas na raiz do projeto esteja assim para que os scripts de inicialização funcionem:

```text
/
├── docker-compose.yaml
├── docker/
│   ├── mysql/
│   │   └── init.sql         # Cria a tabela 'paciente' automaticamente
│   └── localstack/
│       └── init-aws.sh      # Cria fila SQS e tópico SNS automaticamente