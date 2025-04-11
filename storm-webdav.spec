# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

# Turn off meaningless jar repackaging
%define __jar_repack 0

# Remember to define the base_version macro
%{!?base_version: %global base_version 0.0.0}
%global slash_name storm/webdav

Name:    storm-webdav
Version: %{base_version}
Release: 1%{?dist}
Summary: The StoRM WebDAV server
Group:   Applications/File
License: Apache-2.0
URL:     https://github.com/italiangrid/storm-webdav

BuildArch: noarch

BuildRequires: maven-openjdk17

Requires: java-17-openjdk-headless

%description
StoRM provides an SRM interface to any POSIX filesystem with direct file
access ("file:" transport protocol), but can take advantage of special
features of high performance parallel and cluster file systems, as GPFS from
IBM and Lustre from SUN.

This package provides the StoRM WebDAV server.

%prep

%build
mvn -DskipTests -U clean package

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}
tar -C %{buildroot} -xvzf target/%{name}-server.tar.gz

%files

%attr(644,root,root) %{_exec_prefix}/lib/systemd/system/%{name}.service
%dir %attr(644,root,root) %{_sysconfdir}/systemd/system/%{name}.service.d
%attr(644,root,root) %config(noreplace) %{_sysconfdir}/systemd/system/%{name}.service.d/filelimit.conf
%attr(644,root,root) %config(noreplace) %{_sysconfdir}/systemd/system/%{name}.service.d/storm-webdav.conf

%attr(755,root,root) %dir %{_javadir}/%{name}
%attr(644,root,root) %{_javadir}/%{name}/%{name}-server.jar

%defattr(640,root,storm,755)

%config(noreplace) %{_sysconfdir}/%{slash_name}/logback.xml
%config(noreplace) %{_sysconfdir}/%{slash_name}/logback-access.xml
%config(noreplace) %{_sysconfdir}/%{slash_name}/config/application.yml

%{_sysconfdir}/%{slash_name}/README.md

%dir %{_sysconfdir}/%{slash_name}/sa.d
%{_sysconfdir}/%{slash_name}/sa.d/README.md
%{_sysconfdir}/%{slash_name}/sa.d/*.template

%dir %{_sysconfdir}/%{slash_name}/config
%{_sysconfdir}/%{slash_name}/config/README.md

%dir %{_sysconfdir}/%{slash_name}/vo-mapfiles.d
%{_sysconfdir}/%{slash_name}/vo-mapfiles.d/README.md

%attr(750,storm,storm) %dir %{_localstatedir}/log/storm
%attr(750,storm,storm) %dir %{_localstatedir}/log/storm/webdav
%attr(755,storm,storm) %dir %{_localstatedir}/lib/%{name}/work

%pre
# create user storm, if it does not exist
getent group storm > /dev/null || groupadd -r storm
getent passwd storm > /dev/null || useradd -r -g storm \
  -d %{_sysconfdir}/storm -s /sbin/nologin -c "StoRM server account" storm

%post
# when installing
if [ "$1" = "1" ] ; then
  # enable the service
  systemctl enable %{name}.service
# when upgrading
elif [ $1 -gt 1 ] ; then
  # restart the service
  systemctl daemon-reload
  systemctl restart %{name}.service
fi

%preun
# when uninstalling
if [ "$1" = "0" ] ; then
  # stop and disable service
  systemctl stop %{name}.service
fi

%changelog
