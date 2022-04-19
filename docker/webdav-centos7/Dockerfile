FROM centos:7

ENV TINI_VERSION v0.18.0
ARG STORM_USER_UID=1000

RUN \
  yum -y install epel-release redhat-lsb-core wget git tar && \
  yum update -y && \
  yum install -y yum-utils gfal2-all davix attr acl sudo && \
  echo '%wheel ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers && \
  rpm --import http://repository.egi.eu/sw/production/umd/UMD-RPM-PGP-KEY && \
  yum install -y http://repository.egi.eu/sw/production/umd/4/centos7/x86_64/updates/umd-release-4.1.3-1.el7.centos.noarch.rpm && \
  adduser --uid ${STORM_USER_UID} storm && \
  usermod -a -G wheel storm && \
  yum-config-manager --add-repo https://repo.cloud.cnaf.infn.it/repository/storm/storm-nightly-centos7.repo && \
  yum install -y storm-webdav voms-clients-java jq && \
  yum install -y java-11-openjdk-devel && yes 1 | update-alternatives --config java && \
  yum clean all

ADD ./run.sh /
RUN chmod +x /run.sh

ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

CMD /run.sh

USER storm
WORKDIR /etc/storm/webdav
