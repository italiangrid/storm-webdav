#!/bin/bash
set -ex

SA_CONFIG_DIR=${SA_CONFIG_DIR:-/sa.d}
VOMAP_CONFIG_DIR=${VOMAP_CONFIG_DIR:-/vo-mapfiles.d}

cp ${SA_CONFIG_DIR}/* /etc/storm/webdav/sa.d
chown -R storm:storm /etc/storm/webdav/sa.d

cp ${VOMAP_CONFIG_DIR}/* /etc/storm/webdav/vo-mapfiles.d
chown -R storm:storm /etc/storm/webdav/vo-mapfiles.d
