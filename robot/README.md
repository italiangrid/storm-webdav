# StoRM WebDAV robot testsuite

## Run test suite

The [compose](../compose/README.md) folder already includes all services necessary to run the testsuite.

Start all services with

```
cd compose
docker-compose up -d
```

In case of _Error response from daemon: failed to create symlink_ error, please run the `trust` container first.

Enter into the testsuite container with

```
docker-compose exec ts bash 
```

To perform token based authorization, the testsuite requires a valid access token
released by the [WLCG IAM](https://wlcg.cloud.cnaf.infn.it) trough a public client
with the client credentials authorization grant enabled.

Please set the following environment variable to allow the token credential setup

```
export IAM_CLIENT_ID=<client-id>
```

Set the minimum arguments required by the testsuite to run against the two StoRM WebDAV
servers deployed into the compose:

```
export ROBOT_ARGS="--variable dav.host:storm.test.example --variable remote.dav.host:storm-alias.test.example --variable remote.davs.port:443"
```

Now you can run the test suite with

```
/scripts/ci-run-testsuite.sh
```

The default path for the test suite report is `/home/test/robot/reports`;
in case you want to copy it locally, run

```
docker cp storm-webdav-ts-1:/home/test/robot/reports .
```

### Testsuite parameters

| Parameter name | Description                        | Default value                                                                                                    |
| -------------- | ---------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `dav.host`     | Hostname of the WebDAV server considered as running locally  | localhost                                                                                             |
| `dav.port`     | Schema of the WebDAV server considered as running locally | 8085                                                                                             |
| `davs.port`     | Schema of the WebDAV server considered as running locally with HTTPS | 8443                                                                                             |
| `remote.dav.host`     | Hostname of the WebDAV server considered as running remotely  | localhost                                                                                             |
| `remote.dav.port`     | Schema of the WebDAV server considered as running remotely | 8085                                                                                             |
| `remote.davs.port`     | Schema of the WebDAV server considered as running remotely with HTTPS | 8443                                                                                             |
| `token.endpoint`     | WebDAV endpoint for the locally issued tokens | https://localhost:8443/oauth/token                                                                                             |
| `cred.oauth.env_var_name`     | Environment variable for an OAuth access token | IAM_ACCESS_TOKEN                                                                                             |
| `cred.voms.use_os`     | Use `/tmp/x509up_u<user-id>` as proxy path | True                                                                                             |
| `cred.voms.default`     | Custom path to proxy (when `cred.voms.use_os=false`) | assets/certs/voms.1                                                                                               |

For other parameters, see the [variables file](./test/variables.robot).