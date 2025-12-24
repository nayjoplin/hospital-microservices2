# Dockerfile para Serviço de Agendamento
# Build em múltiplos estágios para otimizar tamanho da imagem

# Estágio 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Garantir certificados atualizados (evita falhas de handshake TLS/SSL)
RUN apk add --no-cache ca-certificates && update-ca-certificates

WORKDIR /app

# Garantir CA certificates no container (evita falhas de handshake SSL ao baixar deps)
RUN apk add --no-cache ca-certificates && update-ca-certificates

# Copiar apenas pom.xml primeiro (para cache de dependências)
COPY pom.xml .


# Copiar código fonte
COPY src ./src

# Compilar aplicação (pular testes para build mais rápido)
RUN mvn -B -ntp -DskipTests clean package

# Estágio 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Configurar JVM para containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Executar aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
