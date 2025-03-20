<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

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

To perform token based authorization, the testsuite requires a valid oidc-agent client
registered in the [IAM DEV](https://iam-dev.cloud.cnaf.infn.it).

Please set the following environment variable to allow the token credential setup

```
export OIDC_AGENT_ALIAS=<your-client-alias>
export OIDC_AGENT_SECRET=<your-client-secret>
```

You may want to customize the testsuite run to set some variable or some argument for the robot
command, such as

```
export ROBOT_ARGS="-L DEBUG --exclude known-issue"
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
| `dav.host`     | Hostname of the WebDAV server considered as running locally  | storm.test.example                                                                                             |
| `dav.port`     | Schema of the WebDAV server considered as running locally | 8085                                                                                             |
| `davs.port`     | Schema of the WebDAV server considered as running locally with HTTPS | 8443                                                                                             |
| `remote.dav.host`     | Hostname of the WebDAV server considered as running remotely  | storm-alias.test.example                                                                                             |
| `remote.dav.port`     | Schema of the WebDAV server considered as running remotely | 80                                                                                             |
| `remote.davs.port`     | Schema of the WebDAV server considered as running remotely with HTTPS | 443                                                                                             |
| `token.endpoint`     | WebDAV endpoint for the locally issued tokens | https://localhost:8443/.storm-webdav/oauth/token                                                                                             |
| `cred.oauth.env_var_name`     | Environment variable for an OAuth access token | IAM_ACCESS_TOKEN                                                                                             |
| `cred.voms.use_os`     | Use `/tmp/x509up_u<user-id>` as proxy path | True                                                                                             |
| `oidc-agent.alias`     | Alias for the oidc-agent client | dev-wlcg                                                                                               |
| `oauth.group.claim`     | Claim for the token group | wlcg.groups                                                                                               |
| `oauth.optional.group.claim`     | Claim for the optional token group. In IAM, optional groups appears in the token only if explicitly requested | wlcg.groups:/data-manager                                                                                               |

For other parameters, see the [variables file](./test/variables.robot).


### Enable custom token issuers

In order for authorization tests being executed with custom token issuers, one needs to modify
the StoRM WebDAV configuration as follow:

* append the custom token issuer among the `orgs` comma separated list, in the [fga.property](../compose/assets/etc/storm/webdav/sa.d/fga.properties) and [oauth-authz.properties](../compose/assets/etc/storm/webdav/sa.d/oauth-authz.properties) files
* include the custom token issuer in the [application-issuers.yml](../compose/assets/etc/storm/webdav/config/application-issuers.yml) file
* write down authorization policies for the `fga` storage area indicating your token issuer in the [application-policies.yml](../compose/assets/etc/storm/webdav/config/application-policies.yml) file. The default behavior is:
  * users presenting a VOMS proxy released by a `test.vo` can read/write in the SA
  * anyone can read in the `/public` folder and sub-folders
  * users presenting a JWT token which embeds the `/cms` group have read/write access in the `/cms` folder and sub-folders
  * users presenting a JWT token which embeds the `/data-manager` group have read/write access in the SA.

In case the group claim in your token is not `wlcg.groups`, please append among the `ROBOT_ARGS`

```
--variable oauth.group.claim:<your-token-group-claim> --variable oauth.optional.group.claim:<may-be-equal-to-oauth.group.claim>
```

Remember to set the proper oidc-agent alias appending also

```
--variable oidc-agent.alias:<your-client-alias>
```
