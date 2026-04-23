FROM eclipse-temurin:21-jre
WORKDIR /work/
COPY target/*-runner.jar /work/app.jar
COPY random-model.json /work/random-model.json
EXPOSE 8080
CMD ["java", "-jar", "/work/app.jar"]

