#!/bin/bash
set -ex

CERT_DIR=${CERT_DIR:-/certs}

cp ${CERT_DIR}/* /etc/grid-security
mkdir /etc/grid-security/storm-webdav/
cp ${CERT_DIR}/* /etc/grid-security/storm-webdav/
chown -R storm:storm /etc/grid-security/storm-webdav
