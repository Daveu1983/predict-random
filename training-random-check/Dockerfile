FROM eclipse-temurin:17-jre
WORKDIR /work/
COPY target/*-runner.jar /work/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/work/app.jar"]

