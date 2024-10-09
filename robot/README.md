# StoRM WebDAV robot testsuite

## Run test suite

The `compose` folder already includes all services necessary to run the testsuite.

Start all services with

```
cd compose
docker-compose up -d
```

Now you can run the test suite with

```
docker-compose exec ts bash -c '/scripts/ci-run-testsuite.sh'
```

The default path for the test suite report is `/home/test/robot/reports`;
in case you want to copy it locally run

```
docker cp storm-webdav-ts-1:/home/test/robot/reports .
```