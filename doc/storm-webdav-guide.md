<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# StoRM WebDAV installation and configuration guide

## Introduction

The StoRM WebDAV service provides a storage management and data access solution
supporting VOMS and OAuth/OpenID Connect authentication and authorization
mechanisms.

Starting from version 1.1.0, StoRM WebDAV supports third-party WebDAV COPY
transfers (see [here][doma-tpc] for technical details).

## Install the service package

Grap the latest package from the StoRM repository. See instructions
[here](https://italiangrid.github.io/storm/download.html).

```bash
yum install storm-webdav
```

## Service configuration defaults

### `/etc/systemd/system/storm-webdav.service.d/storm-webdav.conf`

The storm-webdav service configuration lives in this file. Typically the
configuration works out of the box, but changes are required for instance to
enable third-party transfer support.

StoRM can also be configured using (one or more) YAML files.

You can find an empty YAML configuration file in
`/etc/storm/webdav/config/application.yml` together with a `README.md` file in
the same directory that provides configuration instructions.

That configuration file is used to override settings in the configuration file
embedded in the storm webdav jar package:

https://github.com/italiangrid/storm-webdav/blob/master/src/main/resources/application.yml

that you can consult to see what are the default settings.

### Memory

You should give a reasonable amount of memory to StoRM WebDAV to do its work.
The amount depends on the number of concurrent requests that the server needs
to handle.

A good starting point is giving the server 2G of heap memory, by setting the
following env variable:

```env
STORM_WEBDAV_JVM_OPTS=-Xms2048m -Xmx2048m
```

In general, allowing for `256Mb + (# threads * 6Mb)` should give StoRM WebDAV
enough memory to do its work.

### Threadpool sizes

The size of the thread pool used to serve incoming requests and
third-party-copy requests can be set with the following variables:

```yaml
tpc:
  max-connections: 200
  max-connections-per-route: 150
  progress-report-thread-pool-size: (# of cores of your machine)

storm:
  connector:
    max-connections: 300
    max-queue-size: 900
```

### Conscrypt

Conscrypt improves TLS performance and can be enabled as follows:

```yaml
tpc:
  use-conscrypt: true

storm:
  tls:
    use-conscrypt: true
    enable-http2: true
```

### Use `/dev/urandom` for random number generation

Using `/dev/random` can lead to the service being blocked if not enough entropy
is available in the system.

To avoid this scenario, use `/dev/urandom`, by setting the JVM options as
follows:

```env
STORM_WEBDAV_JVM_OPTS=-Xms2048m -Xmx2048m -Djava.security.egd=file:/dev/./urandom
```
## Configure the service with Puppet

The [StoRM puppet module][storm-puppet] can be used to configure the service on 
CENTOS 7.  

We recommend to use directly the StoRM WebDAV YAML configuration to tune your
deployment configuration, instead of using variables defined at the puppet
level, i.e.:

```puppet
class { 'storm::webdav':
  hostnames => ['storm-webdav.test.example'],
}
...
storm::webdav::application_file { 'application.yml':
  source => 'puppet:///the/path/to/the/application.yml',
}

```

#### VO mapfiles

When VO map files are enabled, users can authenticate to the StoRM webdav
service using the certificate in their browser and be granted VOMS attributes
if their subject is listed in one of the supported VO mapfile. You can
configure whether users listed in VO map files will be granted read-only or
write permissions in the storage area configuration in the
`/etc/storm/webdav/sa.d` directory.

This mechanism is very similar to the traditional Gridmap file but is just used
to know whether a given user is registered as a member in a VOMS managed VO and
not to map his/her certificate subject to a local unix account.

##### How to enable VO map files

VO map files support is disabled by default in StoRM WebDAV.

Set `STORM_WEBDAV_VO_MAP_FILES_ENABLE=true` in 

`/etc/systemd/system/storm-webdav.service.d/storm-webdav.conf` to enable VO map file support.

### VO map files format and location

A VO map file is a csv file listing a certificate subject, issuer and email for each line.
It can be easily generated for a given VO using the `voms-admin` command line utility.
VO map files by default live in the `/etc/storm/webdav/vo-mapfiles.d` directory.

For each VO, a file named:

`VONAME.vomap`

is put in the `/etc/storm/webdav/vo-mapfiles.d` directory. 

##### VO Map file examples

The file `/etc/storm/webdav/vo-mapfiles.d/test.vomap` with the following content:

```csv
/C=IT/O=INFN/OU=Personal Certificate/L=CNAF/CN=Andrea Ceccanti,/C=IT/O=INFN/CN=INFN CA,andrea.ceccanti@cnaf.infn.it
/C=IT/O=INFN/OU=Personal Certificate/L=CNAF/CN=Enrico Vianello,/C=IT/O=INFN/CN=INFN CA,enrico.vianello@cnaf.infn.it
```

will grant the `test` VO membership to clients authenticated with the above subjects.

To generate a VO mapfile for the `cms` VO, you could run the following command

```bash
voms-admin --host voms.cern.ch --vo cms list-users > /etc/storm/webdav/vo-mapfiles.d/cms.vomap
```

### Storage area configuration

StoRM WebDAV service configuration lives in the directory `/etc/storm/webdav`.
See [Storage area configuration][storage-area-conf] for more information.

## Service operation

### Starting and stopping the service

Start the service:

```
systemct start storm-webdav
```

Stop the service:

```
systemctl stop storm-webdav
```

Check service status:

```
systemctl status storm-webdav
```

Check that the service responds:

```
$ curl http://localhost:8085/.storm-webdav/actuator/health
{"status":"UP"}
```

Get service metrics:

```
# curl http://localhost:8085/status/metrics?pretty=true
{
  "version" : "4.0.0",
  "gauges" : {
    "jvm.gc.G1-Old-Generation.count" : {
      "value" : 0
    },
    "jvm.gc.G1-Old-Generation.time" : {
      "value" : 0
    }
    ...
}

```

### Service logs

The service logs live in the `/var/log/storm/webdav` directory.

- `storm-webdav-server.log` provides the main service log
- `storm-webdav-server-access.log` provides an http access log

### Access points

By default a storage area named `sa` is accessible at the URL
`https://hostname:8443/sa` or, if anonymous access is granted, at
`http://hostname:8085/sa`

[doma-tpc]: https://twiki.cern.ch/twiki/bin/view/LCG/HttpTpcTechnical
[storm-puppet]: https://github.com/italiangrid/storm-puppet-module
[storage-area-conf]: storage-area-configuration.md
