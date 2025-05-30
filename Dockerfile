# syntax=docker/dockerfile:1
# Multi-stage build com otimizações agressivas para tamanho mínimo
FROM gradle:8.11-jdk21-alpine AS builder

# Install UPX for compression
RUN apk add --no-cache upx

WORKDIR /app

# Copiar e baixar dependências (cache layer)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
RUN gradle dependencies --no-daemon --quiet

# Copiar código e build otimizado
COPY src/ src/
RUN gradle bootJar -x test --build-cache --parallel \
    && ls -la build/libs/ \
    && echo "Original JAR size:" && du -h build/libs/*.jar

# Extrair camadas do JAR para melhor cache
RUN java -Djarmode=layertools -jar build/libs/*.jar extract \
    && echo "Compressing extracted layers with UPX..." \
    && find . -name "*.jar" -exec upx --best --lzma {} \; || true \
    && find . -name "*.class" -exec upx --best --lzma {} \; 2>/dev/null || true \
    && echo "Compression complete, checking sizes:" \
    && du -sh dependencies/ spring-boot-loader/ snapshot-dependencies/ application/

# Stage intermediário para limpeza adicional
FROM alpine:3.19 AS cleaner
RUN apk add --no-cache findutils
COPY --from=builder /app/dependencies/ /tmp/app/dependencies/
COPY --from=builder /app/spring-boot-loader/ /tmp/app/spring-boot-loader/
COPY --from=builder /app/snapshot-dependencies/ /tmp/app/snapshot-dependencies/
COPY --from=builder /app/application/ /tmp/app/application/

# Remove arquivos desnecessários
RUN find /tmp/app -name "*.md" -delete \
    && find /tmp/app -name "*.txt" -delete \
    && find /tmp/app -name "*.properties" ! -name "application*.properties" -delete \
    && find /tmp/app -name "*.xml" ! -name "pom.xml" -delete \
    && find /tmp/app -name "*.yml" ! -name "application*.yml" -delete \
    && find /tmp/app -name "*.yaml" ! -name "application*.yaml" -delete \
    && find /tmp/app -path "*/META-INF/maven" -type d -exec rm -rf {} + 2>/dev/null || true \
    && find /tmp/app -name "module-info.class" -delete \
    && echo "Cleaned files, final sizes:" \
    && du -sh /tmp/app/*

# Imagem final ultra-otimizada com distroless
FROM gcr.io/distroless/java21-debian12:nonroot

# Timezone
ENV TZ=America/Sao_Paulo

WORKDIR /app

# Copiar camadas do Spring Boot em ordem de cache otimizada (já limpas)
COPY --from=cleaner /tmp/app/dependencies/ ./
COPY --from=cleaner /tmp/app/spring-boot-loader/ ./
COPY --from=cleaner /tmp/app/snapshot-dependencies/ ./
COPY --from=cleaner /tmp/app/application/ ./

EXPOSE 8080

# JVM otimizada para containers pequenos e distroless
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseZGC \
               -XX:+UseStringDeduplication \
               -XX:+UseCompressedOops \
               -XX:+UseCompressedClassPointers \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.jmx.enabled=false \
               -Dspring.output.ansi.enabled=NEVER \
               -Dfile.encoding=UTF-8"

# Usar JarLauncher diretamente (mais eficiente que shell)
ENTRYPOINT ["java", "-server", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseZGC", "-XX:+UseStringDeduplication", "-XX:+UseCompressedOops", "-XX:+UseCompressedClassPointers", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.jmx.enabled=false", "-Dspring.output.ansi.enabled=NEVER", "-Dfile.encoding=UTF-8", "org.springframework.boot.loader.launch.JarLauncher"]
