<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# This is the StoRM WebDAV service configuration directory

The logback.xml file is used to configure the logging verbosity of the StoRM
WebDAV service.

The logging configuration is monitored by the StoRM WebDAV service, so changes
will be applied to the logging configuration without the need to restart the
service.

## Storage areas configuration

Storage area configuration lives in the `sa.d` directory.
For more information see the README.md file there.
