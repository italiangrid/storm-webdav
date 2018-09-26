FROM italiangrid/base-dev

RUN \
  yum update -y && \
  yum install -y attr acl storm-webdav

COPY setup /
RUN chmod 755 "/run.sh"

CMD /run.sh
