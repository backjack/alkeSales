FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY frontend/package.json frontend/package-lock.json frontend/
COPY frontend/angular.json frontend/tsconfig.json frontend/tsconfig.app.json frontend/
COPY frontend/src frontend/src
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/alkeSales.jar /app/alkeSales.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/alkeSales.jar"]
