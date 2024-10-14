# https://spring.io/guides/topicals/spring-boot-docker#_multi_stage_build
FROM eclipse-temurin:11-jdk-alpine as build
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

FROM eclipse-temurin:11-centos7
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

RUN groupadd --gid ${USER_GID} ${USERNAME}
RUN useradd --uid ${USER_UID} --gid ${USER_GID} -m ${USERNAME}
RUN echo ${USERNAME} ALL=\(root\) NOPASSWD:ALL > /etc/sudoers
RUN chmod 0440 /etc/sudoers
USER ${USERNAME}

ENTRYPOINT java ${STORM_WEBDAV_JVM_OPTS} -cp app:app/lib/* org.italiangrid.storm.webdav.WebdavService
