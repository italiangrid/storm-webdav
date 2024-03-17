# https://spring.io/guides/topicals/spring-boot-docker#_multi_stage_build
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app
RUN apk add maven
COPY pom.xml .
COPY maven maven
RUN mvn dependency:resolve -s maven/cnaf-mirror-settings.xml
RUN mvn dependency:resolve-plugins -s maven/cnaf-mirror-settings.xml
COPY .git .git
COPY etc etc
COPY src src
RUN mvn package -s maven/cnaf-mirror-settings.xml -Dmaven.test.skip
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY src src
EXPOSE 8086
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-cp","app:app/lib/*","org.italiangrid.storm.webdav.WebdavService"]
