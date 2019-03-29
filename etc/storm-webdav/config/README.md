# Trusted OAuth/OpenID connect token issuers configuration

The `application.yml` file can be used to override settings embedded in the
storm-webdav configuration.

It's mainly used to configure the list of trusted OAuth/OpenID Connect token issuers.

In order to enable an OAuth token issuer in the context of a storage area, such
issuer must be included in the list of trusted issuer specified in this file 
__and__ in the list of trusted organizations for the storage area in the storage area
properties in /etc/storm/webdav/sa.d.

## Example configuration

To trust the `super-provider.example` OAuth/OpenID Connect provider for
the storage area `example`, such provider  must be listed among the trusted
token issuers in `/etc/storm/webdav/config/application.yml:`

```yaml
oauth:
  issuers:
    - name: super-provider
      issuer: https://super-provider.example/
```

**And** in the storage area configuration `/etc/storm/sa.d/example.properties`:

```properties
name=example
rootPath=/storage/example
accessPoints=/example
vos=example
orgs=https://super-provider.example/
```
