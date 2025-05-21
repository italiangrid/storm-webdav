<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# StoRM WebDAV storage area configuration

Each storage area is configured by placing a properties file in the `/etc/storm/webdav/sa.d` directory.
StoRM WebDAV will look for configuration in all files ending with `.properties` in this directory. If no configuration files are found, the service will not start.

## Storage area configuration properties

| _Name_                     | _Description_                                                                                                                | _Required_ | _Example_                               | _Default_ |
| -------------------------- | ---------------------------------------------------------------------------------------------------------------------------- | ---------- | --------------------------------------- | --------- |
| `name`                     | The name of the storage area                                                                                                 | True       | `name=sa`                               | N/A       |
| `rootPath`                 | Root path for the storage area. Files will be served from this path.                                                         | True       | `rootPath=/storage/sa`                  | N/A       |
| `accessPoints`             | Comma separated list of storage area access points.                                                                          | True       | `accessPoints=/sa`                      | N/A       |
| `vos`                      | Comma-separated list of VOMS VOs trusted in this storage area                                                                | False      | `vos=atlas`                             | N/A       |
| `orgs`                     | Comma-separated list of OAuth/OpenID Connect token issuers trusted in this storage area                                      | False      | `orgs=https://wlcg.cloud.cnaf.infn.it/` | N/A       |
| `authenticatedReadEnabled` | Enables read access to users authenticated with an X.509 certificate issued by a trusted CA (users without VOMS credentials) | False      | `authenticatedReadEnabled=true`         | false     |
| `anonymousReadEnabled`     | Enables read access to anonymous users.                                                                                      | False      | `anonymousReadEnabled=false`            | false     |
| `voMapEnabled`             | Enables VO map files for this storage area.                                                                                  | False      | `voMapEnabled=false`                    | true      |
| `voMapGrantsWriteAccess`   | VO map files grants read-only access to storage area files. Use this flag to grant write access to VO map files users        | False      | `voMapGrantsWriteAccess=true`           | false     |
| `orgsGrantReadPermission`  | Enables read access to storage area files to users authenticated using OAuth/OIDC                                            | False      | `orgsGrantReadPermission=false`         | true      |
| `orgsGrantWritePermission` | Enables write access to storage area files to users authenticated using OAuth/OIDC                                           | False      | `orgsGrantWritePermission=false`        | false     |
| `wlcgScopeAuthzEnabled`    | Enables scope-based authorization following the rules imposed by the WLCG JWT profile                                        | False      | `wlcgScopeAuthzEnabled=true`            | false     |
| `fineGrainedAuthzEnabled`  | Enables fine-grained authorization engine for the storage area                                                               | False      | `fineGrainedAuthzEnabled=true`          | false     |
| `tapeEnabled`              | Tape enabled storage area (e.g. stub files and premigrate/migrated xattrs)                                                   | False      | `tapeEnabled=true`                      | false     |

## Authorization configuration

Up to version 1.2.0, StoRM WebDAV supported only flat authorization on
storage areas. This means that the same access privileges would be granted
to all the members of trusted VOMS Virtual Organization, or OpenID
Connect/OAuth token issuer.

This authorization model is good enough in many scientific computing
scenarios, and is the main one in use in WLCG computing.

Starting with version 1.3.1, StoRM WebDAV introduces a new
authorization engine that allows for finer-grained auhtorization.

### Flat authorization configuration

#### Anonymous access

To grant anonymous read access on the storage area, set
`anonymousReadEnabled=true`.

#### X.509 authenticated access

To grant anonymous read access to users authenticated with an X.509
certificate issued by a trusted CA (i.e., users without VOMS
credentials), set `authenticatedReadEnabled=true`.

#### VOMS authorization

Flat VOMS authorization is configured by including the VO that
needs to be trusted in the `vos` property of the storage area
configuration.

#### OAuth/OIDC authorization

Flat OAuth/OIDC authorization is enabled by including the token issuer
in the `orgs` property of the storage area configuration. 

Note that the token issuer must also be included in the list of trusted
issuer for the whole service in the `/etc/storm/webdav/config/application.yml` file.

By default, only read access is granted to the storage area. To enable
write access, set `orgsGrantWritePermission=true`.

### WLCG JWT scope-based authorization

Starting with version 1.3.1, StoRM WebDAV introduces support for WLCG
JWT profile scope-based authorization.

For this to work, add the trusted token issuer to the trusted ones
(see above) and set `wlcgScopeAuthzEnabled=true`.

By default, adding a token issuer to the list of the trusted ones for a
storage area will grant read access to the storage area. If you want to
enforce WLCG scope-based authorization completely, set
`orgsGrantReadPermission=false`.

### Fine-grained authorization

Starting with version 1.3.1, StoRM WebDAV introduces a new authorization
engine which allows for finer-grained authorization. It's possible to
define access policies for individual files/folders in the storage area,
and targeting specific authenticated users or group of users.

More flexibility means also more configuration complexity.
To enable fine-grained authorization on a storage area, set
`fineGrainedAuthzEnabled=true`. The authorization engine will be
activated **together with other authorization mechanism described
above**.

#### Fine-grained authorization policies

Fine-grained authorization policies are defined using spring application
configuration YAML files, i.e., typically by editing the
`/etc/storm/webdav/config/application.yml` file.

An example set of policies is given below:

```yaml
storm:
  authz:
    policies:
    - sa: wlcg
      actions:
      - list
      - read
      effect: permit
      description: Grant read access to any user
      principals:
      - type: anyone
    - sa: wlcg
      actions:
      - all
      effect: permit
      description: Grant read/write access to WLCG VOMS vo users
      principals:
      - type: vo
        params:
          vo: wlcg
```

Two policies are defined, for the `wlcg` storage area.
These policies are flat, i.e. cover the whole storage area.
The first policy allows read access to anonymous users.
The second one allows read/write access to any user authenticated with
VOMS credentials from the `wlcg` VO.

The policy above is basically equivalent to these settings in the
storage area configuration.

```properties
anonymousReadEnabled=true
vos=wlcg
```

Using the fine-grained authorization engine is not convenient if the
access model for the storage area is flat.

Policies are evaluated in the order in which they are defined, and the
fist applicable one is used to render the authorization decision.

A policy contains the following directives:

-   `sa`: The nome of the storage area to which the policy applies.

-   `actions`: a list of actions that the policy is authorizing. Possible
    values:

    -   `list`: the ability to list directory contents
    -   `read`: the ability to read files
    -   `write`: the ability to write files and create directories
    -   `delete`: the ability to delete files/directories
    -   `all`: a catchall for all other actions

-   `paths`: the list of paths, within the storage area, the policy
    applies to. If omitted, the policy will apply to all paths.

    To match a directory and all its contents, use a wildcard like the
    following: `/some/dir/**`

-   `effect`:

    -   `permit`: defines a permit policy
    -   `deny`: defines a deny policy

-   `description`: A textual description of the policy. This is a required
    field, and is mainly useful to help debugging authorization problems.

-   `principals`: a list of principals that the policy will apply to.
    Simple principal types are defined by a string value, while complex
    principal types require parameters.

    For example, the  `anyone` principal is a simple principal type,
    while the `jwt-group` principal is a complex type that requires
    two parameters: the token issuer URI, and the  group name.

    | _Principal_              | _Type_  | _Description_                                                                                      | _Parameters_                                          |
    | ------------------------ | ------- | -------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
    | `anyone`                 | simple  | will match any user (authenticated or not)                                                         | N/A                                                   |
    | `any-authenticated-user` | simple  | will match any authenticated user                                                                  | N/A                                                   |
    | `anonymous`              | simple  | will match any anonymous user                                                                      | N/A                                                   |
    | `vo`                     | complex | will match authenticated users presenting a valid VOMS    credential for a given VO.               | `vo`: the name of the VO                              |
    | `vo-map`                 | complex | will match authenticated mapped to a VO through a vomap configuration                              | `vo`: the name of the VO                              |
    | `fqan`                   | complex | will match authenticated users presenting a valid VOMS    credential that contains the given FQAN. | `fqan`: the VOMS fqan                                 |
    | `jwt-group`              | complex | will match authenticated users with valid JWT token and belonging to the given group.              | `iss`: the token issuer, `group`: the group name      |
    | `jwt-scope`              | complex | will match authenticated users with valid JWT token bearing the requested scope.                   | `iss`: the token issuer, `scope`: the scope           |
    | `jwt-issuer`             | complex | will match authenticated users with valid JWT token issued by the required token issuer            | `iss`: the token issuer                               |
    | `jwt-subject`            | complex | will match authenticated users with valid JWT token and presenting the requested subject claim.    | `iss`: the token issuer, `sub`: the subject           |
    | `x509-subject`           | complex | will match authenticated users presenting a valid x.509 credential and a given subject             | `subject`: the certificate subject, in RFC2253 format |

## Examples

```yaml
storm:
  authz:
    policies:
    - sa: example
      actions:
      - list
      - read
      effect: permit
      paths:
      - /read-only/**
      description: Grant read access to any authenticated user to /read-only data
      principals:
      - type: anyone
    - sa: example
      actions:
      - all
      effect: permit
      description: Grant read/write access to the whole sa to /example/admins group members
      principals:
      - type: jwt-group
        params:
            iss: https://iam.example
            group: /example/admins
      - type: fqan
        params:
          fqan: /example/admins
```

The policies above grant:

-   read-only access to anyone on the `/read-only` folder, in the `example` storage area
-   all access to the `example` storage area to users in the group
    `/example/admins`, authenticated with VOMS or OpenID Connect
