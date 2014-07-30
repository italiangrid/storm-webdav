# StoRM WebDAV service refactoring

## Apporach

Use milton only for webdav methods.
Use jetty code for everything else.

One servlet for storage area access point maps the context to the storage area
root path.

org.italiangrid.storm.webdav.fs.FilesystemAccess provides the low-level
filesystem management logic, which is mediated by the milton code.

## Configuration

### Service
Main service configuration is now defined via environment variables.

See:

org.italiangrid.storm.webdav.config.ServiceConfiguration
org.italiangrid.storm.webdav.config.ServiceEnvConfiguration

### Storage areas

Storage area configuration will be parsed from a directory (configurable via an
environment variable).

For each storage area a simple properties file is defined.
So storage area CMS will have the cms.properties file.

See: org.italiangrid.storm.webdav.config.StorageAreaInfo

We use the owner library to parse properties:
http://owner.aeonbits.org/

Access policy configuration will be added here as well for the simple auhtz
scenario we have now (readable for everybody, client-authentication, voms).



