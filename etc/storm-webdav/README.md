# This is the StoRM WebDAV service configuration directory

The logback.xml file is used to configure the logging verbosity of the StoRM
WebDAV service.

The logging configuration is monitored by the StoRM WebDAV service, so changes
will be applied to the logging configuration without the need to restart the
service.

The logback-access.xml file is used to configure the service access log, and
should be normally left unchanged.

## Storage areas configuration
Storage area configuration lives in the `sa.d` directory.
For more information see the README.md file there.

## VOMS map files configuration
VOMS map files contains the list of VO members as obtained by running the
voms-admin list-users command.

When VOMS mapfiles are enabled, users can authenticate to the StoRM webdav
service using the certificate in their browser and be granted VOMS attributes
if their subject is listed in one of the supported VOMS mapfile.

For each supported VO, a file having the same name as the VO is put in the
voms-mapfiles directory.

*Example*: to generate a VOMS mapfile for the `cms` VO, run the following
command

```bash
voms-admin --vo cms list-users > cms
```
