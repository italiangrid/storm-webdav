#!/bin/bash
set -ex

JARDIR=/usr/share/java/storm-webdav

if [ -z "${DONT_UNPACK_TARBALL}" ]; then
  TARFILE="target/storm-webdav-server.tar.gz"
  tar -C / --owner=storm --group=storm -xvzf /code/$TARFILE
fi
