#!/bin/sh
echo "### Iniciando configuração do LocalStack (SNS + SQS) ###"

# 1. Criar o Tópico SNS
# O ARN padrão no LocalStack é arn:aws:sns:us-east-1:000000000000:<nome>
echo "Criando Tópico SNS..."
awslocal sns create-topic --name notificacao-agenda-topic

# 2. Criar a Fila SQS
echo "Criando Fila SQS..."
awslocal sqs create-queue --queue-name notificacao-agenda-queue

# 3. Fazer a Assinatura (Subscription)
# A fila 'notificacao-agenda-queue' vai receber tudo que for publicado no tópico
echo "Assinando a Fila ao Tópico..."
awslocal sns subscribe \
    --topic-arn arn:aws:sns:us-east-1:000000000000:notificacao-agenda-topic \
    --protocol sqs \
    --notification-endpoint arn:aws:sqs:us-east-1:000000000000:notificacao-agenda-queue

echo "### Configuração Concluída ###"