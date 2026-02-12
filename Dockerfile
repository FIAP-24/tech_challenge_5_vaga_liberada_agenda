# Construindo pacote
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Criando imagem jdk 21 slim
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

# Copiando o JAR que foi criado no builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expor a porta
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]