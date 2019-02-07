FROM centos:6

ARG STORM_USER_UID=1000

RUN \
  yum -y install epel-release redhat-lsb-core wget git tar && \
  yum update -y && \
  yum install -y gfal2-all davix attr acl storm-webdav nc python34 && \
  yum install -y python34-devel && \
  yum install -y python34-setuptools && \
  python3 /usr/lib/python3.4/site-packages/easy_install.py pip && \
  pip3 install --upgrade pip && \
  pip3 install robotframework && pip3 install --upgrade robotframework-httplibrary && \
  yum install -y java-1.8.0-openjdk-devel && \
  rpm --import http://repository.egi.eu/sw/production/umd/UMD-RPM-PGP-KEY && \
  yum install -y http://repository.egi.eu/sw/production/umd/4/sl6/x86_64/updates/umd-release-4.1.3-1.el6.noarch.rpm && \
  adduser --uid ${STORM_USER_UID} storm && \
  wget https://repo.cloud.cnaf.infn.it/repository/storm/stable/storm-stable-centos6.repo -O /etc/yum.repos.d/storm-stable-centos6.repo && \

  yum install -y storm-webdav voms-clients-java jq


ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]
