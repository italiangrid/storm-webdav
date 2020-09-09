# Custom StoRM WebDAV configuration

The `application.yml` file can be used to override settings embedded in the
storm-webdav configuration.

It's mainly used to:

- configure the list of trusted OAuth token issuers
- configure the list of trusted OpenID Connect providers (since version 1.3.0)
- configure fine-grained authorization policies (since version 1.3.0)

## Trusted OAuth connect token issuers configuration

In order to enable an OAuth token issuer in the context of a storage area, such
issuer must be included in the list of trusted issuer specified in this file
**and** in the list of trusted organizations for the storage area in the storage area
properties in /etc/storm/webdav/sa.d.

### OAuth authorization example configuration

To trust the `super-provider.example` OAuth/OpenID Connect provider for
the storage area `example`, such provider must be listed among the trusted
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

## OpenID connect authentication

Starting with version 1.3.0, StoRM WebDAV supports OpenID Connect
authentication on its storage area browser application (i.e., the thing you get
when you point your browser to a StoRM WebDAV instance and that you use to
browse the storage area contents).

To enable OpenID Connect authentication you need:

- a trusted OpenID Connect provider, e.g. INDIGO IAM for WLCG
- a client registered in such provider with the `authorization_code` grant type
  enabled; instructions on how to register a client are typically provided by
  the OpenID provider

OpenID Connect authentication must be explicitly enabled in
`/etc/storm-webdav/config/application.yml`, as follows:

```yaml
oauth:
  enable-oidc: true
```

StoRM WebDAV leverages the [OpenID Connect support provider by Spring security
libraries][spring-oidc-support]. Below an example is given for the WLCG IAM
OpenID Connect provider:

```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          wlcg:
            issuer-uri: https://wlcg.cloud.cnaf.infn.it/
        registration:
          wlcg:
            provider: wlcg
            client-name: WLCG IAM
            client-id: PUT_YOUR_CLIENT_ID_HERE
            client-secret: PUT_YOUR_CLIENT_SECRET_HERE
            scope:
              - openid
              - profile
              - wlcg.groups
```

## Fine-grained authorization

Starting with version 1.3.0, StoRM WebDAV provides support for fine-grained authorization on storage areas.

Before 1.3.0, authorization was coarse-grained, i.e. read and write privileges on a storage area were granted to a VOMS VO or OAuth trusted issuer, and those privileges were applied to the whole storage area, i.e. it was not possible to have different authorization policies on folders or on specific paths, or to grant different privileges to specific groups or subjects.

Fine-grained authorization can be enabled on a storage area as follows:

```properties
name=example
rootPath=/storage/example
...
fineGrainedAuthzEnabled=true
```

Then authorization policies must be included in the `/etc/storm/webdav/config/application.yml` configuration. Example policies are given below:

```yaml
storm:
  authz:
    policies:
    - sa: example
      actions:
      - list
      - read
      effect: permit
      description: Grant read access to users in the /example/read group
      principals:
      - type: oauth-group
        params:
          iss: https://vo.example/
          group: /example/read
    - sa: example
      actions:
      - all
      effect: permit
      description: Grant write access to /example/write-area to /example/write users
      principals:
      - type: oauth-group
        params:
          iss: https://vo.example/
          group: /example/write
      paths:
       - /write-area/**
```

The first policy grants read access to the whole `example` storage area to members
of group `/example/read`, as asserted by the trusted OAuth token issuer `https://vo.example`.

The second policy grants all privileges to members of group `/example/write` to the path `/write-area`,
and all its subpaths, as defined in the context of the storage area, i.e. below any of the access points defined for that storage
area. So, assuming the the `example` storage area is configured to have a single access point, `/example`, the policy will
apply to `/example/write-area` and all its subpath.

### Fine-grained authorization policies

A fine-grained authorization policy defines access rules to directory and files in the context of a storage area.
In detail, an access policy defines access rules that regulate which actions on which resources are allowed for a set of principals.
Actions, in this context, are read, write, list and delete operations on files and directories. Resources are files and directories. Principals are authenticated entities (i.e., users or agents).

Policies are defined in the context of a storage area, and are applied in order following a first-applicable principle.
This means that the first policy found in the list that matches an incoming request will be applied when rendering an authorization
decision.

A policy matches an incoming request when the resources, actions and principal in the policy match the target resource action
and principal in the request.

An example, annotated policy is given below:

```yaml
policies:
    // The storage area that will apply to this policy
    - sa: example
      actions:
      // An array of actions regulated by this policy, possible values are
      // read, write, delete, list, all
      - list
      - read
      // The policy effect. Possible values are 'permit', 'deny'
      effect: permit
      // A policy description.
      description: Grant read access to selected vo users
      // List of paths that this policy will apply to.
      // Paths are specific to the storage area, i.e. are relative to the
      // storage area access points. 
      // Example:
      // In a storage area 'example', that as the '/example/' access point,
      // in order to match the '/example/read-only' path and its subpaths, the
      // path expression included in the policy would be '/read-only/**'
      // '**' matches any file or directory, at any level of nesting, of a given path
      // '*' matches any file or directory contained in a given path
      // The matching rules follow the Spring AntPath matcher conventions, see:
      // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html
      paths:
      - /read-only/**
      // The list of principals this policy will apply to. Below examples of principals 
      // are given
      principals:
      // Anymous principal, will match users not authenticated
      - type: anonymous 
      // Any authenticated user, will match users that have been authenticated
      // (with any supported mechanism, i.e. X.509, OAuth or OIDC)
      - type: any-authenticated-user
      // anyone, will match any principal
      - type: anyone
      // a VOMS fully qualified attribute name
      - type: fqan
        params:
          fqan: /test.vo/Role=admin
      // A VOMS vo
      - type: vo
        params:
          vo: dteam
      // An OAuth/OpenID Connect group, as asserted by a trusted OAuth/OpenID Connect token issuer
      - type: oauth-group
        params:
          iss: https://wlcg.cloud.cnaf.infn.it/
          group: /wlcg/xfers
      // An OAuth/OpenID Connect scope, as asserted by a trusted OAuth/OpenID Connect token issuer
      - type: oauth-scope
        params:
          iss: https://wlcg.cloud.cnaf.infn.it/
          scope: /storage.read:/
      // An OAuth/OpenID Connect subject, as asserted by a trusted OAuth/OpenID Connect token issuer
      - type: oidc-subject
        params:
          iss: https://wlcg.cloud.cnaf.infn.it/
          sub: a1b98335-9649-4fb0-961d-5a49ce108d49
```

[spring-oidc-support]: https://docs.spring.io/spring-boot/docs/2.1.12.RELEASE/reference/html/boot-features-security.html#boot-features-security-oauth2-client
