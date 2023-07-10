FROM openjdk:11-jre-slim
COPY ./build/libs/specieslist-webapp-4.2.0-SNAPSHOT.war app.war
CMD ["java", "-jar", "-Dserver.port=8080", "app.war"]