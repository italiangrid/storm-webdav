<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# StoRM WebDAV support for Third Party Copy transfers

Starting with version 1.1.0, StoRM WebDAV supports third party
copy transfers. 

## Configuring support for third-party transfers

Third party transfer support is enabled and active by default, but will work as
expected only when properly configured.

### Hostname configuration

Support for third-party transfers is implemented by supporting a COPY method
request where the Source or Destination header points to a remote resource.

In order to tell apart remote resources from local ones, StoRM webdav must be
configured accordingly.

This is done via the `STORM_WEBDAV_HOSTNAME_0`, `STORM_WEBDAV_HOSTNAME_1`, ...,
environment variables, which allow to define for which hostnames (and aliases)
the service is serving requests.

Example:

```
STORM_WEBDAV_HOSTNAME_0="ds-808.cr.cnaf.infn.it"
STORM_WEBDAV_HOSTNAME_1="gridhttps-storm-atlas.cr.cnaf.infn.it"
```

### OAuth authorization server configuration

The OAuth authorization server can be used by clients such as FTS to obtain an
OAuth access token that grants the same privileges as a VOMS credential. This
mechanism is currently used to implement a form of delegated authorization in
support of third-party transfers. 

The tokens issued by the StoRM WebDAV OAuth authorization servers will only be
accepted by the StoRM WebDAV service instances. In a replicated setup, just 
ensure that the OAuth server configuration is consistent across the replicas.

Below is an example of configuration:

```
# THe issuer linked to the issued tokens. This is typically the 'https://' followed
# by the storm webdav instance main alias.
STORM_WEBDAV_AUTHZ_SERVER_ISSUER="https://gridhttps-storm.atlas.cr.cnaf.infn.it"

# The secret used to sign the tokens. 
STORM_WEBDAV_AUTHZ_SERVER_SECRET="supersupersupersecret"

# The maximum token lifetime in seconds (43200 = 12 hours)
STORM_WEBDAV_AUTHZ_SERVER_MAX_TOKEN_LIFETIME_SEC="43200"

# This will allow access to token-based authorized clients over HTTPS, by 
# dropping the requirement for client certificate authentication
STORM_WEBDAV_REQUIRE_CLIENT_CERT="false"
```
For other configuration options, see the /etc/sysconfig/storm-webdav file.

## SciTags

StoRM WebDAV supports the `SciTag` header.
To correctly mark the network packets and/or network flows, you need to install [flowd](https://github.com/scitags/flowd) and configure it to use the `np_api` plugin.

Example flowd configuration (`/etc/flowd/flowd.cfg`):

```
PLUGIN='np_api'
BACKEND='udp_firefly'
FLOW_MAP_API='https://scitags.docs.cern.ch/api.json'
IP_DISCOVERY_ENABLED=True
```
