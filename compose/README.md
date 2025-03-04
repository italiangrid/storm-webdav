<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# Docker compose for StoRM WebDAV

Run the services with

```
$ docker-compose up -d
```

The docker-compose contains several services:

* `trust`: docker image for the GRID CA certificates, mounted in the `/etc/grid-security/certificates` path of the other services. The _igi-test-ca_ used in this deployment is also present in that path
* `storage-setup`: sidecar container, used to allocate proper volumes (i.e. storage areas) owned by _storm_
* `webdav`: is the main service, also known as StoRM WebDAV. The StoRM WebDAV base URL is https://storm.test.example:8443. It serves the following storage areas:
  * `test.vo` for users presenting a proxy issued by a _test.vo_ VO
  * `noauth`: which allows read/write mode also to anonymous users
  * `fga`: for a fined grained authorization storage area. Its access policies are set in the [application](./assets/etc/storm/webdav/config/application-policies.yml) file
  * `oauth-authz`: for users presenting a token issued by the [IAM DEV](https://iam-dev.cloud.cnaf.infn.it)
* `ts`: used for running the StoRM WebDAV testsuite. It shares the storage with the `webdav` service, to run local tests
* `nginx`: is the NGINX service, used as remote StoRM server for WebDAV calls. It does not forward requests to StoRM WebDAV, but just serves local resources in a separate storage. URL of this service is https://storm-alias.test.example. In the testsuite, the local resources are served by an `oauth-authz` endpoint, that does not require authentication.

To resolve the hostname of the service, add a line in your `/etc/hosts` file with

```
127.0.0.1	storm.test.example    storm-alias.test.example
```
