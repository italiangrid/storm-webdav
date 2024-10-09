# How to run the test suite locally

## Build service

The [docker image](../docker/webdav-centos7/Dockerfile) used as WebDAV service
for Centos7 OS downloads the `storm-webdav` server from the
[nightly repo][nightly-repo].

In case a newer version of the `storm-webdav` server is uploaded to the [nightly repo][nightly-repo]
but not to [dockerHub](https://hub.docker.com/r/italiangrid/storm-webdav-centos7),
please build the image manually, with

```
cd docker/webdav-centos7
docker build -t italiangrid/storm-webdav-centos7 .
```

## Run test suite

Setup and run the services with

```
cd compose
docker-compose up trust # and wait for fetch crl to be done
docker-compose up -d ts nginx
```

Now you can run the test suite with

```
docker-compose exec ts bash -c '/scripts/ci-run-testsuite.sh'
```

The default path for the test suite report is `/home/test/robot/reports`;
in case you want to copy it locally run

```
docker cp storm-webdav_ts_1:/home/test/robot/reports .
```

[nightly-repo]: https://repo.cloud.cnaf.infn.it/repository/storm/storm-nightly-centos7.repo