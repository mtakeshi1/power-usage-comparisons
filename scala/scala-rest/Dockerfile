FROM amazoncorretto:20

COPY ./app/build/libs/app-all.jar /app-all.jar

EXPOSE 8080

CMD ["java", "-jar", "-Xmx4g", "-Xms4g", "/app-all.jar"]
