#!/bin/bash
set -ex

SA_CONFIG_DIR=${SA_CONFIG_DIR:-/sa.d}
CONFIG_DIR=${CONFIG_DIR:-/config}

cp ${SA_CONFIG_DIR}/* /etc/storm/webdav/sa.d
chown -R storm:storm /etc/storm/webdav/sa.d

cp ${CONFIG_DIR}/* /etc/storm/webdav/config
chown -R storm:storm /etc/storm/webdav/config