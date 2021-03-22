# Changelog

## 1.4.0 (2021-04-01)

### Added

- [Add support for externalized session management](https://issues.infn.it/jira/browse/STOR-1336)
### Fixed

- [Login with OIDC button not shown for error
  pages](https://issues.infn.it/jira/browse/STOR-1335) 
- [StoRM WebDAV: Login with OIDC button displayed only on storage area index
  page]( https://issues.infn.it/jira/browse/STOR-1332)
- [StoRM WebDAV rpm doesn't set the proper ownership on
  /var/log/storm](https://issues.infn.it/jira/browse/STOR-1298)
- [StoRM WebDAV package should install Java
  11](https://issues.infn.it/jira/browse/STOR-1358)

## 1.2.0 (2019-08-??)

### Added

- [Spring boot updated to 2.1.4.RELEASE][STOR-1098]
- [Introduced support for Conscrypt JSSE provider to improve TLS
  performace][STOR-1097]

### Fixed

- [StoRM WebDAV default configuration does not depend anymore on
  iam-test.indigo-datacloud.eu][STOR-1095]
- [Unreachable OpenID Connect provider causes StoRM WebDAV startup
  failure][STOR-1096]

## 1.1.0 (2019-02-28)

### Added

- Token-based authorization support
- Third-party copy support
- Jetty 9.4 and Spring Boot 2.1 porting
- Dates in logs now are in standard UTC format
- Rotated log files are compressed

### Fixed

- POST handled as GET fixed 


[STOR-1095]: https://issues.infn.it/jira/browse/STOR-1095
[STOR-1096]: https://issues.infn.it/jira/browse/STOR-1096
[STOR-1097]: https://issues.infn.it/jira/browse/STOR-1097
[STOR-1098]: https://issues.infn.it/jira/browse/STOR-1098
