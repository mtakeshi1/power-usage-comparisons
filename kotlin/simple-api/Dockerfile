FROM amazoncorretto:20

COPY ./build/libs/mtakeshi1.githut.io.simple-api-all.jar /simple-api.jar

EXPOSE 8080

CMD ["java", "-jar", "-Xmx4g", "-Xms4g", "/simple-api.jar"]