FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml .

RUN mvn dependency:go-offline -q

COPY src ./src

RUN mvn clean package -DskipTests -q


FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S finance && \
    adduser -S finance -G finance

WORKDIR /app

COPY --from=builder /app/target/finance-dashboard-2.0.0.jar app.jar

RUN mkdir -p /app/uploads && \
    chown -R finance:finance /app

USER finance

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Xms128m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]