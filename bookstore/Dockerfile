# ==========================================================
# ESTÁGIO 1: Compilação e Build da Aplicação (Maven)
# ==========================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia os arquivos de configuração do Maven e o código-fonte
COPY pom.xml .
COPY src ./src

# Executa o comando do Maven para limpar, compilar e gerar o arquivo .jar
# pulando os testes para acelerar o deploy no Render
RUN mvn clean package -DskipTests

# ==========================================================
# ESTÁGIO 2: Execução da Aplicação com Java 21 Lite
# ==========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o arquivo .jar gerado no estágio anterior para este container final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão que o Render utiliza
EXPOSE 8080

# Comando oficial que inicia a aplicação Spring Boot na nuvem
ENTRYPOINT ["java", "-jar", "app.jar"]