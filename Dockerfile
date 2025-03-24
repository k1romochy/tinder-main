FROM maven:3.9-eclipse-temurin-17 AS build
 WORKDIR /app
 COPY pom.xml .
 RUN mvn dependency:go-offline
 
 COPY src ./src
 RUN mvn package -DskipTests
 
 FROM eclipse-temurin:17
 WORKDIR /app
 COPY --from=build /app/target/*.jar app.jar
 
 ENV SPRING_PROFILES_ACTIVE=prod
 ENV JAVA_OPTS=""
 
 EXPOSE 8080
 ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]