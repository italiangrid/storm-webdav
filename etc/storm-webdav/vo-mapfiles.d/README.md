<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

## VO map files configuration
VO map files contains the list of the members of a VOMS-managed Virtual Organization (VO).

## What are VO map files

When VO map files are enabled, users can authenticate to the StoRM webdav
service using the certificate in their browser and be granted VOMS attributes
if their subject is listed in one of the supported VO mapfile.

This mechanism is very similar to the traditional Gridmap file but is just used
to know whether a given user is registered as a member in a VOMS managed VO and
not to map his/her certificate subject to a local unix account.

### How to enable VO map files

VO map files support is disabled by default in StoRM WebDAV.

Set `STORM_WEBDAV_VO_MAP_FILES_ENABLE=true`` in /etc/sysconfig/storm-webdav
to enable VO map file support.

### How to generate VO map files

VO map files are generated using the voms-admin list-users command.

For each supported VO, a file named:

<voname>.vomap

is put in the voms-mapfiles.d directory.

*Example*: to generate a VO mapfile for the `cms` VO, run the following
command

```bash
voms-admin --vo cms list-users > /etc/storm/webdav/vo-mapfiles.d/cms.vomap
```

*N.B.:* Ensure that vo map files are readable by the user that runs the StORM
WebDAV service (by default, the `storm` user).
