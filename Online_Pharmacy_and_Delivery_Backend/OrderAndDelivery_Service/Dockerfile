FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR_NAME
COPY --from=build /app/target/${JAR_NAME} app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]