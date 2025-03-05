# https://spring.io/guides/topicals/spring-boot-docker#_multi_stage_build
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app
RUN apk add maven
COPY pom.xml .
RUN mvn dependency:resolve
RUN mvn dependency:resolve-plugins
COPY .git .git
COPY etc etc
COPY src src
RUN mvn package -Dmaven.test.skip
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-alpine
ENV STORM_WEBDAV_JVM_OPTS="-Dspring.profiles.active=dev"
ARG DEPENDENCY=/workspace/app/target/dependency

#WORKDIR /app
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY src src

ARG USERNAME=storm
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

RUN apk add --no-cache curl
RUN addgroup --gid ${USER_GID} ${USERNAME}
RUN adduser --uid ${USER_UID} --ingroup ${USERNAME} ${USERNAME} --disabled-password
RUN echo ${USERNAME} ALL=\(root\) NOPASSWD:ALL > /etc/sudoers
RUN chmod 0440 /etc/sudoers
USER ${USERNAME}

ENTRYPOINT java ${STORM_WEBDAV_JVM_OPTS} -cp app:app/lib/* org.italiangrid.storm.webdav.WebdavService
