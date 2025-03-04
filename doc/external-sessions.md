<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# Externalized session support

Starting with version 1.4.0, StoRM WebDAV supports storing HTTP session 
information in an external [redis][redis] server.

This can be useful when deploying multiple replicas of the StoRM WebDAV
service. In particular, it is recommended to configure external session storage
when OpenID Connect authentication is enabled for some storage area.

## Configuring support for externalized sessions 

Externalized session support can be enabled by adding the following
configuration to the `/etc/storm/webdav/config/application.yaml` file:

```yaml
spring:
  session:
    store-type: redis

  data:
    redis:
      host: redis.host.example
      port: 6379

management:
  health:
    redis:
      enabled: true
```

For other redis connection configuration options, see the [Spring boot reference guide][spring-boot-reference].

[redis]: https://redis.io/
[spring-boot-reference]: https://docs.spring.io/spring-boot/appendix/application-properties/index.html#appendix.application-properties.data
