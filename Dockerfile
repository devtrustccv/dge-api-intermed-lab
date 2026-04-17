# Etapa 1: Build da aplicação
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia os arquivos necessários para o build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final para produção
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia apenas o JAR gerado na etapa anterior
COPY --from=builder /app/target/*.jar app.jar

COPY --from=builder /app/src/main/resources/db/migration/*.sql ./flyway/sql/

# Expõe a porta da aplicação
EXPOSE 8080

# Define a variável de ambiente para o perfil de produção
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
