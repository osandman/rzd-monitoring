FROM openjdk:17-alpine
#ARG JAR_FILE=target/*.jar
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
#WORKDIR /app
#COPY ${JAR_FILE} rzd-monitoring.jar
#ENTRYPOINT ["java","-jar","/app/app.jar"]
ENTRYPOINT ["java","-cp","app:app/lib/*","net.osandman.rzdmonitoring.RzdMonitoringApplication"]
