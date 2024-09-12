FROM eclipse-temurin:17

WORKDIR /usr/src/app
COPY --from=build /usr/src/app/target/devops.jar /usr/src/app/devops.jar
# COPY target/devops.jar devops.jar
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/usr/src/app/devops.jar"]