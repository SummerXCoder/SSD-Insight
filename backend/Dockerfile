# ============================================================
# Dockerfile for Meeting Room Reservation System (Backend)
# Multi-stage build: builder → runtime
# ============================================================

# -------------------- builder 阶段 --------------------
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# 第一步：仅复制依赖描述文件，利用 Docker 层缓存优化依赖下载
COPY pom.xml .

# 使用 BuildKit 缓存挂载加速 Maven 依赖下载
# --mount=type=cache 确保 .m2/repository 在多次构建间复用
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B

# 第二步：复制源码并执行构建
COPY src ./src

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn package -DskipTests -B \
    && JAR_FILE=$(find target -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print -quit) \
    && mv "$JAR_FILE" /app.jar

# -------------------- runtime 阶段 --------------------
FROM eclipse-temurin:17-jre-jammy

# 创建非 root 用户运行应用
RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --shell /bin/bash --create-home appuser

WORKDIR /app

# 仅从 builder 阶段复制最终可执行 JAR
COPY --from=builder --chown=appuser:appgroup /app.jar ./app.jar

# 创建日志目录并赋权
RUN mkdir -p /app/logs \
    && chown -R appuser:appgroup /app/logs

# 切换到非 root 用户
USER appuser

# 暴露端口（依据 application.yml 中 server.port=8080）
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 使用 exec form 启动，确保 Java 进程接收信号
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]
